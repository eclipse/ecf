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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.internal.discovery.ui.Activator;
import org.eclipse.ecf.internal.discovery.ui.Messages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class DiscoveryView extends ViewPart {

	private static final IContributionItem[][] EMPTY_CONTRIBUTION = new IContributionItem[][] {{}};

	protected TreeViewer viewer;

	Action refreshAction;

	private Action emptyServiceAccessHandlerAction;

	private ServiceTracker discoveryServiceTracker;

	private IServiceListener serviceListener;

	class DiscoveryViewServiceListener implements IServiceListener {
		public void serviceDiscovered(IServiceEvent anEvent) {
			addServiceInfo(anEvent.getServiceInfo());
		}

		public void serviceUndiscovered(IServiceEvent anEvent) {
			removeServiceInfo(anEvent.getServiceInfo());
		}
	}

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new DiscoveryViewContentProvider(this));
		viewer.setLabelProvider(new DiscoveryViewLabelProvider());
		viewer.setInput(getViewSite());
		initializeDiscoveryContainer();
		makeActions();
		hookContextMenu();
		hookActionBars();
	}

	private void hookActionBars() {
		final IActionBars bars = this.getViewSite().getActionBars();
		bars.getMenuManager().add(refreshAction);
		bars.getToolBarManager().add(refreshAction);
	}

	protected IDiscoveryContainerAdapter getDiscovery() {
		if (discoveryServiceTracker == null) {
			discoveryServiceTracker = new ServiceTracker(Activator.getContext(), IDiscoveryService.class.getName(), new ServiceTrackerCustomizer() {

				public Object addingService(ServiceReference reference) {
					final Object result = Activator.getContext().getService(reference);
					initializeDiscoveryContainer((IDiscoveryContainerAdapter) result);
					return result;
				}

				public void modifiedService(ServiceReference reference, Object service) {
					// do nothing
				}

				public void removedService(ServiceReference reference, Object service) {
					// do nothing
				}
			});
			discoveryServiceTracker.open();
		}
		return (IDiscoveryContainerAdapter) discoveryServiceTracker.getService();
	}

	protected void initializeDiscoveryContainer() {
		// Call this to make sure that ECF core plugin is loaded (which will frequently result
		// in discovery creation/starting
		ContainerFactory.getDefault().getDescriptions();
		// Assure ECF core is loaded, and that discovery is started
		initializeDiscoveryContainer(getDiscovery());
	}

	protected synchronized void initializeDiscoveryContainer(IDiscoveryContainerAdapter discovery) {
		IServiceInfo[] existingServices = null;
		if (discovery != null) {
			if (serviceListener == null) {
				serviceListener = new DiscoveryViewServiceListener();
				discovery.addServiceListener(serviceListener);
			}
			existingServices = discovery.getServices();
		}
		// Now show any previously discovered services
		if (existingServices != null) {
			for (int i = 0; i < existingServices.length; i++) {
				addServiceInfo(existingServices[i]);
			}
		}
	}

	public void clearAllServices() {
		final IDiscoveryContainerAdapter discovery = getDiscovery();
		if (discovery != null) {
			synchronized (this) {
				if (serviceListener != null) {
					discovery.removeServiceListener(serviceListener);
					serviceListener = null;
				}
			}
		}
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				final DiscoveryViewContentProvider vcp = (DiscoveryViewContentProvider) viewer.getContentProvider();
				if (vcp != null) {
					vcp.clear();
					refreshView();
				}
			}
		});
	}

	public void addServiceTypeInfo(final String type) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				final DiscoveryViewContentProvider vcp = (DiscoveryViewContentProvider) viewer.getContentProvider();
				if (vcp != null) {
					vcp.addServiceTypeInfo(type);
					refreshView();
				}
			}
		});
	}

	public void addServiceInfo(final IServiceInfo serviceInfo) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				final DiscoveryViewContentProvider vcp = (DiscoveryViewContentProvider) viewer.getContentProvider();
				if (vcp != null) {
					vcp.addServiceInfo(serviceInfo);
					refreshView();
				}
			}
		});
	}

	public void addServiceInfo(final IServiceID id) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				final DiscoveryViewContentProvider vcp = (DiscoveryViewContentProvider) viewer.getContentProvider();
				if (vcp != null) {
					vcp.addServiceInfo(id);
					refreshView();
				}
			}
		});
	}

	public void removeServiceInfo(final IServiceInfo serviceInfo) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				final DiscoveryViewContentProvider vcp = (DiscoveryViewContentProvider) viewer.getContentProvider();
				if (vcp != null) {
					vcp.removeServiceInfo(serviceInfo);
					refreshView();
				}
			}
		});
	}

	protected void refreshView() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh();
				expandAll();
			}
		});
	}

	protected void expandAll() {
		viewer.expandToLevel(3);
	}

	private void makeActions() {
		emptyServiceAccessHandlerAction = new Action() {
			public void run() {
				// Do nothing
			}
		};
		emptyServiceAccessHandlerAction.setText(Messages.DiscoveryView_NO_SERVICE_HANDLER_LABEL);
		emptyServiceAccessHandlerAction.setEnabled(false);

		refreshAction = new Action() {
			public void run() {
				clearAllServices();
				initializeDiscoveryContainer();
			}
		};
		refreshAction.setText(Messages.DiscoveryView_REFRESH_ACTION_LABEL);
		refreshAction.setToolTipText(Messages.DiscoveryView_REFRESH_SERVICES_TOOLTIPTEXT);
		refreshAction.setEnabled(true);
		refreshAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/nav_refresh.gif")); //$NON-NLS-1$
	}

	void fillContextMenu(IMenuManager manager) {
		final DiscoveryViewTreeObject discoveryViewTreeObject = getSelectedTreeObject();
		if (discoveryViewTreeObject != null && discoveryViewTreeObject instanceof DiscoveryViewTreeParent) {
			final DiscoveryViewTreeParent tp = (DiscoveryViewTreeParent) discoveryViewTreeObject;
			final DiscoveryViewContentProvider vcp = (DiscoveryViewContentProvider) viewer.getContentProvider();
			final IServiceInfo serviceInfo = tp.getServiceInfo();
			if ((vcp != null && vcp.isRoot(tp)) || (serviceInfo == null) || (tp.getID() == null)) {
				// If it's root, show nothing.
			} else {
				final IContributionItem[][] contributions = getContributionsForServiceInfo(serviceInfo);
				if (contributions != null) {
					int count = 0;
					for (int i = 0; i < contributions.length; i++) {
						for (int j = 0; j < contributions[i].length; j++) {
							count++;
							manager.add(contributions[i][j]);
						}
						manager.add(new Separator());
					}
					if (count == 0)
						manager.add(emptyServiceAccessHandlerAction);
				}
			}
		}
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected DiscoveryViewTreeObject getSelectedTreeObject() {
		final ISelection selection = viewer.getSelection();
		return (DiscoveryViewTreeObject) ((IStructuredSelection) selection).getFirstElement();
	}

	private IContributionItem[][] getContributionsForServiceInfo(IServiceInfo serviceInfo) {
		final IExtensionRegistry reg = Activator.getDefault().getExtensionRegistry();
		if (reg != null) {
			final IExtensionPoint serviceAccessPoint = reg.getExtensionPoint(Activator.PLUGIN_ID, "serviceAccessHandler"); //$NON-NLS-1$
			if (serviceAccessPoint == null)
				return EMPTY_CONTRIBUTION;
			final IConfigurationElement[] serviceAccessConfigurationElements = serviceAccessPoint.getConfigurationElements();
			final List results = new ArrayList();
			for (int i = 0; i < serviceAccessConfigurationElements.length; i++) {
				try {
					final IServiceAccessHandler sah = (IServiceAccessHandler) serviceAccessConfigurationElements[i].createExecutableExtension("class"); //$NON-NLS-1$
					results.add(sah.getContributionsForService(serviceInfo));
				} catch (final Exception e) {
					Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, IStatus.WARNING, Messages.DiscoveryView_EXCEPTION_CREATING_SERVICEACCESSHANDLER, e));
				}
			}
			return (IContributionItem[][]) results.toArray(new IContributionItem[][] {});
		}
		return EMPTY_CONTRIBUTION;
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

	public void dispose() {
		super.dispose();
		final IDiscoveryContainerAdapter discovery = getDiscovery();
		if (discovery != null)
			discovery.removeServiceListener(serviceListener);
		if (discoveryServiceTracker != null) {
			discoveryServiceTracker.close();
			discoveryServiceTracker = null;
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}