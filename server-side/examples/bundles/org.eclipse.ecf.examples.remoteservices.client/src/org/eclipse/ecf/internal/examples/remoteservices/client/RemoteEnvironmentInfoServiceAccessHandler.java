package org.eclipse.ecf.internal.examples.remoteservices.client;

import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.discovery.ui.handlers.AbstractRemoteServiceAccessHandler;
import org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

public class RemoteEnvironmentInfoServiceAccessHandler extends AbstractRemoteServiceAccessHandler {

	public RemoteEnvironmentInfoServiceAccessHandler() {
	}

	protected List getRemoteServiceContainerAdapters() {
		final List results = new ArrayList();
		final IContainer container = Activator.getDefault().getConnectedContainer();
		if (container == null)
			return results;
		// If it's not connected already, then return empty list
		if (container.getConnectedID() == null)
			return results;
		final IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) container.getAdapter(IRemoteServiceContainerAdapter.class);
		// If namespaces match and there's an adapter then add it to list
		if (container.getConnectNamespace().getName().equals(getConnectNamespace()) && adapter != null)
			results.add(adapter);
		return results;
	}

	protected IRemoteCall createGetPropertyRemoteCall() throws ClassNotFoundException, NotSerializableException {
		IRemoteEnvironmentInfo.class.getDeclaredMethods();
		final InputDialog input = new InputDialog(null, "Get property", "Enter key", "user.name", null);
		input.setBlockOnOpen(true);
		final Object[] params = new Object[1];
		if (input.open() == Window.OK) {
			params[0] = input.getValue();
			return new IRemoteCall() {

				public String getMethod() {
					return "getProperty";
				}

				public Object[] getParameters() {
					return params;
				}

				public long getTimeout() {
					return 30000;
				}

			};
		} else
			return null;
	}

	protected void showResult(final Object result) {
		final Object display = (result != null && result.getClass().isArray()) ? Arrays.asList((Object[]) result) : result;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(null, "Received Response", NLS.bind("{0}", display));
			}
		});
	}

	protected void showException(final Throwable t) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(null, "Received Exception", NLS.bind("Exception: {0}", t.getLocalizedMessage()));
			}
		});
	}

	protected IContributionItem[] getContributionItemsForService(final IRemoteServiceContainerAdapter adapter) {
		final String className = getRemoteServiceClass();
		if (className == null)
			return NOT_AVAILABLE_CONTRIBUTION;
		else if (className.equals(IRemoteEnvironmentInfo.class.getName()))
			return getContributionItemsForRemoteEnvironmentService(adapter);
		else
			return NOT_AVAILABLE_CONTRIBUTION;
	}

	private IContributionItem createContributionItem(final IRemoteService remoteService, final int invokeMode) {
		final IAction action = new Action() {
			public void run() {
				try {
					final IRemoteCall remoteCall = createGetPropertyRemoteCall();
					if (remoteCall != null) {
						switch (invokeMode) {
							// callSynch
							case 0 :
								showResult(remoteService.callSynch(remoteCall));
								break;
							// callAsynch (listener)
							case 1 :
								remoteService.callAsynch(remoteCall, new IRemoteCallListener() {
									public void handleEvent(IRemoteCallEvent event) {
										if (event instanceof IRemoteCallCompleteEvent) {
											IRemoteCallCompleteEvent complete = (IRemoteCallCompleteEvent) event;
											if (complete.hadException()) {
												showException(complete.getException());
											} else
												showResult(complete.getResponse());
										}
									}
								});
								break;
							// callAsynch (future)
							case 2 :
								showResult(remoteService.callAsynch(remoteCall).get());
								break;
							// proxy
							case 3 :
								IRemoteEnvironmentInfo proxy = (IRemoteEnvironmentInfo) remoteService.getProxy();
								showResult(proxy.getProperty((String) remoteCall.getParameters()[0]));
								break;
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		switch (invokeMode) {
			case 0 :
				action.setText("getProperty (synch)");
				break;
			case 1 :
				action.setText("getProperty (async)");
				break;
			case 2 :
				action.setText("getProperty (future)");
				break;
			case 3 :
				action.setText("getProperty (proxy)");
				break;
		}
		return new ActionContributionItem(action);
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
			return new IContributionItem[] {createContributionItem(remoteService, 0), createContributionItem(remoteService, 1), createContributionItem(remoteService, 2), createContributionItem(remoteService, 3)};
		} catch (final Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return NOT_AVAILABLE_CONTRIBUTION;
		}

	}
}
