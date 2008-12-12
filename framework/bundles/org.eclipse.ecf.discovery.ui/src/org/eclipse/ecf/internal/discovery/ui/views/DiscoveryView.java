/****************************************************************************
 * Copyright (c) 2008 Versant Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Versant Corp. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.discovery.ui.views;

import org.eclipse.ecf.discovery.ui.model.provider.DiscoveryEditingDomainProvider;
import org.eclipse.ecf.internal.discovery.ui.statusline.AdapterFactoryStatuslineProvider;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;


public class DiscoveryView extends ViewPart {

	public static final String ID = "org.eclipse.ecf.discovery.ui.DiscoveryView";

	private DrillDownAdapter drillDownAdapter;

	private TreeViewer selectionViewer;

	private CollapseAllHandler collapseHandler;


	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * This creates a context menu for the viewer and adds a listener as well registering the menu for extension. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 */
	protected void createContextMenuFor(StructuredViewer viewer) {
		MenuManager contextMenu = new MenuManager("#PopUp"); //$NON-NLS-1$
		contextMenu.add(new Separator("additions")); //$NON-NLS-1$
		contextMenu.setRemoveAllWhenShown(true);
		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(contextMenu, viewer);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		ComposedAdapterFactory adapterFactory = DiscoveryEditingDomainProvider.eINSTANCE.getAdapterFactory();
		
		// create the viewer
		selectionViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		selectionViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
		selectionViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
		selectionViewer.setComparator(new ViewerComparator());
		selectionViewer.setFilters(getViewerFilters());
		getSite().setSelectionProvider(selectionViewer);

		// populate the viewer with the model if available
		EList resources = DiscoveryEditingDomainProvider.eINSTANCE.getEditingDomain().getResourceSet()
				.getResources();
		if (resources != null) {
			selectionViewer.setInput(resources.get(0));
			selectionViewer.setSelection(new StructuredSelection(resources.get(0)), true);
		}

		new AdapterFactoryTreeEditor(selectionViewer.getTree(), adapterFactory);
		selectionViewer.addPostSelectionChangedListener(new AdapterFactoryStatuslineProvider(adapterFactory,
				getViewSite().getActionBars().getStatusLineManager()));
		
		drillDownAdapter = new DrillDownAdapter(selectionViewer);
		createContextMenuFor(selectionViewer);
		hookContextMenu();
		contributeToActionBars();
		
		// add collapse handler
		IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		collapseHandler = new CollapseAllHandler(selectionViewer);
		handlerService.activateHandler(CollapseAllHandler.COMMAND_ID, collapseHandler);
		
		// add DND support
		Transfer[] supportedTransfers = { LocalSelectionTransfer.getTransfer() };
		selectionViewer.addDragSupport(DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE, supportedTransfers, new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(selectionViewer.getSelection());
			}
		});
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "org.eclipse.ecf.discovery.ui.ServiceView");
	}

	/**
	 * @return
	 */
	private ViewerFilter[] getViewerFilters() {
		//TODO lookup view filters via EP
		return new ViewerFilter[0];
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);

		// Other plug-ins can contribute their actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		drillDownAdapter.addNavigationActions(manager);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
			 */
			public void menuAboutToShow(IMenuManager manager) {
				//TODO https://bugs.eclipse.org/bugs/show_bug.cgi?id=151604
				// add a menu listener 
		        // that will fire a selection changed event, in order
		        // to update the selection in contributed actions
				selectionViewer.setSelection(selectionViewer.getSelection());
				
				DiscoveryView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(selectionViewer.getControl());
		selectionViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, selectionViewer);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		selectionViewer.getControl().setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		if(collapseHandler != null) {
			collapseHandler.dispose();
		}
	}
}
