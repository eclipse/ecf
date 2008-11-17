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

package org.eclipse.ecf.internal.discovery.ui;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.ui.views.DiscoveryView;
import org.eclipse.jface.viewers.*;

public class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
	/**
	 * 
	 */
	private final DiscoveryView discoveryView;

	/**
	 * @param discoveryView
	 */
	public ViewContentProvider(DiscoveryView discoveryView) {
		this.discoveryView = discoveryView;
	}

	protected ViewTreeService root;

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		// nothing to do
	}

	public void dispose() {
		// nothing to do
	}

	public Object[] getElements(Object parent) {
		if (parent.equals(this.discoveryView.getViewSite())) {
			if (root == null)
				initialize();
			return getChildren(root);
		}
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		if (child instanceof ViewTreeObject)
			return ((ViewTreeObject) child).getParent();
		return null;
	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof ViewTreeService)
			return ((ViewTreeService) parent).getChildren();
		if (parent instanceof ViewTreeType)
			return ((ViewTreeType) parent).getChildren();
		return new Object[0];
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof ViewTreeService)
			return ((ViewTreeService) parent).hasChildren();
		if (parent instanceof ViewTreeType)
			return ((ViewTreeType) parent).hasChildren();
		return false;
	}

	private void initialize() {
		root = new ViewTreeService(null, Messages.DiscoveryView_Services, null);
	}

	public void clear() {
		if (root != null) {
			root.clearChildren();
		}
	}

	public boolean isRoot(ViewTreeService tp) {
		if (tp != null && tp == root)
			return true;
		return false;
	}

	void replaceOrAdd(ViewTreeType top, ViewTreeService newEntry) {
		final ViewTreeObject[] childs = top.getChildren();
		for (int i = 0; i < childs.length; i++) {
			if (childs[i] instanceof ViewTreeService) {
				final IServiceID childID = ((ViewTreeService) childs[i]).getID();
				if (childID.equals(newEntry.getID())) {
					// It's already there...replace
					top.removeChild(childs[i]);
				}
			}
		}
		// Now add
		top.addChild(newEntry);
	}

	public void addServiceTypeInfo(String type) {
		final ViewTreeType typenode = findServiceTypeNode(type);
		if (typenode == null) {
			root.addChild(new ViewTreeType(type));
		}
	}

	ViewTreeType findServiceTypeNode(String typename) {
		final ViewTreeObject[] types = root.getChildren();
		for (int i = 0; i < types.length; i++) {
			if (types[i] instanceof ViewTreeType) {
				final String type = types[i].getName();
				if (type.equals(typename))
					return (ViewTreeType) types[i];
			}
		}
		return null;
	}

	private ViewTreeType findServiceTypeNode(IServiceTypeID typeID) {
		String[] services = typeID.getServices();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < services.length; i++) {
			buffer.append(services[i]).append(':');
		}
		buffer.deleteCharAt(buffer.length() - 1);

		return findServiceTypeNode(buffer.toString());
	}

	public void addServiceInfo(IServiceID id) {
		ViewTreeType typenode = findServiceTypeNode(id.getServiceTypeID());
		if (typenode == null) {
			typenode = new ViewTreeType(id.getServiceTypeID());
			root.addChild(typenode);
		}
		final ViewTreeService newEntry = new ViewTreeService(id, id.getServiceName(), null);
		replaceOrAdd(typenode, newEntry);
	}

	public void addServiceInfo(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return;
		final IServiceID svcID = serviceInfo.getServiceID();
		ViewTreeType typenode = findServiceTypeNode(svcID.getServiceTypeID());
		if (typenode == null) {
			typenode = new ViewTreeType(svcID.getServiceTypeID());
			root.addChild(typenode);
		}
		replaceOrAdd(typenode, new ViewTreeService(svcID, svcID.getServiceName(), serviceInfo));
	}

	public void removeServiceInfo(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return;
		final IServiceID svcID = serviceInfo.getServiceID();
		final ViewTreeType typenode = findServiceTypeNode(svcID.getServiceTypeID().getName());
		if (typenode == null)
			return;
		final ViewTreeObject[] childs = typenode.getChildren();
		for (int i = 0; i < childs.length; i++) {
			if (childs[i] instanceof ViewTreeService) {
				final ViewTreeService parent = ((ViewTreeService) childs[i]);
				if (parent.getID().equals(svcID)) {
					typenode.removeChild(parent);
				}
			}
		}
		if (typenode.getChildren().length == 0)
			root.removeChild(typenode);
	}
}