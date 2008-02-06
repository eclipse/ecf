package org.eclipse.ecf.internal.examples.remoteservices.client;

import java.io.NotSerializableException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.discovery.ui.handlers.AbstractRemoteServiceAccessHandler;
import org.eclipse.ecf.examples.remoteservices.common.IConcatService;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallEvent;
import org.eclipse.ecf.remoteservice.util.RemoteCallMethod;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

public class ConcatRemoteServiceAccessHandler extends AbstractRemoteServiceAccessHandler {

	public ConcatRemoteServiceAccessHandler() {
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

	protected IRemoteCall getRemoteCallForConcatService() throws ClassNotFoundException, NotSerializableException {
		final Class clazz = Class.forName(getRemoteServiceClass());
		final Method[] methods = clazz.getDeclaredMethods();
		final InputDialog input = new InputDialog(null, "Invoke concat", "Enter two parameters for concatination (separated by comma)", System.getProperty("user.name") + ", is cool", null);
		input.setBlockOnOpen(true);
		final Object[] params = new Object[2];
		if (input.open() == Window.OK) {
			final String res = input.getValue();
			final StringTokenizer toks = new StringTokenizer(res, ",");
			if (toks.countTokens() >= 2) {
				params[0] = toks.nextToken();
				params[1] = toks.nextToken();
			} else if (toks.countTokens() >= 1) {
				params[0] = toks.nextToken();
				params[1] = " is cool";
			} else {
				params[0] = System.getProperty("user.name");
				params[1] = " is cool";
			}
		}
		final List results = new ArrayList();
		for (int i = 0; i < methods.length; i++) {
			results.add(new RemoteCallMethod(methods[i], params));
		}
		return (IRemoteCall) results.get(0);
	}

	protected void showResult(final Object result) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(null, "Got Result", NLS.bind("Got result!\n\nThe result was: {0}", result));
			}
		});
	}

	protected void showException(final Throwable t) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(null, "Got Exception", NLS.bind("Got Exception!  The exception was {0}", t.getLocalizedMessage()));
			}
		});
	}

	protected IContributionItem[] getContributionItemsForService(final IRemoteServiceContainerAdapter adapter) {
		final String className = getRemoteServiceClass();
		if (className == null || !className.equals(IConcatService.class.getName()))
			return NOT_AVAILABLE_CONTRIBUTION;
		try {
			final IRemoteServiceReference[] references = getRemoteServiceReferences(adapter);
			if (references == null)
				return NOT_AVAILABLE_CONTRIBUTION;
			final IRemoteService remoteService = adapter.getRemoteService(references[0]);

			final IAction callSynchAction = new Action() {
				public void run() {
					try {
						final IRemoteCall remoteCall = getRemoteCallForConcatService();
						showResult(remoteService.callSynch(remoteCall));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			callSynchAction.setText("Synchronous concat...");
			final IAction callAsynchAction = new Action() {
				public void run() {
					try {
						final IRemoteCall remoteCall = getRemoteCallForConcatService();
						remoteService.callAsynch(remoteCall, new IRemoteCallListener() {
							public void handleEvent(IRemoteCallEvent event) {
								System.out.println("handleEvent(" + event + ")");
								if (event instanceof IRemoteCallCompleteEvent) {
									IRemoteCallCompleteEvent complete = (IRemoteCallCompleteEvent) event;
									if (complete.hadException()) {
										showException(complete.getException());
									} else
										showResult(complete.getResponse());
								}
							}
						});
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			callAsynchAction.setText("Asynchronous concat...");
			return new IContributionItem[] {new ActionContributionItem(callSynchAction), new ActionContributionItem(callAsynchAction)};
		} catch (final Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return NOT_AVAILABLE_CONTRIBUTION;
		}
	}
}
