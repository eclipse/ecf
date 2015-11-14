/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
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
public abstract class AbstractRemoteServiceAdminView extends ViewPart {

	private DiscoveryComponent discovery;
	protected TreeViewer viewer;
	protected RSAContentProvider contentProvider;
	public AbstractRemoteServiceAdminView() {
		super();
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

	protected void updateModel(final int type) {
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

	protected RSAContentProvider createContentProvider(IViewSite viewSite) {
		return new RSAContentProvider(viewSite);
	}

	protected void fillContextMenu(IMenuManager manager) {
		
	}
	
	protected void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				AbstractRemoteServiceAdminView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void makeActions() {
		
	}
	
	protected AbstractRSANode getSelectedNode() {
		return ((AbstractRSANode) ((ITreeSelection) viewer.getSelection())
				.getFirstElement());
	}

	protected AbstractRegistrationNode getSelectedRegistrationNode() {
		AbstractRSANode aen = getSelectedNode();
		return (aen instanceof AbstractRegistrationNode) ? (AbstractRegistrationNode) aen : null;
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

	public abstract void handleRSAEvent(RemoteServiceAdminEvent event);
	
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

	@Override
	public void setFocus() {
	}

}