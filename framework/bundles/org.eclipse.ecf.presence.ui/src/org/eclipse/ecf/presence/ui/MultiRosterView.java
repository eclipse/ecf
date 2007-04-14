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
import java.util.Map;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.presence.ui.Activator;
import org.eclipse.ecf.internal.presence.ui.Messages;
import org.eclipse.ecf.internal.presence.ui.dialogs.AddContactDialog;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.Presence;
import org.eclipse.ecf.presence.im.IChatManager;
import org.eclipse.ecf.presence.im.IChatMessageSender;
import org.eclipse.ecf.presence.im.ITypingMessageSender;
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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
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
 */
public class MultiRosterView extends ViewPart implements IMultiRosterViewPart {

	public static final String VIEW_ID = "org.eclipse.ecf.presence.ui.MultiRosterView"; //$NON-NLS-1$

	protected static final int DEFAULT_EXPAND_LEVEL = 3;

	protected TreeViewer treeViewer;

	protected List rosterAccounts = new ArrayList();

	private IMenuManager setStatusMenu;

	private IAction imAction;

	private IAction removeAction;

	private IAction setAvailableAction;

	private IAction setAwayAction;

	private IAction setDNDAction;

	private IAction setInvisibleAction;

	private IAction setOfflineAction;

	private IRosterSubscriptionListener subscriptionListener;

	private IPresenceListener presenceListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		setupTreeViewer(parent);
	}

	protected String getRosterEntryChildrenFromPresence(IRosterEntry entry) {
		IPresence presence = entry.getPresence();
		Map properties = presence.getProperties();
		int fixedEntries = 3;
		StringBuffer buffer = new StringBuffer();
		buffer.append(NLS.bind(Messages.RosterWorkbenchAdapterFactory_Account,
				entry.getUser().getID().getName()));
		buffer.append(Text.DELIMITER);
		buffer.append(NLS.bind(Messages.RosterWorkbenchAdapterFactory_Type,
				presence.getType()));
		buffer.append(Text.DELIMITER);
		buffer.append(NLS.bind(Messages.RosterWorkbenchAdapterFactory_Mode,
				presence.getMode().toString()));
		for (Iterator i = properties.keySet().iterator(); i.hasNext(); fixedEntries++) {
			buffer.append(Text.DELIMITER);
			Object key = i.next();
			buffer.append(key).append(": ").append(properties.get(key)); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	protected void setupTreeViewer(Composite parent) {
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE
				| SWT.V_SCROLL);
		getSite().setSelectionProvider(treeViewer);
		subscriptionListener = new RosterSubscriptionListener();
		presenceListener = new PresenceListener();
		treeViewer.setContentProvider(new MultiRosterContentProvider());
		treeViewer.setLabelProvider(new MultiRosterLabelProvider());
		treeViewer.setInput(rosterAccounts);
		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent e) {
				message((IStructuredSelection) e.getSelection());
			}
		});

		JFaceResources.getColorRegistry().put(ViewerToolTip.HEADER_BG_COLOR,
				new RGB(255, 255, 255));
		JFaceResources.getFontRegistry().put(
				ViewerToolTip.HEADER_FONT,
				JFaceResources.getFontRegistry().getBold(
						JFaceResources.getDefaultFont().getFontData()[0]
								.getName()).getFontData());

		ToolTip toolTip = new ViewerToolTip(treeViewer.getControl());
		toolTip.setShift(new Point(-5, -5));
		toolTip.setHideOnMouseDown(false);

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
					sendPresence(IPresence.Mode.AVAILABLE);
				}
			}
		};

		setAwayAction = new Action(Messages.MultiRosterView_SetAway,
				IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					sendPresence(IPresence.Mode.AWAY);
				}
			}
		};

		setDNDAction = new Action(Messages.MultiRosterView_SetDoNotDisturb,
				IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					sendPresence(IPresence.Mode.DND);
				}
			}
		};

		setInvisibleAction = new Action(Messages.MultiRosterView_SetInvisible,
				IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					sendPresence(IPresence.Mode.INVISIBLE);
				}
			}
		};

		setOfflineAction = new Action(Messages.MultiRosterView_SetOffline,
				IAction.AS_RADIO_BUTTON) {
			public void run() {
				if (isChecked()) {
					for (int i = 0; i < rosterAccounts.size(); i++) {
						MultiRosterAccount account = (MultiRosterAccount) rosterAccounts
								.get(i);
						account.getRosterManager()
								.removeRosterSubscriptionListener(
										subscriptionListener);
						treeViewer.remove(account);
					}
					rosterAccounts.clear();
					refreshTreeViewer(null, false);
					setStatusMenu.setVisible(false);
					getViewSite().getActionBars().getMenuManager()
							.update(false);
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
			imAction.setText(Messages.MultiRosterView_SendIM);
			// if the person is not online, we'll disable the action
			imAction
					.setEnabled(entry.getPresence().getType() == IPresence.Type.AVAILABLE);
			manager.add(removeAction);
		}
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
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
		try {
			IRoster roster = entry.getRoster();
			if (roster != null) {
				roster.getPresenceContainerAdapter().getRosterManager()
						.getRosterSubscriptionSender().sendRosterRemove(
								entry.getUser().getID());
			}
		} catch (ECFException e) {
			e.printStackTrace();
		}
	}

	private void message(IStructuredSelection iss) {
		Object element = iss.getFirstElement();
		if (!(element instanceof IRosterEntry)) {
			return;
		}
		IRosterEntry entry = (IRosterEntry) element;
		IRoster roster = entry.getRoster();
		if (roster != null) {
			IChatManager manager = roster.getPresenceContainerAdapter()
					.getChatManager();
			IChatMessageSender icms = manager.getChatMessageSender();
			ITypingMessageSender itms = manager.getTypingMessageSender();
			try {
				MessagesView view = (MessagesView) getSite()
						.getWorkbenchWindow().getActivePage().showView(
								MessagesView.VIEW_ID);
				view.selectTab(icms, itms, roster.getUser().getID(), entry
						.getUser().getID());
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		setStatusMenu = new MenuManager(Messages.MultiRosterView_SetStatusAs,
				null);
		setStatusMenu.add(setAvailableAction);
		setStatusMenu.add(setAwayAction);
		setStatusMenu.add(setDNDAction);
		setStatusMenu.add(setInvisibleAction);
		setStatusMenu.add(setOfflineAction);
		setStatusMenu.setVisible(false);
		manager.add(setStatusMenu);
		manager.add(new Separator());
		manager.add(new Action(Messages.MultiRosterView_AddContact) {
			public void run() {
				AddContactDialog dialog = new AddContactDialog(treeViewer
						.getControl().getShell());
				dialog.setInput(rosterAccounts);
				if (Window.OK == dialog.open()) {
					IPresenceContainerAdapter ipca = dialog.getSelection();
					IRosterSubscriptionSender sender = ipca.getRosterManager()
							.getRosterSubscriptionSender();
					try {
						sender.sendRosterAdd(dialog.getAccountID(), dialog
								.getAlias(), null);
					} catch (ECFException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		treeViewer = null;
		for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
			MultiRosterAccount account = (MultiRosterAccount) i.next();
			account.getRosterManager().removeRosterSubscriptionListener(
					subscriptionListener);
		}
		for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
			MultiRosterAccount account = (MultiRosterAccount) i.next();
			account.getRosterManager().removePresenceListener(presenceListener);
		}
		rosterAccounts.clear();
		super.dispose();
	}

	protected boolean addRosterAccount(MultiRosterAccount account) {
		return account != null && rosterAccounts.add(account);
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
		} else {
			MultiRosterAccount account = new MultiRosterAccount(this,
					container, containerAdapter);
			if (!addRosterAccount(account)) {
				return false;
			}

			IRosterManager manager = containerAdapter.getRosterManager();
			try {
				if (setAvailableAction.isChecked()
						|| setOfflineAction.isChecked()) {
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
			containerAdapter.getRosterManager().addPresenceListener(
					presenceListener);
			setStatusMenu.setVisible(true);
			getViewSite().getActionBars().getMenuManager().update(true);
			treeViewer.add(treeViewer.getInput(), account.getRoster());
			return true;
		}
	}

	private class RosterSubscriptionListener implements
			IRosterSubscriptionListener {

		public void handleSubscribeRequest(ID fromID) {
			// TODO Auto-generated method stub
		}

		public void handleSubscribed(ID fromID) {
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

	private class PresenceListener implements IPresenceListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.presence.IPresenceListener#handleRosterEntryAdd(org.eclipse.ecf.presence.roster.IRosterEntry)
		 */
		public void handleRosterEntryAdd(final IRosterEntry entry) {
			treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					treeViewer.add(entry.getParent(), entry);
				}
			});
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.presence.IPresenceListener#handleRosterEntryRemove(org.eclipse.ecf.presence.roster.IRosterEntry)
		 */
		public void handleRosterEntryRemove(IRosterEntry entry) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.presence.IPresenceListener#handleRosterEntryUpdate(org.eclipse.ecf.presence.roster.IRosterEntry)
		 */
		public void handleRosterEntryUpdate(IRosterEntry entry) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.presence.IParticipantListener#handlePresence(org.eclipse.ecf.core.identity.ID,
		 *      org.eclipse.ecf.presence.IPresence)
		 */
		public void handlePresence(ID fromID, IPresence presence) {
			// TODO Auto-generated method stub

		}

	}

	private class ViewerToolTip extends ToolTip {

		public static final String HEADER_BG_COLOR = Activator.PLUGIN_ID
				+ ".TOOLTIP_HEAD_BG_COLOR"; //$NON-NLS-1$

		public static final String HEADER_FONT = Activator.PLUGIN_ID
				+ ".TOOLTIP_HEAD_FONT"; //$NON-NLS-1$

		public ViewerToolTip(Control control) {
			super(control);
		}

		protected Composite createToolTipContentArea(Event event,
				Composite parent) {
			TreeItem item = treeViewer.getTree().getItem(
					new Point(event.x, event.y));
			IRosterEntry entry = (IRosterEntry) item.getData();

			GridLayout gl = new GridLayout();
			gl.marginBottom = 0;
			gl.marginTop = 0;
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.verticalSpacing = 1;
			parent.setLayout(gl);

			Composite topArea = new Composite(parent, SWT.NONE);
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
			data.widthHint = 200;
			topArea.setLayoutData(data);
			topArea.setBackground(JFaceResources.getColorRegistry().get(
					HEADER_BG_COLOR));

			gl = new GridLayout();
			gl.marginBottom = 2;
			gl.marginTop = 2;
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			gl.marginLeft = 5;
			gl.marginRight = 2;

			topArea.setLayout(gl);

			Label l = new Label(topArea, SWT.NONE);
			l.setText(entry.getName());
			l.setBackground(JFaceResources.getColorRegistry().get(
					HEADER_BG_COLOR));
			l.setFont(JFaceResources.getFontRegistry().get(HEADER_FONT));
			l.setLayoutData(new GridData(GridData.FILL_BOTH));

			createContentArea(parent, entry).setLayoutData(
					new GridData(GridData.FILL_BOTH));

			return parent;
		}

		protected Composite createContentArea(Composite parent,
				IRosterEntry entry) {
			Composite comp = new Composite(parent, SWT.NONE);
			comp.setBackground(parent.getDisplay().getSystemColor(
					SWT.COLOR_INFO_BACKGROUND));
			comp.setLayout(new FillLayout());
			Label label = new Label(comp, SWT.NONE);
			label.setText(getRosterEntryChildrenFromPresence(entry));
			return comp;
		}

		protected boolean shouldCreateToolTip(Event e) {
			if (super.shouldCreateToolTip(e)) {
				TreeItem item = treeViewer.getTree().getItem(
						new Point(e.x, e.y));
				return item != null && item.getData() instanceof IRosterEntry;
			} else {
				return false;
			}
		}
	}
}
