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

import java.util.*;
import java.util.List;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.discovery.*;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.service.IDiscoveryService;
import org.eclipse.ecf.internal.discovery.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class DiscoveryView extends ViewPart implements ITabbedPropertySheetPageContributor {

	private static final IContributionItem[][] EMPTY_CONTRIBUTION = new IContributionItem[][] {{}};

	protected TreeViewer viewer;

	Action refreshAction;

	private Action emptyServiceAccessHandlerAction;

	private ServiceTracker discoveryServiceTracker;

	private Map discoveryContainers = new HashMap();

	class DiscoveryViewServiceListener implements IServiceListener {
		public void serviceDiscovered(IServiceEvent anEvent) {
			addServiceInfo(anEvent.getServiceInfo());
		}

		public void serviceUndiscovered(IServiceEvent anEvent) {
			removeServiceInfo(anEvent.getServiceInfo());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor#getContributorId()
	 */
	public String getContributorId() {
		return getSite().getId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySheetPage.class)
			return new TabbedPropertySheetPage(this);
		return super.getAdapter(adapter);
	}

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider(this));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent e) {
				openPropertiesView((IStructuredSelection) e.getSelection());
			}

		});

		getSite().setSelectionProvider(viewer);
		initializeDiscoveryContainer();
		makeActions();
		hookContextMenu();
		hookActionBars();
	}

	void openPropertiesView(IStructuredSelection selection) {
		Object element = selection.getFirstElement();
		if (!(element instanceof ViewTreeService)) {
			return;
		}
		try {
			getSite().getWorkbenchWindow().getActivePage().showView("org.eclipse.ui.views.PropertySheet"); //$NON-NLS-1$
		} catch (PartInitException e) {
			MessageDialog.openError(getSite().getShell(), Messages.DiscoveryView_ERROR_SHOW_VIEW_TITLE, Messages.DiscoveryView_ERROR_SHOW_VIEW_MESSAGE);
		}

	}

	private void hookActionBars() {
		final IActionBars bars = this.getViewSite().getActionBars();
		bars.getToolBarManager().add(refreshAction);
	}

	protected void initializeDiscoveryTracker() {
		if (discoveryServiceTracker == null) {
			//			discoveryServiceTracker = new ServiceTracker(Activator.getContext(), Activator.getFilter(), new ServiceTrackerCustomizer() {
			discoveryServiceTracker = new ServiceTracker(Activator.getContext(), IDiscoveryService.class.getName(), new ServiceTrackerCustomizer() {

				public Object addingService(ServiceReference reference) {
					final Object result = Activator.getContext().getService(reference);
					addDiscoveryContainer((IDiscoveryContainerAdapter) result);
					return result;
				}

				public void modifiedService(ServiceReference reference, Object service) {
					// do nothing
				}

				public void removedService(ServiceReference reference, Object service) {
					removeDiscoveryContainer((IDiscoveryContainerAdapter) service);
				}
			});
			discoveryServiceTracker.open();
		} else {
			discoveryServiceTracker.close();
			discoveryServiceTracker = null;
			initializeDiscoveryTracker();
		}
	}

	protected void initializeDiscoveryContainer() {
		// Call this to make sure that ECF core plugin is loaded (which will frequently result
		// in discovery creation/starting
		ContainerFactory.getDefault().getDescriptions();
		// initializeDiscoveryTracker
		initializeDiscoveryTracker();
	}

	protected void addDiscoveryContainer(final IDiscoveryContainerAdapter discovery) {
		synchronized (discoveryContainers) {
			if (!discoveryContainers.containsKey(discovery)) {
				IServiceListener l = new DiscoveryViewServiceListener();
				discovery.addServiceListener(l);
				discoveryContainers.put(discovery, l);

				Job job = new Job("Getting services...") { //$NON-NLS-1$
					protected IStatus run(IProgressMonitor monitor) {
						IServiceInfo[] existingServices = discovery.getServices();
						// Now show any previously discovered services
						if (existingServices != null) {
							for (int i = 0; i < existingServices.length; i++) {
								addServiceInfo(existingServices[i]);
							}
						}
						return Status.OK_STATUS;
					}
				};
				job.schedule();

			}
		}
	}

	protected void removeDiscoveryContainer(IDiscoveryContainerAdapter discovery) {
		synchronized (discoveryContainers) {
			discoveryContainers.remove(discovery);
		}
	}

	protected void clearDiscoveryContainers() {
		synchronized (discoveryContainers) {
			for (Iterator i = discoveryContainers.keySet().iterator(); i.hasNext();) {
				IDiscoveryContainerAdapter d = (IDiscoveryContainerAdapter) i.next();
				IServiceListener l = (IServiceListener) discoveryContainers.get(d);
				if (l != null)
					d.removeServiceListener(l);
			}
		}
	}

	public void clearAllServices() {
		clearDiscoveryContainers();
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
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
				final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
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
				final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
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
				final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
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
				final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
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
		final ViewTreeObject viewTreeObject = getSelectedTreeObject();
		if (viewTreeObject != null && viewTreeObject instanceof ViewTreeService) {
			final ViewTreeService tp = (ViewTreeService) viewTreeObject;
			final ViewContentProvider vcp = (ViewContentProvider) viewer.getContentProvider();
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

	protected ViewTreeObject getSelectedTreeObject() {
		final ISelection selection = viewer.getSelection();
		return (ViewTreeObject) ((IStructuredSelection) selection).getFirstElement();
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
					IContributionItem[] contributions = sah.getContributionsForService(serviceInfo);
					if (contributions != null) {
						results.add(contributions);
					}
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
		clearDiscoveryContainers();
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