package org.eclipse.ecf.tutorial.lab1.actions;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.tutorial.internal.lab1.Activator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class SampleAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	
	public static final String R_OSGI_TYPE = "ecf.r_osgi.peer";
	public static final String GENERIC_TYPE = "ecf.generic.client";
	
	/**
	 * The constructor.
	 */
	public SampleAction() {
	}

	protected IContainerFactory getContainerFactory() {
		return Activator.getDefault().getContainerManager().getContainerFactory();
	}
	
	protected IContainer createContainer(String type) throws ContainerCreateException {
		return getContainerFactory().createContainer(type);
	}
	
	protected IRemoteServiceContainerAdapter getRemoteServicesContainerAdapter(IContainer container) {
		return (IRemoteServiceContainerAdapter) container.getAdapter(IRemoteServiceContainerAdapter.class);
	}
	
	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		MessageDialog.openInformation(
			window.getShell(),
			"ECF EclipseCon2009 Tutorial Lab 1",
			"Hello, Eclipse world");
	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}