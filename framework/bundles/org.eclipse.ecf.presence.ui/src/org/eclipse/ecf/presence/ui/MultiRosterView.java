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
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.presence.ui.Messages;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.IPresenceSender;
import org.eclipse.ecf.presence.Presence;
import org.eclipse.ecf.presence.im.IChatMessageSender;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.roster.IRosterGroup;
import org.eclipse.ecf.presence.roster.IRosterManager;
import org.eclipse.ecf.presence.roster.IRosterSubscriptionListener;
import org.eclipse.ecf.presence.roster.IRosterSubscriptionSender;
import org.eclipse.ecf.ui.SharedImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
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

	private IAction imAction;

	private IAction removeAction;

	private IAction setAvailableAction;

	private IAction setAwayAction;

	private IAction setDNDAction;

	private IAction setInvisibleAction;

	private IAction setOfflineAction;

	private IRosterSubscriptionListener subscriptionListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		setupTreeViewer(parent);
	}

	protected void setupTreeViewer(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL);
		getSite().setSelectionProvider(treeViewer);
		multiRosterContentProvider = new MultiRosterContentProvider();
		multiRosterLabelProvider = new MultiRosterLabelProvider();
		subscriptionListener = new RosterSubscriptionListener();
		treeViewer.setLabelProvider(multiRosterLabelProvider);
		treeViewer.setContentProvider(multiRosterContentProvider);
		treeViewer.setInput(new Object());
		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent e) {
				message((IStructuredSelection) e.getSelection());
			}
		});

		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	private void makeActions() {
		imAction = new Action() {
			public void run() {
				message((IStructuredSelection) treeViewer.getSelection());
			}
		};
		imAction.setImageDescriptor(SharedImages
				.getImageDescriptor(SharedImages.IMG_MESSAGE));

		removeAction = new Action() {
			public void run() {
				IStructuredSelection iss = (IStructuredSelection) treeViewer
						.getSelection();
				remove((IRosterEntry) iss.getFirstElement());
			}
		};
		removeAction.setText(Messages.MultiRosterView_Remove);
		removeAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));

		setAvailableAction = new Action(Messages.MultiRosterView_SetAvailable,
				IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					setAwayAction.setChecked(false);
					setDNDAction.setChecked(false);
					setInvisibleAction.setChecked(false);
					setOfflineAction.setChecked(false);
					sendPresence(IPresence.Mode.AVAILABLE);
				}
			}
		};

		setAwayAction = new Action(Messages.MultiRosterView_SetAway,
				IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					setAvailableAction.setChecked(false);
					setDNDAction.setChecked(false);
					setInvisibleAction.setChecked(false);
					setOfflineAction.setChecked(false);
					sendPresence(IPresence.Mode.AWAY);
				}
			}
		};

		setDNDAction = new Action(Messages.MultiRosterView_SetDoNotDisturb,
				IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					setAvailableAction.setChecked(false);
					setAwayAction.setChecked(false);
					setInvisibleAction.setChecked(false);
					setOfflineAction.setChecked(false);
					sendPresence(IPresence.Mode.DND);
				}
			}
		};

		setInvisibleAction = new Action(Messages.MultiRosterView_SetInvisible,
				IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					setAvailableAction.setChecked(false);
					setAwayAction.setChecked(false);
					setDNDAction.setChecked(false);
					setOfflineAction.setChecked(false);
					sendPresence(IPresence.Mode.INVISIBLE);
				}
			}
		};

		setOfflineAction = new Action(Messages.MultiRosterView_SetOffline,
				IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					setAvailableAction.setChecked(false);
					setAwayAction.setChecked(false);
					setDNDAction.setChecked(false);
					setInvisibleAction.setChecked(false);
					for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
						MultiRosterAccount account = (MultiRosterAccount) i
								.next();
						account.getContainer().disconnect();
					}
				}
			}
		};
		setOfflineAction.setChecked(true);
	}

	private void sendPresence(IPresence.Mode mode) {
		try {
			for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
				MultiRosterAccount account = (MultiRosterAccount) i.next();
				account.getRosterManager().getPresenceSender()
						.sendPresenceUpdate(
								null,
								new Presence(IPresence.Type.AVAILABLE, null,
										mode));
			}
		} catch (ECFException e) {
			e.printStackTrace();
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
	}

	private void fillContextMenu(IMenuManager manager) {
		IStructuredSelection iss = (IStructuredSelection) treeViewer
				.getSelection();
		Object element = iss.getFirstElement();
		if (element instanceof IRosterEntry) {
			IRosterEntry entry = (IRosterEntry) element;
			manager.add(imAction);
			imAction.setText(NLS.bind(Messages.MultiRosterView_SendIM, entry
					.getName()));
			// if the person is not online, we'll disable the action
			imAction
					.setEnabled(entry.getPresence().getType() == IPresence.Type.AVAILABLE);
			manager.add(removeAction);
		}
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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

	private IRosterEntry find(Collection items, ID userID) {
		for (Iterator it = items.iterator(); it.hasNext();) {
			Object item = it.next();
			if (item instanceof IRosterGroup) {
				IRosterEntry entry = find(((IRosterGroup) item).getEntries(),
						userID);
				if (entry != null) {
					return entry;
				}
			} else if (userID.equals(((IRosterEntry) item).getUser().getID())) {
				return (IRosterEntry) item;
			}
		}
		return null;
	}

	private void remove(IRosterEntry entry) {
		synchronized (rosterAccounts) {
			for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
				MultiRosterAccount account = (MultiRosterAccount) i.next();
				IRoster roster = account.getRoster();
				if (find(roster.getItems(), entry)) {
					IRosterSubscriptionSender rss = account
							.getPresenceContainerAdapter().getRosterManager()
							.getRosterSubscriptionSender();
					try {
						rss.sendRosterRemove(entry.getUser().getID());
					} catch (ECFException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
	}

	private void message(IStructuredSelection iss) {
		Object element = iss.getFirstElement();
		if (!(element instanceof IRosterEntry)) {
			return;
		}
		IRosterEntry entry = (IRosterEntry) element;
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
					break;
				}
			}
		}
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		IMenuManager subMenu = new MenuManager(
				Messages.MultiRosterView_SetStatusAs, null);
		subMenu.add(setAvailableAction);
		subMenu.add(setAwayAction);
		subMenu.add(setDNDAction);
		subMenu.add(setInvisibleAction);
		subMenu.add(setOfflineAction);
		manager.add(subMenu);
		manager.add(new Separator());
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
		treeViewer = null;
		multiRosterLabelProvider = null;
		multiRosterContentProvider = null;
		for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
			MultiRosterAccount account = (MultiRosterAccount) i.next();
			account.getRosterManager().removeRosterSubscriptionListener(
					subscriptionListener);
		}
		rosterAccounts.clear();
		super.dispose();
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
			IRosterManager manager = containerAdapter.getRosterManager();
			try {
				if (setAvailableAction.isChecked() || setOfflineAction.isChecked()) {
					manager.getPresenceSender().sendPresenceUpdate(null,
							new Presence(null, null, IPresence.Mode.AVAILABLE));
					setOfflineAction.setChecked(false);
					setAvailableAction.setChecked(true);
				} else if (setAwayAction.isChecked()) {
					manager.getPresenceSender().sendPresenceUpdate(null,
							new Presence(null, null, IPresence.Mode.AWAY));
				} else if (setDNDAction.isChecked()) {
					manager.getPresenceSender().sendPresenceUpdate(null,
							new Presence(null, null, IPresence.Mode.DND));
				} else if (setInvisibleAction.isChecked()) {
					manager.getPresenceSender().sendPresenceUpdate(null,
							new Presence(null, null, IPresence.Mode.INVISIBLE));
				}
			} catch (ECFException e) {
				e.printStackTrace();
			}
			containerAdapter.getRosterManager().addRosterSubscriptionListener(
					subscriptionListener);
			refreshTreeViewer(null, true);
			return true;
		} else {
			return false;
		}
	}

	private class RosterSubscriptionListener implements
			IRosterSubscriptionListener {

		public void handleSubscribeRequest(ID fromID) {
			// TODO Auto-generated method stub
		}

		public void handleSubscribed(ID fromID) {
			// TODO Auto-generated method stub
		}

		public void handleUnsubscribed(ID fromID) {
			synchronized (rosterAccounts) {
				for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
					MultiRosterAccount account = (MultiRosterAccount) i.next();
					final IRosterEntry entry = find(account.getRoster()
							.getItems(), fromID);
					if (entry != null) {
						treeViewer.getControl().getDisplay().asyncExec(
								new Runnable() {
									public void run() {
										treeViewer.remove(entry);
									}
								});
					}
				}
			}
		}

	}
}
