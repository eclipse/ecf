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
package org.eclipse.ecf.presence.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.internal.presence.ui.Messages;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.im.IChatMessageSender;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.roster.IRosterGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * View class for displaying multiple rosters in a tree viewer. This view part
 * implements {@link IMultiRosterViewPart} and provides the ability to display
 * multiple rosters in a single tree viewer. This class may be subclassed as
 * desired to add or customize behavior.
 * 
 */
public class MultiRosterView extends ViewPart implements IMultiRosterViewPart {

	public static final String VIEW_ID = "org.eclipse.ecf.presence.ui.MultiRosterView"; //$NON-NLS-1$

	protected static final int DEFAULT_EXPAND_LEVEL = 3;

	protected TreeViewer treeViewer;

	protected MultiRosterLabelProvider multiRosterLabelProvider;

	protected MultiRosterContentProvider multiRosterContentProvider;

	protected List rosterAccounts = new ArrayList();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		setupTreeViewer(parent);
	}

	protected void setupTreeViewer(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI
				| SWT.V_SCROLL);
		getSite().setSelectionProvider(treeViewer);
		multiRosterContentProvider = new MultiRosterContentProvider();
		multiRosterLabelProvider = new MultiRosterLabelProvider();
		treeViewer.setLabelProvider(multiRosterLabelProvider);
		treeViewer.setContentProvider(multiRosterContentProvider);
		treeViewer.setInput(new Object());
		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent e) {
				IStructuredSelection iss = (IStructuredSelection) e
						.getSelection();
				Object element = iss.getFirstElement();
				if (element instanceof IRosterEntry) {
					MultiRosterView.this.open((IRosterEntry) element);
				}
			}
		});

		contributeToActionBars();
	}

	private boolean find(Collection items, Object entry) {
		for (Iterator it = items.iterator(); it.hasNext();) {
			Object item = it.next();
			if (item instanceof IRosterGroup) {
				if (find(((IRosterGroup) item).getEntries(), entry)) {
					return true;
				}
			} else if (item == entry) {
				return true;
			}
		}
		return false;
	}

	private void open(IRosterEntry entry) {
		synchronized (rosterAccounts) {
			for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
				MultiRosterAccount account = (MultiRosterAccount) i.next();
				IRoster roster = account.getRoster();
				if (find(roster.getItems(), entry)) {
					IChatMessageSender icms = account
							.getPresenceContainerAdapter().getChatManager()
							.getChatMessageSender();
					try {
						MessagesView view = (MessagesView) getSite()
								.getWorkbenchWindow().getActivePage().showView(
										MessagesView.VIEW_ID);
						view.openTab(icms, roster.getUser().getID(), entry
								.getUser().getID());
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		final ViewerFilter filter = new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IRosterEntry) {
					return ((IRosterEntry) element).getPresence().getType() == IPresence.Type.AVAILABLE;
				} else {
					return true;
				}
			}
		};
		IAction filterAction = new Action(Messages.MultiRosterView_ShowOffline,
				IAction.AS_CHECK_BOX) {
			public void run() {
				if (isChecked()) {
					treeViewer.addFilter(filter);
				} else {
					treeViewer.removeFilter(filter);
				}
			}
		};
		manager.add(filterAction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		treeViewer = null;
		multiRosterLabelProvider = null;
		multiRosterContentProvider = null;
		rosterAccounts.clear();
	}

	protected void addRosterAccountsToProviders() {
		for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
			MultiRosterAccount account = (MultiRosterAccount) i.next();
			multiRosterContentProvider.add(account.getRoster());
		}
	}

	protected boolean addRosterAccount(MultiRosterAccount account) {
		if (account != null && rosterAccounts.add(account)) {
			if (multiRosterContentProvider != null) {
				multiRosterContentProvider.add(account.getRoster());
			}
			return true;
		} else {
			return false;
		}
	}

	protected boolean removeRosterAccount(MultiRosterAccount account) {
		if (account != null && rosterAccounts.remove(account)) {
			if (multiRosterContentProvider != null) {
				multiRosterContentProvider.remove(account.getRoster());
			}
			account.dispose();
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	protected void refreshTreeViewer(Object val, boolean labels) {
		if (treeViewer != null) {
			if (val != null) {
				treeViewer.refresh(val, labels);
			} else {
				treeViewer.refresh(labels);
			}
			treeViewer.expandToLevel(DEFAULT_EXPAND_LEVEL);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.ui.IMultiRosterViewPart#addContainer(org.eclipse.ecf.core.IContainer)
	 */
	public boolean addContainer(IContainer container) {
		if (container == null) {
			return false;
		}
		IPresenceContainerAdapter containerAdapter = (IPresenceContainerAdapter) container
				.getAdapter(IPresenceContainerAdapter.class);
		if (containerAdapter == null) {
			return false;
		} else if (addRosterAccount(new MultiRosterAccount(this, container,
				containerAdapter))) {
			refreshTreeViewer(null, true);
			return true;
		} else {
			return false;
		}
	}
}
