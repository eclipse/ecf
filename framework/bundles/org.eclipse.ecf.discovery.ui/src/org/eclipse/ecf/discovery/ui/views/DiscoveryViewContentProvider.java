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

import java.util.Enumeration;

import org.eclipse.ecf.discovery.IContainerServiceInfoAdapter;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.internal.discovery.ui.Messages;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;

class DiscoveryViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
	/**
	 * 
	 */
	private final DiscoveryView discoveryView;

	/**
	 * @param discoveryView
	 */
	DiscoveryViewContentProvider(DiscoveryView discoveryView) {
		this.discoveryView = discoveryView;
	}

	private DiscoveryViewTreeParent invisibleRoot;

	protected DiscoveryViewTreeParent root;

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		// nothing to do
	}

	public void dispose() {
		// nothing to do
	}

	public Object[] getElements(Object parent) {
		if (parent.equals(this.discoveryView.getViewSite())) {
			if (invisibleRoot == null)
				initialize();
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		if (child instanceof DiscoveryViewTreeObject)
			return ((DiscoveryViewTreeObject) child).getParent();
		return null;
	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof DiscoveryViewTreeParent)
			return ((DiscoveryViewTreeParent) parent).getChildren();
		return new Object[0];
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof DiscoveryViewTreeParent)
			return ((DiscoveryViewTreeParent) parent).hasChildren();
		return false;
	}

	private void initialize() {
		invisibleRoot = new DiscoveryViewTreeParent(null, "", null); //$NON-NLS-1$
		root = new DiscoveryViewTreeParent(null, Messages.DiscoveryView_Services, null);
		invisibleRoot.addChild(root);
	}

	public void clear() {
		if (root != null) {
			root.clearChildren();
		}
	}

	public boolean isRoot(DiscoveryViewTreeParent tp) {
		if (tp != null && tp == root)
			return true;
		return false;
	}

	void replaceOrAdd(DiscoveryViewTreeParent top, DiscoveryViewTreeParent newEntry) {
		final DiscoveryViewTreeObject[] childs = top.getChildren();
		for (int i = 0; i < childs.length; i++) {
			if (childs[i] instanceof DiscoveryViewTreeParent) {
				final IServiceID childID = ((DiscoveryViewTreeParent) childs[i]).getID();
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
		final DiscoveryViewTreeParent typenode = findServiceTypeNode(type);
		if (typenode == null) {
			root.addChild(new DiscoveryViewTreeParent(null, type, null));
		}
	}

	DiscoveryViewTreeParent findServiceTypeNode(String typename) {
		final DiscoveryViewTreeObject[] types = root.getChildren();
		for (int i = 0; i < types.length; i++) {
			if (types[i] instanceof DiscoveryViewTreeParent) {
				final String type = types[i].getName();
				if (type.equals(typename))
					return (DiscoveryViewTreeParent) types[i];
			}
		}
		return null;
	}

	void addServiceInfo(IServiceID id) {
		DiscoveryViewTreeParent typenode = findServiceTypeNode(id.getServiceTypeID().getName());
		if (typenode == null) {
			typenode = new DiscoveryViewTreeParent(null, id.getServiceTypeID().getName(), null);
			root.addChild(typenode);
		}
		final DiscoveryViewTreeParent newEntry = new DiscoveryViewTreeParent(id, id.getServiceName(), null);
		replaceOrAdd(typenode, newEntry);
	}

	void addServiceInfo(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return;
		final IServiceID svcID = serviceInfo.getServiceID();
		DiscoveryViewTreeParent typenode = findServiceTypeNode(svcID.getServiceTypeID().getName());
		if (typenode == null) {
			typenode = new DiscoveryViewTreeParent(null, svcID.getServiceTypeID().getName(), null);
			root.addChild(typenode);
		}
		final DiscoveryViewTreeParent newEntry = new DiscoveryViewTreeParent(svcID, svcID.getServiceName(), serviceInfo);
		newEntry.addChild(new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryView_AddressLabel, serviceInfo.getLocation())));
		newEntry.addChild(new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryView_TypeLabel, svcID.getServiceTypeID().getName())));
		newEntry.addChild(new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryViewContentProvider_TYPE_INTERNAL_LABEL, svcID.getServiceTypeID().getInternal())));
		newEntry.addChild(new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryViewContentProvider_TYPE_NAMESPACE_LABEL, svcID.getServiceTypeID().getNamespace().getName())));
		newEntry.addChild(new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryViewContentProvider_SERVICE_NAME_LABEL, svcID.getServiceName())));
		newEntry.addChild(new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryViewContentProvider_NAME_LABEL, svcID.getServiceTypeID().getNamespace().getName())));
		newEntry.addChild(new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryViewContentProvider_SERVICE_NAMESPACE_LABEL, svcID.getNamespace().getName())));
		final DiscoveryViewTreeObject prioo = new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryView_PriorityLabel, Integer.toString(serviceInfo.getPriority())));
		newEntry.addChild(prioo);
		final DiscoveryViewTreeObject weighto = new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryView_WeightLabel, Integer.toString(serviceInfo.getWeight())));
		newEntry.addChild(weighto);
		final DiscoveryViewTreeParent propertyParent = new DiscoveryViewTreeParent(null, Messages.DiscoveryViewContentProvider_SERVICE_PROPERTIES_LABEL, null);
		final IServiceProperties props = serviceInfo.getServiceProperties();
		for (final Enumeration e = props.getPropertyNames(); e.hasMoreElements();) {
			final Object key = e.nextElement();
			if (key instanceof String) {
				final String keys = (String) key;
				final String val = props.getPropertyString(keys);
				if (val != null) {
					final DiscoveryViewTreeObject prop = new DiscoveryViewTreeObject(keys + '=' + val);
					propertyParent.addChild(prop);
				}
			}
		}
		newEntry.addChild(propertyParent);
		final IContainerServiceInfoAdapter containerServiceInfo = (IContainerServiceInfoAdapter) serviceInfo.getAdapter(IContainerServiceInfoAdapter.class);
		if (containerServiceInfo != null) {
			final String containerFactory = containerServiceInfo.getContainerFactoryName();
			if (containerFactory != null) {
				final DiscoveryViewTreeParent containerServiceInfoParent = new DiscoveryViewTreeParent(null, Messages.DiscoveryViewContentProvider_CONTAINER_SERVICE_INFO_LABEL, null);
				containerServiceInfoParent.addChild(new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryViewContentProvider_CONTAINER_FACTORY_LABEL, containerServiceInfo.getContainerFactoryName())));
				containerServiceInfoParent.addChild(new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryViewContentProvider_CONNECT_TARGET_LABEL, containerServiceInfo.getConnectTarget())));
				containerServiceInfoParent.addChild(new DiscoveryViewTreeObject(NLS.bind(Messages.DiscoveryViewContentProvider_CONNECT_REQUIRES_PASSWORD_LABEL, containerServiceInfo.connectRequiresPassword())));
				newEntry.addChild(containerServiceInfoParent);
			}
		}
		replaceOrAdd(typenode, newEntry);
	}

	void removeServiceInfo(IServiceInfo serviceInfo) {
		if (serviceInfo == null)
			return;
		final IServiceID svcID = serviceInfo.getServiceID();
		final DiscoveryViewTreeParent typenode = findServiceTypeNode(svcID.getServiceTypeID().getName());
		if (typenode == null)
			return;
		final DiscoveryViewTreeObject[] childs = typenode.getChildren();
		for (int i = 0; i < childs.length; i++) {
			if (childs[i] instanceof DiscoveryViewTreeParent) {
				final DiscoveryViewTreeParent parent = ((DiscoveryViewTreeParent) childs[i]);
				if (parent.getID().equals(svcID)) {
					typenode.removeChild(parent);
					if (typenode.getChildren().length == 0) {
						final DiscoveryViewTreeParent grandParent = typenode.getParent();
						grandParent.removeChild(typenode);
					}
				}
			}
		}
	}
}