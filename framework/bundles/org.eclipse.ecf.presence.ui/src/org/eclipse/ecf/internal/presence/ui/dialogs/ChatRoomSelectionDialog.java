/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.presence.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ecf.internal.presence.ui.Messages;
import org.eclipse.ecf.presence.chatroom.IChatRoomInfo;
import org.eclipse.ecf.presence.chatroom.IChatRoomManager;
import org.eclipse.ecf.presence.ui.MultiRosterAccount;
import org.eclipse.ecf.ui.SharedImages;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ChatRoomSelectionDialog extends TitleAreaDialog {
	MultiRosterAccount[] accounts = null;

	private Room selectedRoom = null;

	public class Room {
		IChatRoomInfo info;

		MultiRosterAccount account;

		public Room(IChatRoomInfo info, MultiRosterAccount man) {
			this.info = info;
			this.account = man;
		}

		public IChatRoomInfo getRoomInfo() {
			return info;
		}

		public MultiRosterAccount getAccount() {
			return account;
		}
	}

	public ChatRoomSelectionDialog(Shell parentShell, MultiRosterAccount[] accounts) {
		super(parentShell);
		this.accounts = accounts;
		setTitleImage(SharedImages.getImage(SharedImages.IMG_CHAT_WIZARD));
	}

	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout());
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableViewer viewer = new TableViewer(main, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		Table table = viewer.getTable();

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TableColumn tc = new TableColumn(table, SWT.NONE);
		tc.setText(Messages.ChatRoomSelectionDialog_ROOM_NAME_COLUMN);
		tc.pack();
		int width = tc.getWidth();
		tc.setWidth(width + (width / 4));
		tc = new TableColumn(table, SWT.NONE);
		tc.setText(Messages.ChatRoomSelectionDialog_SUBJECT_COLUMN);
		tc.pack();
		width = tc.getWidth();
		tc.setWidth(width + (width / 4));
		tc = new TableColumn(table, SWT.NONE);
		tc.setText(Messages.ChatRoomSelectionDialog_DESCRIPTION_COLUMN);
		tc.pack();
		width = tc.getWidth();
		tc.setWidth(width + (width / 3));
		tc = new TableColumn(table, SWT.NONE);
		tc.setText(Messages.ChatRoomSelectionDialog_MEMBERS_COLUMN);
		tc.pack();
		tc = new TableColumn(table, SWT.NONE);
		tc.setText(Messages.ChatRoomSelectionDialog_MODERATED_COLUMN);
		tc.pack();
		tc = new TableColumn(table, SWT.NONE);
		tc.setText(Messages.ChatRoomSelectionDialog_PERSISTENT_COLUMN);
		tc.pack();
		tc = new TableColumn(table, SWT.NONE);
		tc.setText(Messages.ChatRoomSelectionDialog_ACCOUNT_COLUMN);
		tc.pack();
		width = tc.getWidth();
		tc.setWidth(width * 3);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					ChatRoomSelectionDialog.this.getButton(Window.OK).setEnabled(true);
				}
			}

		});

		viewer.setContentProvider(new ChatRoomContentProvider());
		viewer.setLabelProvider(new ChatRoomLabelProvider());

		List all = new ArrayList();
		for (int i = 0; i < accounts.length; i++) {
			IChatRoomManager chatRoomManager = accounts[i].getPresenceContainerAdapter().getChatRoomManager();
			if (chatRoomManager != null) {
				IChatRoomInfo[] infos = chatRoomManager.getChatRoomInfos();
				if (infos != null) {
					for (int j = 0; j < infos.length; j++) {
						if (infos[j] != null && accounts[i] != null) {
							all.add(new Room(infos[j], accounts[i]));
						}
					}
				}
			}
		}
		viewer.setInput(all.toArray());

		this.setTitle(Messages.ChatRoomSelectionDialog_TITLE);
		this.setMessage(Messages.ChatRoomSelectionDialog_MESSAGE);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				IStructuredSelection s = (IStructuredSelection) e.getSelection();
				Object o = s.getFirstElement();
				if (o instanceof Room) {
					selectedRoom = (Room) o;
				}
			}

		});

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				if (selectedRoom != null) {
					ChatRoomSelectionDialog.this.okPressed();
				}
			}

		});

		applyDialogFont(parent);
		return parent;
	}

	private class ChatRoomContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}

	}

	private class ChatRoomLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			Room room = (Room) element;

			IChatRoomInfo info = room.getRoomInfo();
			switch (columnIndex) {
				case 0 :
					return info.getName();
				case 1 :
					return info.getSubject();
				case 2 :
					return info.getDescription();
				case 3 :
					return String.valueOf(info.getParticipantsCount());
				case 4 :
					return String.valueOf(info.isModerated());
				case 5 :
					return String.valueOf(info.isPersistent());
				case 6 :
					return room.getAccount().getContainer().getConnectedID().getName();
				default :
					return ""; //$NON-NLS-1$

			}

		}

		public void addListener(ILabelProviderListener listener) {
			// do nothing
		}

		public void dispose() {
			// do nothing
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// do nothing
		}

	}

	protected Control createButtonBar(Composite parent) {
		Control bar = super.createButtonBar(parent);
		this.getButton(Window.OK).setText(Messages.ChatRoomSelectionDialog_ENTER_CHAT_BUTTON_TEXT);
		this.getButton(Window.OK).setEnabled(false);
		return bar;
	}

	public Room getSelectedRoom() {
		return selectedRoom;
	}
}
