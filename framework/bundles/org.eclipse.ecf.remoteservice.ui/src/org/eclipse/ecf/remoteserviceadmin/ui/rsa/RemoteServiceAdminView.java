package org.eclipse.ecf.remoteserviceadmin.ui.rsa;

import java.util.List;

import org.eclipse.ecf.internal.remoteservices.ui.DiscoveryComponent;
import org.eclipse.ecf.internal.remoteservices.ui.Messages;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ExportReference;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ExportRegistration;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportReference;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin.ImportRegistration;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.AbstractRSANode;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.AbstractRegistrationNode;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.EndpointDescriptionRSANode;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.ExportRegistrationNode;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.ExportedServicesRootNode;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.ImportRegistrationNode;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.ImportedEndpointsRootNode;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.RSAContentProvider;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.ServiceIdNode;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;

/**
 * @since 3.3
 */
public class RemoteServiceAdminView extends ViewPart {

	public static final String ID_VIEW = "org.eclipse.ecf.remoteserviceadmin.ui.views.RSAView"; //$NON-NLS-1$

	private DiscoveryComponent discovery;
	protected TreeViewer viewer;

	protected RSAContentProvider contentProvider;
	protected Action closeExportAction;
	protected Action closeImportAction;

	public RemoteServiceAdminView() {
	}

	@Override
	public void dispose() {
		super.dispose();
		viewer = null;
		contentProvider = null;
		if (discovery != null) {
			discovery.setRSAView(null);
			discovery = null;
		}
	}

	@Override
	public void createPartControl(Composite parent) {

		this.discovery = DiscoveryComponent.getDefault();
		this.discovery.setRSAView(this);

		IViewSite viewSite = getViewSite();

		this.contentProvider = createContentProvider(viewSite);

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(this.contentProvider);
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		viewer.setInput(viewSite);

		makeActions();
		hookContextMenu();
		
		viewSite.setSelectionProvider(viewer);

		RemoteServiceAdmin rsa = this.discovery.getRSA();
		if (rsa != null) updateModel();
	}

	protected RSAContentProvider createContentProvider(IViewSite viewSite) {
		return new RSAContentProvider(viewSite);
	}
	
	protected void fillContextMenu(IMenuManager manager) {
		ITreeSelection selection = (ITreeSelection) viewer.getSelection();
		if (selection != null) {
			Object e = selection.getFirstElement();
			if (e instanceof ImportRegistrationNode) {
				manager.add(closeImportAction);
			} else if (e instanceof ExportRegistrationNode) {
				manager.add(closeExportAction);
			}
		}
	}

	protected void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RemoteServiceAdminView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void makeActions() {
		RemoteServiceAdmin rsa = discovery.getRSA();

		closeExportAction = createCloseAction();
		closeExportAction.setText("Unexport Service");
		closeExportAction.setEnabled(rsa != null);

		closeImportAction = createCloseAction();
		closeImportAction.setText("Unimport Service");
		closeImportAction.setEnabled(rsa != null);

	}

	protected Action createCloseAction() {
		return new Action() {
			public void run() {
				AbstractRegistrationNode n = getRegistrationNodeSelected();
				if (n != null) 
					n.close();
			}
		};
	}
	
	protected AbstractRSANode getSelectedNode() {
		return ((AbstractRSANode) ((ITreeSelection) viewer.getSelection())
				.getFirstElement());
	}

	protected AbstractRegistrationNode getRegistrationNodeSelected() {
		AbstractRSANode aen = getSelectedNode();
		return (aen instanceof AbstractRegistrationNode) ? (AbstractRegistrationNode) aen : null;
	}

	@Override
	public void setFocus() {
	}

	public void handleRSAEvent(final RemoteServiceAdminEvent event) {
		if (viewer == null)
			return;
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				RemoteServiceAdmin rsa = getRSA();
				if (rsa != null) {
					switch (event.getType()) {
					case RemoteServiceAdminEvent.EXPORT_REGISTRATION:
					case RemoteServiceAdminEvent.EXPORT_UNREGISTRATION:
					case RemoteServiceAdminEvent.EXPORT_ERROR:
					case RemoteServiceAdminEvent.EXPORT_UPDATE:
					case RemoteServiceAdminEvent.EXPORT_WARNING:
						updateModel(1);
						break;
					case RemoteServiceAdminEvent.IMPORT_REGISTRATION:
					case RemoteServiceAdminEvent.IMPORT_UNREGISTRATION:
					case RemoteServiceAdminEvent.IMPORT_ERROR:
					case RemoteServiceAdminEvent.IMPORT_UPDATE:
					case RemoteServiceAdminEvent.IMPORT_WARNING:
						updateModel(2);
						break;
					}
				}
			}
		});
	}

	protected RemoteServiceAdmin getRSA() {
		return (discovery == null) ? null : discovery.getRSA();
	}

	protected void updateExports(ExportedServicesRootNode exportedRoot) {
		RemoteServiceAdmin rsa = getRSA();
		if (rsa != null && exportedRoot != null) {
			exportedRoot.clearChildren();
			List<ExportRegistration> exportRegistrations = rsa.getExportedRegistrations();
			for (ExportRegistration er : exportRegistrations) {
				ExportRegistrationNode exportRegistrationNode = new ExportRegistrationNode(er);
				ExportReference eRef = (ExportReference) er.getExportReference();
				if (eRef != null) {
					exportRegistrationNode.addChild(new ServiceIdNode(eRef.getExportedService(), Messages.RSAView_SERVICE_ID_LABEL));
					EndpointDescription ed = (EndpointDescription) eRef.getExportedEndpoint();
					if (ed != null)
						exportRegistrationNode.addChild(new EndpointDescriptionRSANode(ed));
				}
				exportedRoot.addChild(exportRegistrationNode);
			}
		}
	}
	
	protected void updateExports() {
		updateExports(contentProvider.getExportedServicesRoot());
	}

	protected void updateImports(ImportedEndpointsRootNode importedRoot) {
		RemoteServiceAdmin rsa = getRSA();
		if (rsa != null && importedRoot != null) {
			importedRoot.clearChildren();
			List<ImportRegistration> importRegistrations = rsa.getImportedRegistrations();
			for (ImportRegistration ir : importRegistrations) {
				ImportRegistrationNode importRegistrationNode = new ImportRegistrationNode(ir);
				ImportReference iRef = (ImportReference) ir.getImportReference();
				if (iRef != null) {
					importRegistrationNode.addChild(new ServiceIdNode(iRef.getImportedService(), Messages.RSAView_PROXY_SERVICE_ID_LABEL));
					EndpointDescription ed = (EndpointDescription) iRef.getImportedEndpoint();
					if (ed != null)
						importRegistrationNode.addChild(new EndpointDescriptionRSANode(ed, ir));
				}
				importedRoot.addChild(importRegistrationNode);
			}
		}		
	}
	
	protected void updateImports() {
		updateImports(contentProvider.getImportedEndpointsRoot());
	}

	protected void updateModel() {
		updateModel(0);
	}

	protected 
	void updateModel(final int type) {
		if (viewer == null)
			return;
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				switch (type) {
				// both
				case 0:
					updateExports();
					updateImports();
					break;
				// exports
				case 1:
					updateExports();
					break;
				// imports
				case 2:
					updateImports();
					break;
				}
				viewer.setExpandedState(contentProvider.getExportedServicesRoot(), true);
				viewer.setExpandedState(contentProvider.getImportedEndpointsRoot(), true);
				viewer.refresh();
			}
		});

	}

}
