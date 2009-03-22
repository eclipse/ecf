package org.eclipse.ecf.tutorial.lab1.actions;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.tutorial.internal.lab1.Activator;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class Lab1Action implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	
	public static final String R_OSGI_TYPE = "ecf.r_osgi.peer";
	public static final String GENERIC_TYPE = "ecf.generic.client";
	
	/**
	 * The constructor.
	 */
	public Lab1Action() {
	}

	protected IContainer createContainer(String type) throws ContainerCreateException {
		return Activator.getDefault().getContainerFactory().createContainer(type);
	}
	
	protected IRemoteServiceContainerAdapter getRemoteServiceContainerAdapter(IContainer container) {
		return (IRemoteServiceContainerAdapter) container.getAdapter(IRemoteServiceContainerAdapter.class);
	}

	protected ID createTargetID(IContainer container, String target) {
		return IDFactory.getDefault().createID(container.getConnectNamespace(),target);
	}
	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		try {
			
			IContainer container = createContainer(R_OSGI_TYPE);
			IRemoteServiceContainerAdapter adapter = getRemoteServiceContainerAdapter(container);
			// Create target ID
			String target = "r-osgi://localhost:9278";
			ID targetID = createTargetID(container,target);
			// Get and resolve remote service reference
			IRemoteServiceReference[] ref = adapter.getRemoteServiceReferences(targetID, org.eclipse.ecf.examples.remoteservices.common.IRemoteEnvironmentInfo.class.getName(), null);
			IRemoteService svc = adapter.getRemoteService(ref[0]);
			// get proxy
			IRemoteEnvironmentInfo proxy = (IRemoteEnvironmentInfo) svc.getProxy();
			// Call it!
			String osArch = proxy.getOSArch();
			// Show result
			MessageDialog.openInformation(
					window.getShell(),
					"ECF Lab 1",
					"Target "+target+" has OS Arch="+osArch);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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