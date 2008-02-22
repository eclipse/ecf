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

package org.eclipse.ecf.discovery.ui.views;

import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.internal.discovery.ui.Messages;
import org.eclipse.jface.viewers.*;

class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
	/**
	 * 
	 */
	private final DiscoveryView discoveryView;

	/**
	 * @param discoveryView
	 */
	ViewContentProvider(DiscoveryView discoveryView) {
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
		if (parent instanceof DiscoveryViewTypeTreeObject)
			return ((DiscoveryViewTypeTreeObject) parent).getChildren();
		return new Object[0];
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof ViewTreeService)
			return ((ViewTreeService) parent).hasChildren();
		if (parent instanceof DiscoveryViewTypeTreeObject)
			return ((DiscoveryViewTypeTreeObject) parent).hasChildren();
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

	void replaceOrAdd(DiscoveryViewTypeTreeObject top, ViewTreeService newEntry) {
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

	void addServiceTypeInfo(String type) {
		final DiscoveryViewTypeTreeObject typenode = findServiceTypeNode(type);
		if (typenode == null) {
			root.addChild(new DiscoveryViewTypeTreeObject(type));
		}
	}

	DiscoveryViewTypeTreeObject findServiceTypeNode(String typename) {
		final ViewTreeObject[] types = root.getChildren();
		for (int i = 0; i < types.length; i++) {
			if (types[i] instanceof DiscoveryViewTypeTreeObject) {
				final String type = types[i].getName();
				if (type.equals(typename))
					return (DiscoveryViewTypeTreeObject) types[i];
			}
		}
		return null;
	}

	void addServiceInfo(IServiceID id) {
		DiscoveryViewTypeTreeObject typenode = findServiceTypeNode(id.getServiceTypeID().getName());
		if (typenode == null) {
			typenode = new DiscoveryViewTypeTreeObject(id.getServiceTypeID().getName());
			root.addChild(typenode);
		}
		final ViewTreeService newEntry = new ViewTreeService(id, id.getServiceName(), null);
		replaceOrAdd(typenode, newEntry);
	}

	void addServiceInfo(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return;
		final IServiceID svcID = serviceInfo.getServiceID();
		DiscoveryViewTypeTreeObject typenode = findServiceTypeNode(svcID.getServiceTypeID().getName());
		if (typenode == null) {
			typenode = new DiscoveryViewTypeTreeObject(svcID.getServiceTypeID().getName());
			root.addChild(typenode);
		}
		replaceOrAdd(typenode, new ViewTreeService(svcID, svcID.getServiceName(), serviceInfo));
	}

	void removeServiceInfo(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return;
		final IServiceID svcID = serviceInfo.getServiceID();
		final DiscoveryViewTypeTreeObject typenode = findServiceTypeNode(svcID.getServiceTypeID().getName());
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
	}
}