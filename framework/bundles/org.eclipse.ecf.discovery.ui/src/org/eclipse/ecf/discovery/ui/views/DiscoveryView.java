/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.discovery.ui.views;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.identity.ServiceIDFactory;
import org.eclipse.ecf.internal.discovery.ui.Messages;
import org.eclipse.ecf.ui.SharedImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class DiscoveryView extends ViewPart {

	protected static final String DISCOVERED_SERVICES = Messages.DiscoveryView_Services;

	protected static final int SERVICE_INFO_TIMEOUT = 3000;

	protected static final int TREE_EXPANSION_LEVELS = 3;

	private TreeViewer viewer;

	private Action requestServiceInfoAction;
	private Action registerServiceTypeAction;
	private Action connectToAction;
	private Action disconnectContainerAction;
	private Action connectContainerAction;

	IDiscoveryController controller = null;

	String[] controllerServiceTypes = null;

	protected boolean showTypeDetails = false;

	public void setShowTypeDetails(boolean val) {
		showTypeDetails = val;
		refreshView();
	}

	protected boolean isSupportedServiceType(String serviceType) {
		if (controllerServiceTypes == null || serviceType == null)
			return false;
		for (int i = 0; i < controllerServiceTypes.length; i++) {
			if (serviceType.equals(controllerServiceTypes[i]))
				return true;
		}
		return false;
	}

	public void setDiscoveryController(final IDiscoveryController controller) {
		this.controller = controller;
	}

	protected void setConnectMenus(boolean connected) {
		disconnectContainerAction.setEnabled(connected);
		connectContainerAction.setEnabled(!connected);
	}

	protected boolean isConnected() {
		final IDiscoveryController c = getController();
		if (c == null)
			return false;
		else {
			final IContainer container = c.getContainer();
			if (container == null)
				return false;
			else
				return true;
		}
	}

	protected IDiscoveryController getController() {
		return controller;
	}

	protected IContainer getIContainer() {
		final IDiscoveryController c = getController();
		if (c == null)
			return null;
		else
			return c.getContainer();
	}

	protected IDiscoveryContainerAdapter getDiscoveryContainer() {
		final IDiscoveryController c = getController();
		if (c == null)
			return null;
		else
			return c.getDiscoveryContainer();
	}

	class TreeObject implements IAdaptable {
		private final String name;

		private TreeParent parent;

		public TreeObject(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setParent(TreeParent parent) {
			this.parent = parent;
		}

		public TreeParent getParent() {
			return parent;
		}

		public String toString() {
			return getName();
		}

		public Object getAdapter(Class key) {
			return null;
		}
	}

	class TreeParent extends TreeObject {
		private final ArrayList children;

		private final IServiceID id;

		private final IServiceInfo serviceInfo;

		public TreeParent(IServiceID id, String name, IServiceInfo svcInfo) {
			super(name);
			this.id = id;
			children = new ArrayList();
			serviceInfo = svcInfo;
		}

		public IServiceInfo getServiceInfo() {
			return serviceInfo;
		}

		public IServiceID getID() {
			return id;
		}

		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}

		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}

		public TreeObject[] getChildren() {
			return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
		}

		public boolean hasChildren() {
			return children.size() > 0;
		}

		public void clearChildren() {
			children.clear();
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		private TreeParent invisibleRoot;

		protected TreeParent root;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot == null)
					initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof TreeObject)
				return ((TreeObject) child).getParent();
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent) parent).getChildren();
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent) parent).hasChildren();
			return false;
		}

		private void initialize() {
			invisibleRoot = new TreeParent(null, "", null); //$NON-NLS-1$
			root = new TreeParent(null, DISCOVERED_SERVICES, null);
			invisibleRoot.addChild(root);
		}

		public void clear() {
			if (root != null) {
				root.clearChildren();
			}
		}

		public boolean isRoot(TreeParent tp) {
			if (tp != null && tp == root)
				return true;
			else
				return false;
		}

		void replaceOrAdd(TreeParent top, TreeParent newEntry) {
			final TreeObject[] childs = top.getChildren();
			for (int i = 0; i < childs.length; i++) {
				if (childs[i] instanceof TreeParent) {
					final IServiceID childID = ((TreeParent) childs[i]).getID();
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
			final TreeParent typenode = findServiceTypeNode(type);
			if (typenode == null) {
				root.addChild(new TreeParent(null, type, null));
			}
		}

		TreeParent findServiceTypeNode(String typename) {
			final TreeObject[] types = root.getChildren();
			for (int i = 0; i < types.length; i++) {
				if (types[i] instanceof TreeParent) {
					final String type = types[i].getName();
					if (type.equals(typename))
						return (TreeParent) types[i];
				}
			}
			return null;
		}

		void addServiceInfo(IServiceID id) {
			TreeParent typenode = findServiceTypeNode(id.getServiceTypeID().getName());
			if (typenode == null) {
				typenode = new TreeParent(null, id.getServiceTypeID().getName(), null);
				root.addChild(typenode);
			}
			final TreeParent newEntry = new TreeParent(id, id.getServiceName(), null);
			replaceOrAdd(typenode, newEntry);
		}

		void addServiceInfo(IServiceInfo serviceInfo) {
			if (serviceInfo == null)
				return;
			final IServiceID svcID = serviceInfo.getServiceID();
			TreeParent typenode = findServiceTypeNode(svcID.getServiceTypeID().getName());
			if (typenode == null) {
				typenode = new TreeParent(null, svcID.getServiceTypeID().getName(), serviceInfo);
				root.addChild(typenode);
			}
			final TreeParent newEntry = new TreeParent(svcID, svcID.getServiceName(), serviceInfo);
			final InetAddress addr = serviceInfo.getAddress();
			if (addr != null) {
				final TreeObject toaddr = new TreeObject(NLS.bind(Messages.DiscoveryView_AddressLabel, addr.getHostAddress()));
				newEntry.addChild(toaddr);
			}
			final TreeObject typeo = new TreeObject(NLS.bind(Messages.DiscoveryView_TypeLabel, svcID.getServiceTypeID().getName()));
			newEntry.addChild(typeo);
			final TreeObject porto = new TreeObject(NLS.bind(Messages.DiscoveryView_PortLabel, Integer.toString(serviceInfo.getPort())));
			newEntry.addChild(porto);
			final TreeObject prioo = new TreeObject(NLS.bind(Messages.DiscoveryView_PriorityLabel, Integer.toString(serviceInfo.getPriority())));
			newEntry.addChild(prioo);
			final TreeObject weighto = new TreeObject(NLS.bind(Messages.DiscoveryView_WeightLabel, Integer.toString(serviceInfo.getWeight())));
			newEntry.addChild(weighto);
			final IServiceProperties props = serviceInfo.getServiceProperties();
			if (props != null) {
				for (final Enumeration e = props.getPropertyNames(); e.hasMoreElements();) {
					final Object key = e.nextElement();
					if (key instanceof String) {
						final String keys = (String) key;
						final String val = props.getPropertyString(keys);
						if (val != null) {
							final TreeObject prop = new TreeObject(keys + '=' + val);
							newEntry.addChild(prop);
						}
					}
				}
			}
			replaceOrAdd(typenode, newEntry);
		}

		void removeServiceInfo(IServiceInfo serviceInfo) {
			if (serviceInfo == null)
				return;
			final IServiceID svcID = serviceInfo.getServiceID();
			final TreeParent typenode = findServiceTypeNode(svcID.getServiceTypeID().getName());
			if (typenode == null)
				return;
			final TreeObject[] childs = typenode.getChildren();
			for (int i = 0; i < childs.length; i++) {
				if (childs[i] instanceof TreeParent) {
					final TreeParent parent = (TreeParent) childs[i];
					final IServiceID existingID = parent.getID();
					if (existingID.equals(svcID)) {
						typenode.removeChild(parent);
						if (typenode.getChildren().length == 0) {
							final TreeParent grandParent = typenode.getParent();
							grandParent.removeChild(typenode);
						}
					}
				}
			}
		}
	}

	protected String cleanTypeName(String inputName) {
		if (showTypeDetails)
			return inputName;
		String res = inputName.trim();
		while (res.startsWith("_")) { //$NON-NLS-1$
			res = res.substring(1);
		}
		final int dotLoc = res.indexOf('.');
		if (dotLoc != -1) {
			res = res.substring(0, dotLoc);
		}
		return res;
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			if (obj instanceof TreeParent) {
				final TreeParent tp = (TreeParent) obj;
				final IServiceID svcID = tp.getID();
				if (svcID == null)
					return cleanTypeName(tp.getName());
			}
			return super.getText(obj);
		}

		public Image getImage(Object obj) {
			String imageKey = null;
			if (obj instanceof TreeParent) {
				if (((TreeParent) obj).getID() != null) {
					imageKey = ISharedImages.IMG_OBJ_ELEMENT;
				} else {
					imageKey = ISharedImages.IMG_OBJ_FOLDER;
				}
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}

	/**
	 * The constructor.
	 */
	public DiscoveryView() {
	}

	public void clearAllServices() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
					if (vcp != null) {
						vcp.clear();
						refreshView();
					}
				} catch (final Exception e) {
				}
			}
		});
	}

	public void addServiceTypeInfo(final String type) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
					if (vcp != null) {
						vcp.addServiceTypeInfo(type);
						refreshView();
					}
				} catch (final Exception e) {
				}
			}
		});
	}

	public void addServiceInfo(final IServiceInfo serviceInfo) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
					if (vcp != null) {
						vcp.addServiceInfo(serviceInfo);
						refreshView();
					}
				} catch (final Exception e) {
				}
			}
		});
	}

	public void addServiceInfo(final IServiceID id) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
					if (vcp != null) {
						vcp.addServiceInfo(id);
						refreshView();
					}
				} catch (final Exception e) {
				}
			}
		});
	}

	public void removeServiceInfo(final IServiceInfo serviceInfo) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
					if (vcp != null) {
						vcp.removeServiceInfo(serviceInfo);
						refreshView();
					}
				} catch (final Exception e) {
				}
			}
		});
	}

	protected void refreshView() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					viewer.refresh();
					expandAll();
				} catch (final Exception e) {
				}
			}
		});
	}

	protected void expandAll() {
		viewer.expandToLevel(TREE_EXPANSION_LEVELS);
	}

	/*
	 * private void hookDoubleClickAction() { viewer.addDoubleClickListener(new
	 * IDoubleClickListener() { public void doubleClick(DoubleClickEvent event) {
	 * selectedDoubleClickAction.run(); } }); }
	 * 
	 */
	private void makeActions() {
		requestServiceInfoAction = new Action() {
			public void run() {
				final TreeObject treeObject = getSelectedTreeObject();
				if (treeObject instanceof TreeParent) {
					final TreeParent p = (TreeParent) treeObject;
					final IServiceID targetID = p.getID();
					final IDiscoveryContainerAdapter dcontainer = getDiscoveryContainer();
					if (dcontainer != null) {
						dcontainer.requestServiceInfo(targetID, SERVICE_INFO_TIMEOUT);
					}
				}
			}
		};
		requestServiceInfoAction.setText(Messages.DiscoveryView_RequestInfo);
		requestServiceInfoAction.setToolTipText(Messages.DiscoveryView_RequestInfoTooltip);
		requestServiceInfoAction.setEnabled(true);

		registerServiceTypeAction = new Action() {
			public void run() {
				final TreeObject treeObject = getSelectedTreeObject();
				if (treeObject instanceof TreeParent) {
					final TreeParent p = (TreeParent) treeObject;
					final IDiscoveryContainerAdapter dcontainer = getDiscoveryContainer();
					if (dcontainer != null) {
						try {
							final IServiceID serviceID = ServiceIDFactory.getDefault().createServiceID(dcontainer.getServicesNamespace(), p.getName());
							dcontainer.registerServiceType(serviceID.getServiceTypeID());
						} catch (final IDCreateException e) {
							e.printStackTrace();
							return;
						}
					}
				}
			}
		};
		registerServiceTypeAction.setText(Messages.DiscoveryView_RegisterType);
		registerServiceTypeAction.setToolTipText(Messages.DiscoveryView_RegisterTypeTooltip);
		registerServiceTypeAction.setEnabled(true);

		connectToAction = new Action() {
			public void run() {
				final TreeObject treeObject = getSelectedTreeObject();
				if (treeObject instanceof TreeParent) {
					final TreeParent p = (TreeParent) treeObject;
					connectToService(p.getServiceInfo());
				}
			}
		};
		connectToAction.setText(Messages.DiscoveryView_ConnectTo);
		connectToAction.setToolTipText(Messages.DiscoveryView_ConnectToTooltip);
		connectToAction.setEnabled(true);

		disconnectContainerAction = new Action() {
			public void run() {
				if (MessageDialog.openConfirm(DiscoveryView.this.getViewSite().getShell(), Messages.DiscoveryView_StopDiscoveryTitle, Messages.DiscoveryView_StopDiscoveryDescription)) {
					final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
					if (vcp != null) {
						if (isConnected()) {
							final IDiscoveryController dc = getController();
							dc.stopDiscovery();
							clearAllServices();
							setConnectMenus(dc.isDiscoveryStarted());
						}
					}
				}
			}
		};
		disconnectContainerAction.setText(Messages.DiscoveryView_StopDiscovery);
		disconnectContainerAction.setToolTipText(Messages.DiscoveryView_StopDiscoveryTooltip);
		disconnectContainerAction.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.IMG_DISCONNECT));
		disconnectContainerAction.setDisabledImageDescriptor(SharedImages.getImageDescriptor(SharedImages.IMG_DISCONNECT_DISABLED));
		final IDiscoveryController c = getController();
		if (c == null)
			disconnectContainerAction.setEnabled(false);
		else
			disconnectContainerAction.setEnabled((isConnected() && c.isDiscoveryStarted()));

		connectContainerAction = new Action() {
			public void run() {
				final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
				if (vcp != null) {
					if (!isConnected()) {
						final IDiscoveryController c = getController();
						if (c != null) {
							c.startDiscovery();
							setDiscoveryController(c);
							setConnectMenus(c.isDiscoveryStarted());
						}
					}
				}
			}
		};
		connectContainerAction.setText(Messages.DiscoveryView_StartDiscovery);
		connectContainerAction.setToolTipText(Messages.DiscoveryView_StartDiscoveryTooltip);
		if (c == null) {
			connectContainerAction.setEnabled(false);
		} else {
			connectContainerAction.setEnabled(!c.isDiscoveryStarted());
		}
		connectContainerAction.setImageDescriptor(SharedImages.getImageDescriptor(SharedImages.IMG_ADD));
	}

	private void fillContextMenu(IMenuManager manager) {
		final TreeObject treeObject = getSelectedTreeObject();
		if (treeObject != null && treeObject instanceof TreeParent) {
			final TreeParent tp = (TreeParent) treeObject;
			final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
			if (vcp != null && vcp.isRoot(tp)) {
				// If it's root, show nothing.
			} else {
				final IServiceID svcID = tp.getID();
				if (svcID != null) {
					final IServiceInfo svcInfo = tp.getServiceInfo();
					connectToAction.setText(NLS.bind(Messages.DiscoveryView_ConnectToService, svcID.getServiceName()));
					manager.add(connectToAction);
					manager.add(new Separator());
					connectToAction.setEnabled(false);
					requestServiceInfoAction.setText(NLS.bind(Messages.DiscoveryView_RequestInfoAboutService, svcID.getServiceName()));
					manager.add(requestServiceInfoAction);
					requestServiceInfoAction.setEnabled(true);
					if (svcInfo != null) {
						if (svcInfo.isResolved() && isSupportedServiceType(svcID.getServiceTypeID().getName())) {
							try {
								// try to create a URI to see if the format is
								// correct before we enable the action
								new URI(svcInfo.getServiceID().getName());
								connectToAction.setEnabled(true);
							} catch (final URISyntaxException e) {
							}
						}
					}
				}
			}
		}
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void connectToService(IServiceInfo svcInfo) {
		if (controller != null) {
			controller.connectToService(svcInfo);
		}
	}

	protected TreeObject getSelectedTreeObject() {
		final ISelection selection = viewer.getSelection();
		final Object obj = ((IStructuredSelection) selection).getFirstElement();
		final TreeObject treeObject = (TreeObject) obj;
		return treeObject;
	}

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void contributeToActionBars() {
		final IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(connectContainerAction);
		manager.add(new Separator());
		manager.add(disconnectContainerAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(connectContainerAction);
		manager.add(new Separator());
		manager.add(disconnectContainerAction);
	}

	private void hookContextMenu() {
		final MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DiscoveryView.this.fillContextMenu(manager);
			}
		});
		final Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				final Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				final TreeObject treeObject = (TreeObject) obj;
				if (treeObject != null && treeObject instanceof TreeParent) {
					final TreeParent tp = (TreeParent) treeObject;
					if (tp.getID() != null) {
						final IServiceInfo info = tp.getServiceInfo();
						if (info != null && info.isResolved()) {
							connectToAction.run();
						} else {
							requestServiceInfoAction.run();
						}
					}
				}
			}
		});
	}

	public void dispose() {
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}