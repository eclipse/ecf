/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.service;

import java.util.Collection;
import java.util.Map;

import org.eclipse.ecf.internal.remoteservices.ui.Messages;
import org.eclipse.ecf.remoteserviceadmin.ui.service.model.AbstractServicesContentProvider;
import org.eclipse.ecf.remoteserviceadmin.ui.service.model.AbstractServicesNode;
import org.eclipse.ecf.remoteserviceadmin.ui.service.model.RegisteringBundleIdNode;
import org.eclipse.ecf.remoteserviceadmin.ui.service.model.ServiceNode;
import org.eclipse.ecf.remoteserviceadmin.ui.service.model.ServicesContentProvider;
import org.eclipse.ecf.remoteserviceadmin.ui.service.model.ServicesRootNode;
import org.eclipse.ecf.remoteserviceadmin.ui.service.model.UsingBundleIdsNode;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.ServiceEvent;

/**
 * @since 3.3
 */
public abstract class AbstractServicesView extends ViewPart {

	protected TreeViewer viewer;
	protected AbstractServicesContentProvider contentProvider;

	public AbstractServicesView() {
		super();
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
				AbstractServicesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected AbstractServicesNode getSelectedNode() {
		return ((AbstractServicesNode) ((ITreeSelection) viewer.getSelection()).getFirstElement());
	}

	@Override
	public void dispose() {
		super.dispose();
		viewer = null;
		contentProvider = null;
	}

	protected abstract AbstractServicesContentProvider createContentProvider(IViewSite viewSite);

	protected void updateModel() {
	}

	protected abstract void setupListeners();

	public void setSelectedService(final long serviceId) {
		if (viewer == null)
			return;
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer == null)
					return;
				ServiceNode sn = findServiceNode(serviceId);
				if (sn != null)
					viewer.setSelection(new StructuredSelection(sn));
			}
		});
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
		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof ServiceNode && e2 instanceof ServiceNode) {
					return new Long(((ServiceNode) e2).getServiceId() - ((ServiceNode) e1).getServiceId()).intValue();
				}
				return super.compare(viewer, e1, e2);
			}
		});

		makeActions();
		hookContextMenu();

		viewSite.setSelectionProvider(viewer);

		setupListeners();

		updateModel();
	}

	@Override
	public void setFocus() {
	}

	protected ServiceNode createServiceNode(long serviceId, long bundleId, long[] usingBundleIds,
			Map<String, Object> properties) {
		ServiceNode result = new ServiceNode(bundleId, usingBundleIds, properties);
		result.addChild(new RegisteringBundleIdNode(bundleId));
		result.addChild(new UsingBundleIdsNode(Messages.AbstractServicesView_UsingBundlesNodeName, usingBundleIds));
		return result;
	}

	protected void removedService(long serviceId) {
		ServiceNode sn = findServiceNode(serviceId);
		if (sn != null)
			getServicesRoot().removeChild(sn);
	}

	protected ServiceNode findServiceNode(long serviceId) {
		AbstractServicesNode[] services = getServicesRoot().getChildren();
		for (AbstractServicesNode asn : services) {
			if (asn instanceof ServiceNode) {
				ServiceNode sn = (ServiceNode) asn;
				if (serviceId == sn.getServiceId())
					return sn;
			}
		}
		return null;
	}

	protected void modifiedService(long serviceId, Map<String, Object> properties) {
		ServiceNode sn = findServiceNode(serviceId);
		if (sn != null)
			sn.setProperties(properties);
	}

	protected ServicesRootNode getServicesRoot() {
		return ((ServicesContentProvider) contentProvider).getServicesRoot();
	}

	protected void addServiceNodes(final Collection<ServiceNode> sns) {
		if (viewer == null)
			return;
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				ServicesRootNode srn = getServicesRoot();
				for (ServiceNode sn : sns)
					srn.addChild(sn);
				viewer.setExpandedState(getServicesRoot(), true);
				viewer.refresh();
			}
		});
	}

	protected void updateService(final int type, final long serviceId, final long bundleId, final long[] usingBundleIds,
			final Map<String, Object> properties) {
		if (viewer == null)
			return;
		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				switch (type) {
				// add
				case ServiceEvent.REGISTERED:
					getServicesRoot().addChild(createServiceNode(serviceId, bundleId, usingBundleIds, properties));
					break;
				// modified properties
				case ServiceEvent.MODIFIED:
					modifiedService(serviceId, properties);
					break;
				// removed
				case ServiceEvent.UNREGISTERING:
					removedService(serviceId);
					break;
				}
				viewer.setExpandedState(getServicesRoot(), true);
				viewer.refresh();
			}
		});

	}

}