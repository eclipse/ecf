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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.presence.ui.Activator;
import org.eclipse.ecf.internal.presence.ui.Messages;
import org.eclipse.ecf.internal.presence.ui.dialogs.AddContactDialog;
import org.eclipse.ecf.internal.presence.ui.dialogs.ChangePasswordDialog;
import org.eclipse.ecf.internal.presence.ui.dialogs.ChatRoomSelectionDialog;
import org.eclipse.ecf.presence.IAccountManager;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.IPresenceListener;
import org.eclipse.ecf.presence.Presence;
import org.eclipse.ecf.presence.chatroom.IChatRoomInfo;
import org.eclipse.ecf.presence.im.IChatManager;
import org.eclipse.ecf.presence.im.IChatMessageSender;
import org.eclipse.ecf.presence.im.ITypingMessageSender;
import org.eclipse.ecf.presence.roster.IRoster;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.roster.IRosterGroup;
import org.eclipse.ecf.presence.roster.IRosterItem;
import org.eclipse.ecf.presence.roster.IRosterManager;
import org.eclipse.ecf.presence.roster.IRosterSubscriptionSender;
import org.eclipse.ecf.presence.service.IPresenceService;
import org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerView;
import org.eclipse.ecf.presence.ui.chatroom.IChatRoomCommandListener;
import org.eclipse.ecf.presence.ui.chatroom.IChatRoomViewCloseListener;
import org.eclipse.ecf.presence.ui.dnd.IRosterViewerDropTarget;
import org.eclipse.ecf.ui.SharedImages;
import org.eclipse.ecf.ui.dialogs.ContainerConnectErrorDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.PluginTransfer;
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

	private static final String ROSTER_VIEWER_DROP_TARGET_EPOINT = Activator.PLUGIN_ID
			+ "." //$NON-NLS-1$
			+ "rosterViewerDropTarget"; //$NON-NLS-1$

	private static final String ROSTER_VIEWER_DROP_TARGET_CLASS_ATTR = "class"; //$NON-NLS-1$

	private static final Transfer[] dndTransferTypes = {
			EditorInputTransfer.getInstance(), FileTransfer.getInstance(),
			HTMLTransfer.getInstance(), LocalSelectionTransfer.getTransfer(),
			PluginTransfer.getInstance(), RTFTransfer.getInstance(),
			TextTransfer.getInstance() };

	private static final int dndOperations = DND.DROP_COPY | DND.DROP_MOVE
			| DND.DROP_LINK;

	protected TreeViewer treeViewer;

	protected List rosterAccounts = new ArrayList();

	private Hashtable chatRooms = new Hashtable();

	private IMenuManager setStatusMenu;

	private IAction imAction;

	private IAction removeAction;

	private IAction setAvailableAction;

	private IAction setAwayAction;

	private IAction setDNDAction;

	private IAction setInvisibleAction;

	private IAction setOfflineAction;

	private IAction showOfflineAction;

	private IAction showEmptyGroupsAction;

	private IAction addContactAction;

	private IAction openChatRoomAction;

	private IAction openAccountChatRoomAction;

	private IAction changePasswordAction;

	private IAction disconnectAllAccountsAction;

	private IAction disconnectAccountAction;

	// private IRosterSubscriptionListener subscriptionListener;

	private IPresenceListener presenceListener;

	private RosterViewerDropAdapter dropAdapter;

	private ViewerFilter hideOfflineFilter = new ViewerFilter() {
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (element instanceof IRosterEntry) {
				IRosterEntry entry = (IRosterEntry) element;
				IPresence presence = entry.getPresence();
				if (presence != null)
					return (presence.getType() != IPresence.Type.UNAVAILABLE);
				else
					return true;
			} else {
				return true;
			}
		}
	};

	private ViewerFilter hideEmptyGroupsFilter = new ViewerFilter() {
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (element instanceof IRosterGroup) {
				return !((IRosterGroup) element).getEntries().isEmpty();
			} else {
				return true;
			}
		}
	};

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
		presenceListener = new PresenceListener();
		treeViewer.setContentProvider(new MultiRosterContentProvider());
		treeViewer.setLabelProvider(new MultiRosterLabelProvider());
		treeViewer.addFilter(hideOfflineFilter);
		treeViewer.addFilter(hideEmptyGroupsFilter);
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
		toolTip.setHideOnMouseDown(false);
		toolTip.setPopupDelay(200);

		makeActions();
		hookContextMenu();
		contributeToActionBars();
		retrieveServices();
		hookDropSupport();
		treeViewer.expandToLevel(DEFAULT_EXPAND_LEVEL);

	}

	class RosterViewerDropAdapter extends ViewerDropAdapter {
		private List rosterDropTargets = new ArrayList();
		private List rosterPerformDrop = new ArrayList();

		public RosterViewerDropAdapter(TreeViewer treeViewer,
				IRosterViewerDropTarget dropTarget) {
			super(treeViewer);
			Assert.isNotNull(dropTarget);
			setFeedbackEnabled(false);
			addRosterDropTarget(dropTarget);
		}

		public boolean addRosterDropTarget(
				IRosterViewerDropTarget rosterDropTarget) {
			return rosterDropTargets.add(rosterDropTarget);
		}

		public boolean performDrop(Object data) {
			boolean result = false;
			for (Iterator i = rosterPerformDrop.iterator(); i.hasNext();) {
				IRosterViewerDropTarget rdt = (IRosterViewerDropTarget) i
						.next();
				if (rdt.performDrop(data))
					result = true;
			}
			rosterPerformDrop.clear();
			return result;
		}

		public void dispose() {
			rosterDropTargets.clear();
			rosterPerformDrop.clear();
		}

		public boolean validateDrop(Object target, int operation,
				TransferData transferType) {
			if (target != null && target instanceof IRosterItem) {
				rosterPerformDrop.clear();
				boolean result = false;
				for (Iterator i = rosterDropTargets.iterator(); i.hasNext();) {
					IRosterViewerDropTarget rdt = (IRosterViewerDropTarget) i
							.next();
					if (rdt.validateDrop((IRosterItem) target, operation,
							transferType)) {
						result = true;
						rosterPerformDrop.add(rdt);
					}
				}
				return result;
			} else
				return false;
		}
	}

	private void hookDropSupport() {
		IExtensionRegistry reg = Activator.getDefault().getExtensionRegistry();
		if (reg != null) {
			IExtensionPoint extensionPoint = reg
					.getExtensionPoint(ROSTER_VIEWER_DROP_TARGET_EPOINT);
			if (extensionPoint == null) {
				return;
			}
			IConfigurationElement[] configurationElements = extensionPoint
					.getConfigurationElements();

			for (int i = 0; i < configurationElements.length; i++) {
				try {
					IRosterViewerDropTarget rosterDropTarget = (IRosterViewerDropTarget) configurationElements[i]
							.createExecutableExtension(ROSTER_VIEWER_DROP_TARGET_CLASS_ATTR);
					if (dropAdapter == null) {
						dropAdapter = new RosterViewerDropAdapter(treeViewer,
								rosterDropTarget);
						treeViewer.addDropSupport(dndOperations,
								dndTransferTypes, dropAdapter);
					} else
						dropAdapter.addRosterDropTarget(rosterDropTarget);
				} catch (CoreException e) {
					// Log
					Activator
							.getDefault()
							.getLog()
							.log(
									new Status(
											IStatus.ERROR,
											Activator.PLUGIN_ID,
											IStatus.ERROR,
											Messages.MultiRosterView_ROSTER_VIEW_EXT_POINT_ERROR_MESSAGE,
											e));
				}
			}
		}
	}

	private void retrieveServices() {
		IPresenceService[] services = Activator.getDefault()
				.getPresenceServices();
		for (int i = 0; i < services.length; i++) {
			IContainer container = (IContainer) services[i]
					.getAdapter(IContainer.class);
			if (container != null && container.getConnectedID() != null) {
				addContainer(container);
			}
		}
	}

	protected IChatRoomCommandListener createChatRoomCommandListener() {
		return null;
	}

	protected IChatRoomViewCloseListener createChatRoomViewCloseListener(
			final ID connectedID) {
		return new IChatRoomViewCloseListener() {
			public void chatRoomViewClosing() {
				chatRooms.remove(connectedID);
			}
		};
	}

	/**
	 * For the given container, join the chat room specified by roomInfo. NOTE:
	 * this is to be considered provisional 'Gunner' API and may not be
	 * available in subsequent versions of this class.
	 * 
	 * @param container
	 *            the IContainer instance that exposes the chat room. Must not
	 *            be <code>null</code>. Also must be the same container
	 *            associated with one of the accounts managed by this
	 *            MultiRosterView.
	 * @param roomInfo
	 *            chat room information that will be used to join. Must not be
	 *            <code>null</code>.
	 * @param password
	 *            a password associated with chat room access. May be
	 *            <code>null</code>.
	 * @throws ECFException
	 *             if the given container is not connected, or if the given
	 *             container is not managed by this MultiRosterView, or if
	 *             {@link ChatRoomManagerView} cannot be initialized.
	 */
	protected void joinChatRoom(IContainer container, IChatRoomInfo roomInfo,
			String password) throws ECFException {
		Assert.isNotNull(container);
		Assert.isNotNull(roomInfo);
		// Check to make sure given container is connected.
		ID connectedID = container.getConnectedID();
		if (connectedID == null)
			throw new ECFException(
					Messages.MultiRosterView_EXCEPTION_JOIN_ROOM_NOT_CONNECTED);
		// Check to make sure that the given container is one that we have in
		// our accounts set
		if (findAccountForContainer(container) == null)
			throw new ECFException(
					Messages.MultiRosterView_EXCEPTION_JOIN_ROOM_INVALID_ACCOUNT);

		IWorkbenchWindow ww = getViewSite().getPage().getWorkbenchWindow();
		IWorkbenchPage wp = ww.getActivePage();
		// Get existing roomView...if it's there
		RoomWithAView roomView = (RoomWithAView) chatRooms.get(connectedID);
		if (roomView != null) {
			// We've already connected to this room, so just show it.
			ChatRoomManagerView chatroommanagerview = roomView.getView();
			wp.activate(chatroommanagerview);
			chatroommanagerview.joinRoom(roomInfo, password);
			return;
		} else {
			try {
				IViewReference ref = wp
						.findViewReference(
								org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerView.VIEW_ID,
								connectedID.getName());
				// Open view for given connectedID (secondaryID)
				final ChatRoomManagerView chatroommanagerview = (ChatRoomManagerView) ((ref == null) ? wp
						.showView(
								org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerView.VIEW_ID,
								connectedID.getName(),
								IWorkbenchPage.VIEW_ACTIVATE)
						: ref.getView(true));
				// initialize new view
				chatroommanagerview.initializeWithoutManager(
						ChatRoomManagerView.getUsernameFromID(connectedID),
						ChatRoomManagerView.getHostnameFromID(connectedID),
						createChatRoomCommandListener(),
						createChatRoomViewCloseListener(connectedID));
				// join room
				chatroommanagerview.joinRoom(roomInfo, password);

				roomView = new RoomWithAView(chatroommanagerview, connectedID);
				chatRooms.put(roomView.getID(), roomView);
			} catch (Exception e1) {
				throw new ECFException(e1);
			}
		}

	}

	private void selectAndJoinChatRoomForAccounts(MultiRosterAccount[] accounts) {
		// Create chat room selection dialog with managers, open
		ChatRoomSelectionDialog dialog = new ChatRoomSelectionDialog(
				getViewSite().getShell(), accounts);
		dialog.open();
		// If selection cancelled then simply return
		if (dialog.getReturnCode() != Window.OK)
			return;
		// Get selected room, selected manager, and selected IChatRoomInfo
		IChatRoomInfo selectedInfo = dialog.getSelectedRoom().getRoomInfo();
		MultiRosterAccount account = dialog.getSelectedRoom().getAccount();
		// Now get the secondary ID from the selected room id
		final IContainer container = account.getContainer();
		final ID connectedID = container.getConnectedID();
		if (connectedID == null) {
			MessageDialog
					.openError(
							getViewSite().getShell(),
							Messages.MultiRosterView_NO_IDENTIFIER_FOR_ROOM_TITLE,
							NLS
									.bind(
											Messages.MultiRosterView_NO_IDENTIFIER_FOR_ROOM_MESSAGE,
											selectedInfo.getRoomID()));
			return;
		}

		try {
			joinChatRoom(container, selectedInfo, null);
		} catch (ECFException e) {
			Throwable e1 = e.getStatus().getException();
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							IStatus.ERROR,
							Messages.MultiRosterView_EXCEPTION_LOG_JOIN_ROOM,
							e1));
			ContainerConnectErrorDialog ed = new ContainerConnectErrorDialog(
					getViewSite().getShell(), selectedInfo.getRoomID()
							.getName(), e1);
			ed.open();
		}
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

		showOfflineAction = new Action(Messages.MultiRosterView_ShowOffline,
				Action.AS_CHECK_BOX) {
			public void run() {
				if (isChecked()) {
					treeViewer.removeFilter(hideOfflineFilter);
				} else {
					treeViewer.addFilter(hideOfflineFilter);
				}
			}
		};

		showEmptyGroupsAction = new Action(
				Messages.MultiRosterView_ShowEmptyGroups, Action.AS_CHECK_BOX) {
			public void run() {
				if (isChecked()) {
					treeViewer.removeFilter(hideEmptyGroupsFilter);
				} else {
					treeViewer.addFilter(hideEmptyGroupsFilter);
				}
			}
		};

		addContactAction = new Action(Messages.MultiRosterView_AddContact,
				SharedImages.getImageDescriptor(SharedImages.IMG_ADD_BUDDY)) {
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
						Activator.getDefault().getLog().log(e.getStatus());
					}
				}
			}
		};

		openChatRoomAction = new Action() {
			public void run() {
				selectAndJoinChatRoomForAccounts((MultiRosterAccount[]) rosterAccounts
						.toArray(new MultiRosterAccount[] {}));
			}
		};
		openChatRoomAction
				.setText(Messages.MultiRosterView_ENTER_CHATROOM_ACTION_TEXT);
		openChatRoomAction
				.setToolTipText(Messages.MultiRosterView_ENTER_CHATROOM_TOOLTIP_TEXT);
		openChatRoomAction.setImageDescriptor(SharedImages
				.getImageDescriptor(SharedImages.IMG_ADD_CHAT));
		openChatRoomAction.setEnabled(true);

		openAccountChatRoomAction = new Action() {
			public void run() {
				IStructuredSelection iss = (IStructuredSelection) treeViewer
						.getSelection();
				IRoster roster = (IRoster) iss.getFirstElement();
				MultiRosterAccount account = findAccountForUser(roster
						.getUser().getID());
				if (account != null)
					selectAndJoinChatRoomForAccounts(new MultiRosterAccount[] { account });
			}
		};
		openAccountChatRoomAction
				.setText(Messages.MultiRosterView_SHOW_CHAT_ROOMS_FOR_ACCOUNT_ACTION_TEXT);
		openAccountChatRoomAction.setEnabled(true);
		openAccountChatRoomAction.setImageDescriptor(SharedImages
				.getImageDescriptor(SharedImages.IMG_ADD_CHAT));

		changePasswordAction = new Action() {
			public void run() {
				IStructuredSelection iss = (IStructuredSelection) treeViewer
						.getSelection();
				IRoster roster = (IRoster) iss.getFirstElement();
				MultiRosterAccount account = findAccountForUser(roster
						.getUser().getID());
				if (account != null)
					changePasswordForAccount(account);
			}
		};

		changePasswordAction
				.setText(Messages.MultiRosterView_CHANGE_PASSWORD_MENU);
		changePasswordAction.setEnabled(true);

		disconnectAllAccountsAction = new Action() {
			public void run() {
				if (MessageDialog
						.openQuestion(
								getViewSite().getShell(),
								Messages.MultiRosterView_DISCONNECT_QUESTION_TITLE,
								Messages.MultiRosterView_DISCONNECT_ALL_ACCOUNTS_QUESTION_MESSAGE)) {
					disconnectAccounts((MultiRosterAccount[]) rosterAccounts
							.toArray(new MultiRosterAccount[] {}));
				}
			}
		};
		disconnectAllAccountsAction
				.setText(Messages.MultiRosterView_DISCONNECT_ALL_ACCOUNTS_ACTION_TEXT);
		disconnectAllAccountsAction.setEnabled(true);
		disconnectAllAccountsAction.setImageDescriptor(PlatformUI
				.getWorkbench().getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));

		disconnectAccountAction = new Action() {
			public void run() {
				IStructuredSelection iss = (IStructuredSelection) treeViewer
						.getSelection();
				IRoster roster = (IRoster) iss.getFirstElement();
				MultiRosterAccount account = findAccountForUser(roster
						.getUser().getID());
				ID connectedID = account.getContainer().getConnectedID();
				if (account != null
						&& connectedID != null
						&& MessageDialog
								.openQuestion(
										getViewSite().getShell(),
										Messages.MultiRosterView_DISCONNECT_QUESTION_TITLE,
										NLS
												.bind(
														Messages.MultiRosterView_DISCONNECT_ACCOUNT_QUESTION_MESSAGE,
														connectedID.getName()))) {
					disconnectAccounts(new MultiRosterAccount[] { account });
				}
			}
		};
		disconnectAccountAction
				.setText(Messages.MultiRosterView_DISCONNECT_ACCOUNT_ACTION_TEXT);
		disconnectAccountAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));

	}

	protected void changePasswordForAccount(MultiRosterAccount account) {
		ChangePasswordDialog cpd = new ChangePasswordDialog(getViewSite()
				.getShell(), account.getRoster().getUser().getID().getName());
		cpd.open();
		if (cpd.getResult() == Window.OK) {
			IPresenceContainerAdapter pc = account
					.getPresenceContainerAdapter();
			IAccountManager am = pc.getAccountManager();
			try {
				if (am.changePassword(cpd.getNewPassword()))
					MessageDialog
							.openInformation(
									getViewSite().getShell(),
									Messages.MultiRosterView_PASSWORD_CHANGED_DIALOG_TITLE,
									Messages.MultiRosterView_PASSWORD_CHANGED_MESSAGE);
				else
					MessageDialog
							.openInformation(
									getViewSite().getShell(),
									Messages.MultiRosterView_PASSWORD_NOT_CHANGED_TITLE,
									Messages.MultiRosterView_PASSWORD_NOT_CHANGED_MESSAGE);
			} catch (ECFException e) {
				MessageDialog.openError(getViewSite().getShell(),
						Messages.MultiRosterView_PASSWORD_NOT_CHANGED_TITLE,
						Messages.MultiRosterView_PASSWORD_CHANGE_ERROR);
				Activator.getDefault().getLog().log(e.getStatus());
			}
		}
	}

	protected void disconnectAccounts(MultiRosterAccount[] accounts) {
		for (int i = 0; i < accounts.length; i++)
			accounts[i].getContainer().disconnect();
	}

	private MultiRosterAccount findAccountForUser(ID userID) {
		for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
			MultiRosterAccount account = (MultiRosterAccount) i.next();
			if (account.getRoster().getUser().getID().equals(userID))
				return account;
		}
		return null;
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
			Activator.getDefault().getLog().log(e.getStatus());
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
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(removeAction);
		} else if (element instanceof IRoster) {
			manager.add(changePasswordAction);
			manager.add(new Separator());
			manager.add(addContactAction);
			manager.add(new Separator());
			manager.add(openAccountChatRoomAction);
			manager.add(new Separator());
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(disconnectAccountAction);
		} else {
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		}
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

	private MultiRosterAccount findAccountForContainer(IContainer container) {
		if (container == null)
			return null;
		synchronized (rosterAccounts) {
			for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
				MultiRosterAccount account = (MultiRosterAccount) i.next();
				if (account.getContainer().getID().equals(container.getID()))
					return account;
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
			MessageDialog
					.openError(
							getViewSite().getShell(),
							Messages.MultiRosterView_ERROR_CONTACT_REMOVE_TITLE,
							NLS
									.bind(
											Messages.MultiRosterView_ERROR_CONTACT_REMOVED_MESSAGE,
											entry.getUser().getID().getName()));
			Activator.getDefault().getLog().log(e.getStatus()); //$NON-NLS-1$
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

	private void setLocalPullDownEnabled(boolean enabled) {
		setAvailableAction.setEnabled(enabled);
		setAwayAction.setEnabled(enabled);
		setDNDAction.setEnabled(enabled);
		setInvisibleAction.setEnabled(enabled);
		setOfflineAction.setEnabled(enabled);
		showOfflineAction.setEnabled(enabled);
		showEmptyGroupsAction.setEnabled(enabled);
		addContactAction.setEnabled(enabled);
		openChatRoomAction.setEnabled(enabled);
		disconnectAllAccountsAction.setEnabled(enabled);
	}

	private void fillLocalPullDown(IMenuManager manager) {
		setStatusMenu = new MenuManager(Messages.MultiRosterView_SetStatusAs,
				null);
		setStatusMenu.add(setAvailableAction);
		setStatusMenu.add(setAwayAction);
		setStatusMenu.add(setDNDAction);
		setStatusMenu.add(setInvisibleAction);
		setStatusMenu.add(setOfflineAction);
		manager.add(setStatusMenu);
		manager.add(new Separator());

		manager.add(showOfflineAction);

		manager.add(showEmptyGroupsAction);

		manager.add(new Separator());
		manager.add(addContactAction);
		manager.add(new Separator());
		manager.add(openChatRoomAction);
		manager.add(new Separator());
		manager.add(disconnectAllAccountsAction);
		setLocalPullDownEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		if (dropAdapter != null) {
			dropAdapter.dispose();
			dropAdapter = null;
		}
		treeViewer = null;
		for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
			MultiRosterAccount account = (MultiRosterAccount) i.next();
			account.getRosterManager().removePresenceListener(presenceListener);
		}
		rosterAccounts.clear();
		super.dispose();
	}

	protected boolean addRosterAccount(MultiRosterAccount account) {
		boolean result = account != null && rosterAccounts.add(account);
		if (result)
			setLocalPullDownEnabled(true);
		return result;
	}

	protected void rosterAccountDisconnected(
			MultiRosterAccount disconnectedAccount) {
		// remove account. This will be changed to maintain the roster account
		// info even though disconnected...see bug
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=166670
		removeRosterAccount(disconnectedAccount);
	}

	protected void removeRosterAccount(MultiRosterAccount account) {
		// Remove presence listener
		account.getRosterManager().removePresenceListener(presenceListener);

		if (treeViewer != null)
			treeViewer.remove(account.getRoster());
		// Remove account
		rosterAccounts.remove(account);
		// Disable local pull down window if no more accounts
		setLocalPullDownEnabled(rosterAccounts.size() > 0);
		account.dispose();
		refreshTreeViewer(null, true);
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
			Control c = treeViewer.getControl();
			if (c != null && !c.isDisposed()) {
				if (val != null) {
					treeViewer.refresh(val, labels);
					ViewerFilter[] filters = treeViewer.getFilters();
					if (filters.length != 0) {
						treeViewer.refresh(labels);
					}
				} else {
					treeViewer.refresh(labels);
				}
				treeViewer.expandToLevel(DEFAULT_EXPAND_LEVEL);
			}
		}
	}

	protected void addEntryToTreeViewer(IRosterEntry entry) {
		if (treeViewer != null)
			treeViewer.add(entry.getParent(), entry);
	}

	protected void removeEntryFromTreeViewer(IRosterEntry entry) {
		if (treeViewer != null)
			treeViewer.remove(entry);
	}

	protected void removeEntryFromTreeViewer(ID entryID) {
		synchronized (rosterAccounts) {
			for (Iterator i = rosterAccounts.iterator(); i.hasNext();) {
				MultiRosterAccount account = (MultiRosterAccount) i.next();
				final IRosterEntry entry = find(account.getRoster().getItems(),
						entryID);
				if (entry != null)
					treeViewer.remove(entry);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.presence.ui.IMultiRosterViewPart#addContainer(org.eclipse.ecf.core.IContainer)
	 */
	public boolean addContainer(IContainer container) {
		if (container == null)
			return false;
		IPresenceContainerAdapter containerAdapter = (IPresenceContainerAdapter) container
				.getAdapter(IPresenceContainerAdapter.class);
		if (containerAdapter == null)
			return false;
		else {
			MultiRosterAccount account = new MultiRosterAccount(this,
					container, containerAdapter);
			if (!addRosterAccount(account))
				return false;

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
				Activator.getDefault().getLog().log(e.getStatus());
			}
			containerAdapter.getRosterManager().addPresenceListener(
					presenceListener);
			setStatusMenu.setVisible(true);
			getViewSite().getActionBars().getMenuManager().update(true);
			treeViewer.add(treeViewer.getInput(), account.getRoster());
			return true;
		}
	}

	private class PresenceListener implements IPresenceListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.presence.IParticipantListener#handlePresence(org.eclipse.ecf.core.identity.ID,
		 *      org.eclipse.ecf.presence.IPresence)
		 */
		public void handlePresence(ID fromID, IPresence presence) {
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

			GridData contentData = new GridData(SWT.FILL, SWT.FILL, true, true);
			contentData.heightHint = 96;
			createContentArea(parent, entry).setLayoutData(contentData);

			return parent;
		}

		protected Composite createContentArea(Composite parent,
				IRosterEntry entry) {
			byte[] data = entry.getPresence().getPictureData();
			Color background = parent.getDisplay().getSystemColor(
					SWT.COLOR_INFO_BACKGROUND);
			if (data.length == 0) {
				// if there is no picture data, we'll just draw our standard
				// tooltip
				Composite comp = new Composite(parent, SWT.NONE);
				comp.setLayout(new FillLayout());
				Label label = new Label(comp, SWT.NONE);
				label.setText(getRosterEntryChildrenFromPresence(entry));
				label.setBackground(background);
				return comp;
			} else {
				Composite comp = new Composite(parent, SWT.NONE);
				comp.setBackground(parent.getDisplay().getSystemColor(
						SWT.COLOR_INFO_BACKGROUND));

				Label label = new Label(comp, SWT.NONE);
				label.setText(getRosterEntryChildrenFromPresence(entry));
				label.setBackground(background);

				// create a canvas for drawing
				Canvas canvas = new Canvas(comp, SWT.NONE);
				canvas.setBackground(background);
				GridData canvasData = new GridData(SWT.FILL, SWT.FILL, true,
						true);
				// set a minimum width hint of 96 pixels
				canvasData.widthHint = 96;
				canvas.setLayoutData(canvasData);
				try {
					// create the image by reading in the bytes
					final Image image = new Image(canvas.getDisplay(),
							new ByteArrayInputStream(data));
					Rectangle bounds = image.getBounds();
					final int imageHeight = bounds.height;
					final int imageWidth = bounds.width;

					// use a PaintListener to draw the image
					canvas.addPaintListener(new PaintListener() {
						public void paintControl(PaintEvent e) {
							if (imageHeight > 96 && imageHeight > imageWidth) {
								// if the image's height is over 96 and it's
								// larger than the width, we need to scale it
								// down based on the ratio of the height against
								// 96
								double ratio = 96 / imageHeight;
								e.gc.drawImage(image, 0, 0, imageWidth,
										imageHeight,
										48 - ((int) (imageHeight * ratio) / 2),
										48 - ((int) (imageWidth * ratio) / 2),
										(int) (imageWidth * ratio),
										(int) (imageHeight * ratio));
							} else if (imageWidth > 96) {
								// if the image's width is over 96, we'll have
								// to scale the image down based on the ratio of
								// the width against 96
								double ratio = 96 / imageWidth;
								e.gc.drawImage(image, 0, 0, imageWidth,
										imageHeight,
										48 - ((int) (imageHeight * ratio) / 2),
										48 - ((int) (imageWidth * ratio) / 2),
										(int) (imageWidth * ratio),
										(int) (imageHeight * ratio));
							} else {
								// center the image and then draw it
								e.gc.drawImage(image, 0, 0, imageWidth,
										imageHeight, 48 - (imageWidth / 2),
										48 - (imageHeight / 2), imageWidth,
										imageHeight);
							}
						}
					});

					canvas.addDisposeListener(new DisposeListener() {
						public void widgetDisposed(DisposeEvent e) {
							image.dispose();
						}
					});

					label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
							true));

					GridLayout layout = new GridLayout(2, false);
					layout.marginHeight = 0;
					layout.marginWidth = 0;
					layout.horizontalSpacing = 0;
					layout.verticalSpacing = 0;
					comp.setLayout(layout);

					return comp;
				} catch (SWTException e) {
					// if the error wasn't caused by an unsupported format that
					// was thrown because we failed to construct the image, we
					// should propagate the exception upwards
					if (e.code != SWT.ERROR_UNSUPPORTED_FORMAT) {
						throw e;
					}
					canvas.dispose();
					comp.setLayout(new FillLayout());
					return comp;
				}
			}
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

	class RoomWithAView {
		ChatRoomManagerView view;

		ID secondaryID;

		RoomWithAView(ChatRoomManagerView view, ID secondaryID) {
			this.view = view;
			this.secondaryID = secondaryID;
		}

		public ChatRoomManagerView getView() {
			return view;
		}

		public ID getID() {
			return secondaryID;
		}
	}
}
