/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.rsa;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.internal.remoteservices.ui.Activator;
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

/**
 * @since 3.3
 */
public abstract class AbstractRemoteServiceAdminView extends ViewPart {

	protected TreeViewer viewer;
	protected AbstractRSAContentProvider contentProvider;

	public AbstractRemoteServiceAdminView() {
		super();
	}

	protected RemoteServiceAdmin getLocalRSA() {
		DiscoveryComponent discovery = DiscoveryComponent.getDefault();
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
	}

	protected abstract AbstractRSAContentProvider createContentProvider(IViewSite viewSite);

	protected void updateModel() {
	}

	protected abstract void setupListeners();

	protected void log(int level, String message, Throwable e) {
		Activator.getDefault().getLog().log(new Status(level, Activator.PLUGIN_ID, message, e));
	}

	protected void logWarning(String message, Throwable e) {
		log(IStatus.WARNING, message, e);
	}

	protected void logError(String message, Throwable e) {
		log(IStatus.ERROR, message, e);
	}

	@Override
	public void createPartControl(Composite parent) {

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
		
		setupListeners();

		RemoteServiceAdmin rsa = getLocalRSA();
		if (rsa != null)
			updateModel();
	}

	@Override
	public void setFocus() {
	}

}