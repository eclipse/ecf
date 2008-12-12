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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.IAsyncResult;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteService;
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

public class RemoteEnvironmentInfoServiceAccessHandler /*extends AbstractRemoteServiceAccessHandler */ {

	private static final String[] serviceType = new String[]{Constants.DISCOVERY_SERVICE_TYPE, "IRemoteEnvironmentInfo"};
	static Map remoteEnvironmentContainers = new HashMap();

//	/* (non-Javadoc)
//	 * @see org.eclipse.ecf.discovery.ui.views.AbstractRemoteServiceAccessHandler#getContributionsForMatchingService()
//	 */
//	protected IContributionItem[] getContributionsForMatchingService() {
//		IContainer container = null;
//		try {
//			container = findContainerForService(getServiceInfo().getServiceID());
//			if (container == null)
//				return EMPTY_CONTRIBUTION;
//			// Get adapter
//			final IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) container.getAdapter(IRemoteServiceContainerAdapter.class);
//			if (adapter == null)
//				return EMPTY_CONTRIBUTION;
//			// If not connected then get connect contribution
//			if (container.getConnectedID() == null) {
//				// The container is not connected so we create/return action for connecting
//				return getContributionItemsForConnect(container, createConnectID());
//			} else {
//				return getContributionItemsForConnectedContainer(container, adapter);
//			}
//		} catch (final ECFException e) {
//			return EMPTY_CONTRIBUTION;
//		}
//	}
//
//	/**
//	 * @param adapter
//	 * @return
//	 */
//	protected IContributionItem[] getContributionItemsForRemoteServiceAdapter(final IRemoteServiceContainerAdapter adapter) {
//		try {
//			final IRemoteServiceReference[] references = getRemoteServiceReferencesForRemoteServiceAdapter(adapter);
//			if (references == null)
//				return NOT_AVAILABLE_CONTRIBUTION;
//			final IRemoteService remoteService = adapter.getRemoteService(references[0]);
//			return new IContributionItem[] {createDialogContributionItem(IRemoteEnvironmentInfo.class, remoteService)};
//		} catch (final Exception e1) {
//			return NOT_AVAILABLE_CONTRIBUTION;
//		}
//	}

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
//						showException(e);
					}
				}
			}

		};
		action.setText(NLS.bind("Invoke method on IRemoteEnvironmentInfo service...", IRemoteEnvironmentInfo.class.getName()));
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
//					if (complete.hadException()) {
//						showException(complete.getException());
//					} else
//						showResult(interfaceClass.getName(), remoteCall, complete.getResponse());
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
//		showResult(interfaceClass.getName(), remoteCall, result);
	}

	private void invokeFuture(final Class interfaceClass, final IRemoteService remoteService, final IRemoteCall remoteCall) throws InvocationTargetException, InterruptedException {
		// Make async call with future result
		final IAsyncResult asyncResult = remoteService.callAsynch(remoteCall);
		// Call blocking get and show result
//		showResult(interfaceClass.getName(), remoteCall, asyncResult.get());
	}

	private void invokeRemoteEnvironmentInfo(final IRemoteCall remoteCall, IRemoteEnvironmentInfo proxy) throws Exception {
		Object result = null;
		if (remoteCall.getMethod().equals("getCommandLineArgs")) {
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
//			showException(new Exception("Invalid method selected"));
			return;
		}
//		showResult(IRemoteEnvironmentInfo.class.getName(), remoteCall, result);
	}

	private void invokeRemoteEnvironmentInfoProxy(final IRemoteService remoteService, final IRemoteCall remoteCall) throws Exception {
		invokeRemoteEnvironmentInfo(remoteCall, (IRemoteEnvironmentInfo) remoteService.getProxy());
	}

	private void invokeOSGiRemoteEnvironmentInfoProxy(final IRemoteCall remoteCall) throws Exception {
//		final ServiceTracker st = new ServiceTracker(Activator.getDefault().getContext(), IRemoteEnvironmentInfo.class.getName(), null);
//		st.open();
		final IRemoteEnvironmentInfo proxy = null/* (IRemoteEnvironmentInfo) st.getService()*/;
//		st.close();
		if (proxy == null) {
//			showException(new ServiceUnavailableException(NLS.bind("{0} remote service not available", IRemoteEnvironmentInfo.class.getName())));
			return;
		}
		invokeRemoteEnvironmentInfo(remoteCall, proxy);
	}

	private IContainer findContainerForService(IServiceID serviceID) throws ContainerCreateException {
		IContainer result = null;
		synchronized (remoteEnvironmentContainers) {
			result = (IContainer) remoteEnvironmentContainers.get(serviceID);
			if (result == null) {
//				result = createContainer();
				remoteEnvironmentContainers.put(serviceID, result);
			}
		}
		return result;
	}

//	private IContributionItem[] getContributionItemsForConnect(final IContainer container, final ID targetID) {
//		final IAction action = new Action() {
//			public void run() {
//				try {
//					// connect to target ID
//					connectContainer(container, targetID, null);
//				} catch (final ContainerConnectException e) {
//					showException(e);
//				}
//			}
//		};
//		action.setText(NLS.bind("Connect to {0}", targetID.getName()));
//		return new IContributionItem[] {new ActionContributionItem(action)};
//	}

//	/* (non-Javadoc)
//	 * @see org.eclipse.ecf.discovery.ui.views.AbstractRemoteServiceAccessHandler#getContributionsForService(org.eclipse.ecf.discovery.IServiceInfo)
//	 */
//	public IContributionItem[] getContributionsForService(IServiceInfo svcInfo) {
//		if (svcInfo == null)
//			return EMPTY_CONTRIBUTION;
//		this.serviceInfo = svcInfo;
//		String[] services = svcInfo.getServiceID().getServiceTypeID().getServices();
//		if(services.length < 2) {
//			return EMPTY_CONTRIBUTION;
//		}
//		for (int i = 0; i < services.length; i++) {
//			if(!services[i].equals(serviceType[i]))
//				return EMPTY_CONTRIBUTION;
//		}
//		return getContributionsForMatchingService();
//	}

}
