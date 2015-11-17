/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.rsa;

import org.eclipse.ecf.internal.remoteservices.ui.DiscoveryComponent;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.AbstractRSAContentProvider;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.model.AbstractRSANode;
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
	protected AbstractRSAContentProvider contentProvider;

	public AbstractRemoteServiceAdminView() {
		super();
	}

	protected RemoteServiceAdmin getLocalRSA() {
		return (discovery == null) ? null : discovery.getRSA();
	}

	protected void fillContextMenu(IMenuManager manager) {

	}

	protected void makeActions() {

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

	protected AbstractRSANode getSelectedNode() {
		return ((AbstractRSANode) ((ITreeSelection) viewer.getSelection()).getFirstElement());
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

	public void handleRSAEvent(RemoteServiceAdminEvent event) {

	}

	protected abstract AbstractRSAContentProvider createContentProvider(IViewSite viewSite);

	protected void updateModel() {

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
		if (rsa != null)
			updateModel();
	}

	@Override
	public void setFocus() {
	}

}