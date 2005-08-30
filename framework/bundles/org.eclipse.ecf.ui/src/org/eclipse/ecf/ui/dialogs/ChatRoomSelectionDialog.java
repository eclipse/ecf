package org.eclipse.ecf.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.presence.chat.IChatRoomManager;
import org.eclipse.ecf.presence.chat.IRoomInfo;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
	IChatRoomManager manager = null;

	public ChatRoomSelectionDialog(Shell parentShell, IChatRoomManager manager) {
		super(parentShell);
		this.manager = manager;
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
		tc.setText("Name");
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
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (!event.getSelection().isEmpty()) {
					ChatRoomSelectionDialog.this.getButton(Dialog.OK).setEnabled(true);
				}
			}
			
		});
		
		viewer.setContentProvider(new ChatRoomContentProvider());
		viewer.setLabelProvider(new ChatRoomLabelProvider());
		
		ID[] rooms = manager.getChatRooms();

		if (rooms != null && rooms.length > 0) {
			List roomInfo = new ArrayList();

			for (int i = 0; i < rooms.length; ++i) {
				roomInfo.add(manager.getChatRoomInfo(rooms[i]));
			}

			viewer.setInput(roomInfo.toArray(new IRoomInfo[rooms.length]));
		}

		this.setTitle("Chatroom Selection");
		this.setMessage("Select a chatroom to enter.");

		return parent;
	}

	private class ChatRoomContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {

			return (IRoomInfo[]) inputElement;
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
			IRoomInfo info = (IRoomInfo) element;

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
}
