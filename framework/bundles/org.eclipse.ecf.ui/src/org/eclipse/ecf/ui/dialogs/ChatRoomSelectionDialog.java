package org.eclipse.ecf.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ecf.presence.chat.IChatRoomManager;
import org.eclipse.ecf.presence.chat.IRoomInfo;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ChatRoomSelectionDialog extends TitleAreaDialog {
	IChatRoomManager [] managers = null;
	
	private Room selectedRoom = null;

	public class Room {
		IRoomInfo info;
		IChatRoomManager manager;
		
		public Room(IRoomInfo info, IChatRoomManager man) {
			this.info = info;
			this.manager = man;
		}
		public IRoomInfo getRoomInfo() {
			return info;
		}
		public IChatRoomManager getManager() {
			return manager;
		}
	}
	public ChatRoomSelectionDialog(Shell parentShell, IChatRoomManager [] managers) {
		super(parentShell);
		this.managers = managers;
	}

	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout());
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		TableViewer viewer = new TableViewer(main, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = viewer.getTable();
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TableColumn tc = new TableColumn(table, SWT.NONE);
		tc.setText("Room Name");
		tc.pack();
		tc = new TableColumn(table, SWT.NONE);
		tc.setText("Subject");
		tc.pack();
		tc = new TableColumn(table, SWT.NONE);
		tc.setText("Description");
		tc.pack();
		tc = new TableColumn(table, SWT.NONE);
		tc.setText("Members");
		tc.pack();
		tc = new TableColumn(table, SWT.NONE);
		tc.setText("Moderated");
		tc.pack();
		tc = new TableColumn(table, SWT.NONE);
		tc.setText("Persistent");		
		tc.pack();
		tc = new TableColumn(table,  SWT.NONE);
		tc.setText("Account");
		tc.pack();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					ChatRoomSelectionDialog.this.getButton(Dialog.OK).setEnabled(true);
				}
			}
			
		});
		
		viewer.setContentProvider(new ChatRoomContentProvider());
		viewer.setLabelProvider(new ChatRoomLabelProvider());
		
		List all = new ArrayList();
		for(int i=0; i < managers.length; i++) {
			IRoomInfo [] infos = managers[i].getChatRoomsInfo();
			if (infos != null) {
				for(int j=0; j < infos.length; j++) {
					all.add(new Room(infos[j],managers[i]));
				}
			}
		}
		Room [] rooms = (Room []) all.toArray(new Room[] {});
		viewer.setInput(rooms);
		
		this.setTitle("Chatroom Selection");
		this.setMessage("Select a chatroom to enter");
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection s = (StructuredSelection) event.getSelection();
				if (s.getFirstElement() instanceof Room) {
					selectedRoom = (Room) s.getFirstElement();
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

		return parent;
	}

	private class ChatRoomContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {

			return (Room[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private class ChatRoomLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			Room room = (Room) element;

			IRoomInfo info = room.getRoomInfo();
			switch (columnIndex) {
			case 0:
				return info.getName();
			case 1:
				return info.getSubject();
			case 2:
				return info.getDescription();
			case 3:
				return String.valueOf(info.getParticipantsCount());
			case 4:
				return String.valueOf(info.isModerated());
			case 5:
				return String.valueOf(info.isPersistent());
			case 6:
				return info.getConnectedID().getName();
			default:
				return "";

			}

		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

	}

	protected Control createButtonBar(Composite parent) {

		Control bar = super.createButtonBar(parent);
		this.getButton(Dialog.OK).setText("Enter");
		this.getButton(Dialog.OK).setEnabled(false);
		return bar;
	}

	public Room getSelectedRoom() {
		return selectedRoom;
	}
}
