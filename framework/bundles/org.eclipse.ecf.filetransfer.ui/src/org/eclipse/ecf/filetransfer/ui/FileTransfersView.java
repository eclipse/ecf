/****************************************************************************
 * Copyright (c) 2007 Remy Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.filetransfer.ui;

import java.util.Iterator;
import java.util.Vector;
import org.eclipse.ecf.filetransfer.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

public class FileTransfersView extends ViewPart {

	public static final Vector transfers = new Vector();

	public static final String ID = "org.eclipse.ecf.filetransfer.ui.FileTransfersView"; //$NON-NLS-1$

	private static final String[] COLUMNS = {"Name", "Download", "Upload", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"Done"}; //$NON-NLS-1$

	private static final int[] WIDTHS = {250, 100, 100, 40};

	static final Object[] EMPTY_ARRAY = new Object[0];

	private static final double GIGABYTE = Math.pow(2, 30);

	private static final double MEGABYTE = Math.pow(2, 20);

	private static final double KILOBYTE = Math.pow(2, 10);

	private static final int NAME = 0;

	private static final int DOWNLOADED = NAME + 1;

	private static final int UPLOADED = DOWNLOADED + 1;

	private static final int DONE = UPLOADED + 1;

	public static FileTransfersView addTransfer(IFileTransfer transfer) {
		transfers.add(transfer);
		if (instance != null) {
			instance.add(transfer);
		}
		return instance;
	}

	TableViewer viewer;

	private Table table;

	private Action resumeAction;

	private Action pauseAction;

	private Action removeAction;

	static String getTwoDigitNumber(long value) {
		if (value > GIGABYTE) {
			double num = value / GIGABYTE;
			return Double.toString(Math.floor(num * 100) / 100) + " GB"; //$NON-NLS-1$
		} else if (value > MEGABYTE) {
			double num = value / MEGABYTE;
			return Double.toString(Math.floor(num * 100) / 100) + " MB"; //$NON-NLS-1$
		} else if (value > KILOBYTE) {
			double num = value / KILOBYTE;
			return Double.toString(Math.floor(num * 100) / 100) + " KB"; //$NON-NLS-1$
		}
		return value + " bytes"; //$NON-NLS-1$
	}

	private static FileTransfersView instance;

	public FileTransfersView() {
		instance = this;
	}

	public void dispose() {
		instance = null;
		super.dispose();
	}

	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ViewerSorter());
		viewer.setInput(getViewSite());
		table = viewer.getTable();

		for (int i = 0; i < WIDTHS.length; i++) {
			TableColumn col = new TableColumn(table, SWT.LEFT);
			col.setText(COLUMNS[i]);
			col.setWidth(WIDTHS[i]);
		}

		Iterator iterator = transfers.iterator();
		while (iterator.hasNext()) {
			add((IFileTransfer) iterator.next());
		}

		makeActions();
		hookContextMenu();

		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setSize(1000, 1000);
	}

	private void add(IFileTransfer transfer) {
		if (table != null && !table.isDisposed()) {
			viewer.add(transfer);
		}
	}

	public void update(IFileTransfer transfer) {
		if (table != null && !table.isDisposed()) {
			viewer.update(transfer, COLUMNS);
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
				enableActions();
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		table.setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	void fillContextMenu(IMenuManager manager) {
		manager.add(resumeAction);
		manager.add(pauseAction);
		manager.add(removeAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void makeActions() {
		resumeAction = new Action() {
			public void run() {
				ISelection sel = viewer.getSelection();
				if (!(sel instanceof IStructuredSelection)) {
					return;
				}
				IStructuredSelection ssel = (IStructuredSelection) sel;
				Iterator iterator = ssel.iterator();
				while (iterator.hasNext()) {
					IFileTransfer transfer = (IFileTransfer) iterator.next();
					IFileTransferPausable pausable = (IFileTransferPausable) transfer.getAdapter(IFileTransferPausable.class);
					if (pausable != null) {
						pausable.resume();
					}
				}
			}
		};
		resumeAction.setId("resume"); //$NON-NLS-1$
		resumeAction.setText("&Resume"); //$NON-NLS-1$

		pauseAction = new Action() {
			public void run() {
				ISelection sel = viewer.getSelection();
				if (!(sel instanceof IStructuredSelection)) {
					return;
				}
				IStructuredSelection ssel = (IStructuredSelection) sel;
				Iterator iterator = ssel.iterator();
				while (iterator.hasNext()) {
					IFileTransfer transfer = (IFileTransfer) iterator.next();
					IFileTransferPausable pausable = (IFileTransferPausable) transfer.getAdapter(IFileTransferPausable.class);
					if (pausable != null) {
						pausable.pause();
					}
				}
			}
		};
		pauseAction.setText("&Pause"); //$NON-NLS-1$

		removeAction = new Action() {
			public void run() {
				ISelection sel = viewer.getSelection();
				if (!(sel instanceof IStructuredSelection)) {
					return;
				}
				IStructuredSelection ssel = (IStructuredSelection) sel;
				Iterator iterator = ssel.iterator();
				while (iterator.hasNext()) {
					IFileTransfer transfer = (IFileTransfer) iterator.next();
					transfer.cancel();
					viewer.remove(transfer);
				}
			}
		};
		removeAction.setText("&Remove"); //$NON-NLS-1$
		removeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
	}

	void enableActions() {
		ISelection is = viewer.getSelection();
		resumeAction.setEnabled(false);
		pauseAction.setEnabled(false);
		if (is instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) is;
			removeAction.setEnabled(!iss.isEmpty());
			Iterator iterator = iss.iterator();
			while (iterator.hasNext()) {
				IFileTransfer transfer = (IFileTransfer) iterator.next();
				IFileTransferPausable pausable = (IFileTransferPausable) transfer.getAdapter(IFileTransferPausable.class);
				if (pausable != null) {
					resumeAction.setEnabled(true);
					pauseAction.setEnabled(true);
					return;
				}
			}
		}
	}

	public void setFocus() {
		table.setFocus();
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

		public String getColumnText(Object obj, int index) {
			if (!(obj instanceof IFileTransfer)) {
				return getText(obj);
			}
			IFileTransfer transfer = (IFileTransfer) obj;
			switch (index) {
				case NAME :
					return transfer.getID().getName();
				case DOWNLOADED :
					if (transfer instanceof IIncomingFileTransfer)
						return getTwoDigitNumber(((IIncomingFileTransfer) transfer).getBytesReceived());
					return "N/A"; //$NON-NLS-1$
				case UPLOADED :
					if (transfer instanceof IOutgoingFileTransfer)
						return getTwoDigitNumber(((IOutgoingFileTransfer) transfer).getBytesSent());
					return "N/A"; //$NON-NLS-1$
				case DONE :
					double percentComplete = transfer.getPercentComplete();
					return (percentComplete == 1.0) ? "yes" : Double.toString(percentComplete + '%'); //$NON-NLS-1$
			}
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			// nothing to do in this case
		}

		public void dispose() {
			// nothing to do in this case
		}

		public Object[] getElements(Object inputElement) {
			return EMPTY_ARRAY;
		}
	}
}