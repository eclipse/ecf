/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.examples.remoteservices.client;

import java.lang.reflect.InvocationTargetException;

import javax.naming.ServiceUnavailableException;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.IAsyncResult;
import org.eclipse.ecf.discovery.ui.views.AbstractRemoteServiceAccessHandler;
import org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;
import org.eclipse.ecf.remoteservices.ui.MethodInvocationDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.osgi.util.tracker.ServiceTracker;

public class RemoteEnvironmentInfoServiceAccessHandler extends AbstractRemoteServiceAccessHandler {

	public RemoteEnvironmentInfoServiceAccessHandler() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.ui.views.AbstractRemoteServiceAccessHandler#getContributionsForMatchingService()
	 */
	protected IContributionItem[] getContributionsForMatchingService() {
		// If singleton not already set, create a new container (of type specified in serviceInfo
		// and set the singleton to it.  If we can't create it for whatever reason, we have no
		// contribution
		if (Activator.getDefault().getContainer() == null) {
			try {
				final IContainer c = createContainer();
				final IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) c.getAdapter(IRemoteServiceContainerAdapter.class);
				if (adapter == null)
					return EMPTY_CONTRIBUTION;
				Activator.getDefault().setContainer(c);
			} catch (final ContainerCreateException e) {
				return EMPTY_CONTRIBUTION;
			}
		}
		// The container is now not null
		final IContainer container = Activator.getDefault().getContainer();
		// not connected already...so setup contribution that allows connect
		final String ns = getConnectNamespace();
		final String id = getConnectID();
		// If there is no connect namespace or connect id specified, then we have no contribution
		if (container == null || ns == null || id == null)
			return EMPTY_CONTRIBUTION;
		// Create a new connect id from namespace and id
		ID connectTargetID = null;
		try {
			connectTargetID = createID(ns, id);
		} catch (final Exception e) {
			return EMPTY_CONTRIBUTION;
		}
		final ID connectedID = container.getConnectedID();
		// If the container is not already connected
		if (connectedID != null) {
			// If we're already connected, and connected to the *wrong* remote, then disconnect
			if (!connectedID.equals(connectTargetID)) {
				container.disconnect();
				// Otherwise we're already connected to the correct container, and we get the normal contributions
			} else
				return getConnectedContributions(container);
		}
		// Otherwise we need to connect so we create a contribution to allow the user to connect
		// Now we get the contribution to make connection to correct connectTargetID
		final ID cTargetID = connectTargetID;
		final IAction action = new Action() {
			public void run() {
				try {
					// Then we connect
					connectContainer(container, cTargetID, null);
					showInformation("Connected", NLS.bind("Connected to {0}", cTargetID.getName()));
				} catch (final ContainerConnectException e) {
					showException(e);
				}
			}
		};
		action.setText(NLS.bind("Connect to {0}", connectTargetID.getName()));
		return new IContributionItem[] {new ActionContributionItem(action)};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.ui.views.AbstractRemoteServiceAccessHandler#getContributionItemsForService(org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter)
	 */
	protected IContributionItem[] getContributionItemsForService(final IRemoteServiceContainerAdapter adapter) {
		final String className = getRemoteServiceClass();
		if (className == null)
			return NOT_AVAILABLE_CONTRIBUTION;
		else if (className.equals(IRemoteEnvironmentInfo.class.getName()))
			return getContributionItemsForRemoteEnvironmentService(adapter);
		else
			return NOT_AVAILABLE_CONTRIBUTION;
	}

	private IContributionItem createDialogContributionItem(final Class interfaceClass, final IRemoteService remoteService) {
		final IAction action = new Action() {
			public void run() {
				final MethodInvocationDialog mid = new MethodInvocationDialog((Shell) null, IRemoteEnvironmentInfo.class);
				if (mid.open() == Window.OK) {
					final int timeout = (mid.getTimeout() > 0) ? mid.getTimeout() : 30000;
					final String methodName = mid.getMethod().getName();
					final Object[] methodArgs = mid.getMethodArguments();
					final IRemoteCall remoteCall = new IRemoteCall() {
						public String getMethod() {
							return methodName;
						}

						public Object[] getParameters() {
							return methodArgs;
						}

						public long getTimeout() {
							return timeout;
						}
					};
					final int invokeType = mid.getInvocationType();
					try {
						switch (invokeType) {
							case MethodInvocationDialog.ASYNC_FIRE_AND_GO :
								invokeAsyncFire(interfaceClass, remoteService, remoteCall);
								break;
							case MethodInvocationDialog.ASYNC_FUTURE_RESULT :
								invokeFuture(interfaceClass, remoteService, remoteCall);
								break;
							case MethodInvocationDialog.ASYNC_LISTENER :
								invokeAsyncListener(interfaceClass, remoteService, remoteCall);
								break;
							case MethodInvocationDialog.OSGI_SERVICE_PROXY :
								invokeOSGiProxy(interfaceClass, remoteCall);
								break;
							case MethodInvocationDialog.REMOTE_SERVICE_PROXY :
								invokeProxy(interfaceClass, remoteService, remoteCall);
								break;
							case MethodInvocationDialog.SYNCHRONOUS :
								invokeSync(interfaceClass, remoteService, remoteCall);
								break;
							default :
								break;
						}
					} catch (final Exception e) {
						showException(e);
					}
				}
			}

		};
		action.setText(NLS.bind("Select method on IRemoteEnvironmentInfo...", IRemoteEnvironmentInfo.class.getName()));
		return new ActionContributionItem(action);
	}

	protected void invokeProxy(Class interfaceClass, IRemoteService remoteService, IRemoteCall remoteCall) throws Exception {
		if (interfaceClass.equals(IRemoteEnvironmentInfo.class))
			invokeRemoteEnvironmentInfoProxy(remoteService, remoteCall);
	}

	protected void invokeOSGiProxy(Class interfaceClass, IRemoteCall remoteCall) throws Exception {
		if (interfaceClass.equals(IRemoteEnvironmentInfo.class))
			invokeOSGiRemoteEnvironmentInfoProxy(remoteCall);
	}

	private void invokeAsyncListener(final Class interfaceClass, final IRemoteService remoteService, final IRemoteCall remoteCall) {
		// Make async call
		remoteService.callAsynch(remoteCall, new IRemoteCallListener() {
			public void handleEvent(IRemoteCallEvent event) {
				if (event instanceof IRemoteCallCompleteEvent) {
					final IRemoteCallCompleteEvent complete = (IRemoteCallCompleteEvent) event;
					if (complete.hadException()) {
						showException(complete.getException());
					} else
						showResult(interfaceClass.getName(), remoteCall, complete.getResponse());
				}
			}
		});
	}

	private void invokeAsyncFire(Class interfaceClass, final IRemoteService remoteService, final IRemoteCall remoteCall) {
		// Make async call
		remoteService.callAsynch(remoteCall);
	}

	private void invokeSync(Class interfaceClass, final IRemoteService remoteService, final IRemoteCall remoteCall) throws ECFException {
		// Make sync call
		final Object result = remoteService.callSynch(remoteCall);
		// Show result
		showResult(interfaceClass.getName(), remoteCall, result);
	}

	private void invokeFuture(final Class interfaceClass, final IRemoteService remoteService, final IRemoteCall remoteCall) throws InvocationTargetException, InterruptedException {
		// Make async call with future result
		final IAsyncResult asyncResult = remoteService.callAsynch(remoteCall);
		// Call blocking get and show result
		showResult(interfaceClass.getName(), remoteCall, asyncResult.get());
	}

	private void invokeRemoteEnvironmentInfo(final IRemoteCall remoteCall, IRemoteEnvironmentInfo proxy) throws Exception {
		Object result = null;
		if (remoteCall.getMethod().equals("getProperty")) {
			result = proxy.getProperty((String) remoteCall.getParameters()[0]);
		} else if (remoteCall.getMethod().equals("getCommandLineArgs")) {
			result = proxy.getCommandLineArgs();
		} else if (remoteCall.getMethod().equals("getFrameworkArgs")) {
			result = proxy.getFrameworkArgs();
		} else if (remoteCall.getMethod().equals("getNL")) {
			result = proxy.getNL();
		} else if (remoteCall.getMethod().equals("getNonFrameworkArgs")) {
			result = proxy.getNonFrameworkArgs();
		} else if (remoteCall.getMethod().equals("getOS")) {
			result = proxy.getOS();
		} else if (remoteCall.getMethod().equals("getOSArch")) {
			result = proxy.getOSArch();
		} else if (remoteCall.getMethod().equals("getWS")) {
			result = proxy.getWS();
		} else {
			showException(new Exception("Invalid method selected"));
			return;
		}
		showResult(IRemoteEnvironmentInfo.class.getName(), remoteCall, result);
	}

	private void invokeRemoteEnvironmentInfoProxy(final IRemoteService remoteService, final IRemoteCall remoteCall) throws Exception {
		invokeRemoteEnvironmentInfo(remoteCall, (IRemoteEnvironmentInfo) remoteService.getProxy());
	}

	private void invokeOSGiRemoteEnvironmentInfoProxy(final IRemoteCall remoteCall) throws Exception {
		final ServiceTracker st = new ServiceTracker(Activator.getDefault().getContext(), IRemoteEnvironmentInfo.class.getName(), null);
		st.open();
		final IRemoteEnvironmentInfo proxy = (IRemoteEnvironmentInfo) st.getService();
		st.close();
		if (proxy == null) {
			showException(new ServiceUnavailableException(NLS.bind("{0} remote service not available", IRemoteEnvironmentInfo.class.getName())));
			return;
		}
		invokeRemoteEnvironmentInfo(remoteCall, proxy);
	}

	/**
	 * @param adapter
	 * @return
	 */
	private IContributionItem[] getContributionItemsForRemoteEnvironmentService(final IRemoteServiceContainerAdapter adapter) {
		try {
			final IRemoteServiceReference[] references = getRemoteServiceReferences(adapter);
			if (references == null)
				return NOT_AVAILABLE_CONTRIBUTION;
			final IRemoteService remoteService = adapter.getRemoteService(references[0]);
			return new IContributionItem[] {createDialogContributionItem(IRemoteEnvironmentInfo.class, remoteService)};
		} catch (final Exception e1) {
			return NOT_AVAILABLE_CONTRIBUTION;
		}

	}
}
