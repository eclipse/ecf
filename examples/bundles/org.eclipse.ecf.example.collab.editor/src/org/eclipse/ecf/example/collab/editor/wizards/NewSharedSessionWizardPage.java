/*******************************************************************************
 * Copyright (c) 2006 Ken Gilmer. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ken Gilmer - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.example.collab.editor.wizards;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.datashare.events.IChannelEvent;
import org.eclipse.ecf.datashare.events.IChannelMessageEvent;
import org.eclipse.ecf.example.collab.editor.Activator;
import org.eclipse.ecf.example.collab.editor.EditorListener;
import org.eclipse.ecf.example.collab.editor.message.SharedEditorSessionList;
import org.eclipse.ecf.example.collab.editor.message.SharedEditorSessionListRequest;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class NewSharedSessionWizardPage extends WizardPage {
	private Text containerText;

	// private Text fileText;

	private ISelection selection;

	private TableViewer sessionViewer;

	private List sessions;

	public NewSharedSessionWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("Shared Editor");
		setDescription("Connect to a live shared editor session.");
		this.selection = selection;
		sessions = new ArrayList();
	}

	public void createControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(main, SWT.NULL);
		label.setText("&Project:");

		Composite c = new Composite(main, SWT.None);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		containerText = new Text(c, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(c, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});

		Label label2 = new Label(main, SWT.NULL);
		label2.setText("&Shared File:");
		label2.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		sessionViewer = new TableViewer(main, SWT.BORDER);
		sessionViewer.setContentProvider(new ListContentProvider());
		sessionViewer.setLabelProvider(new SessionNameLabelProvider());
		sessionViewer.setInput(sessions);
		sessionViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		sessionViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				dialogChanged();
			}

		});

		initialize();
		dialogChanged();
		setControl(main);
	}

	private class ListContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {

			return ((List) inputElement).toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private class SessionNameLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			String s = (String) element;

			// Assumption that '_' is delimeter character seperating shared
			// group name from file name.
			return s.split("_")[1];
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

	private void initialize() {
		if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();

				containerText.setText(container.getFullPath().toString());
			}
		}

		try {
			IChannel channel = Activator.getDefault().intializePresenceSession(new SessionResponseListener());
			channel.sendMessage(createMessage(new SharedEditorSessionListRequest()));
		} catch (ECFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class SessionResponseListener implements IChannelListener {

		public void handleChannelEvent(IChannelEvent event) {
			if (event instanceof IChannelMessageEvent) {

				IChannelMessageEvent msg = (IChannelMessageEvent) event;

				ByteArrayInputStream bins = new ByteArrayInputStream(msg.getData());
				ObjectInputStream ois;
				try {
					ois = new ObjectInputStream(bins);
					Object o = ois.readObject();

					if (o instanceof SharedEditorSessionList) {
						SharedEditorSessionList l = (SharedEditorSessionList) o;

						sessions.addAll(l.getNames());
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

							public void run() {
								if (!sessionViewer.getTable().isDisposed()) {
									sessionViewer.refresh();
								}
							}

						});

					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private byte[] createMessage(Object obj) throws IOException, ECFException {
		ByteArrayOutputStream bouts = new ByteArrayOutputStream();

		ObjectOutputStream douts = new ObjectOutputStream(bouts);
		douts.writeObject(obj);

		return bouts.toByteArray();
	}

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(getContainerName()));
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (fileName == null || fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}

		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		if (!sessionViewer.getSelection().isEmpty()) {
			StructuredSelection s = (StructuredSelection) sessionViewer.getSelection();

			return ((String) s.getFirstElement()).split("_")[1];
		}

		return null;
	}

}