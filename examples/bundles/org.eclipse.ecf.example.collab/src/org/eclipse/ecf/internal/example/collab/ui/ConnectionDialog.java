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
package org.eclipse.ecf.internal.example.collab.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.internal.example.collab.ClientPlugin;
import org.eclipse.ecf.internal.example.collab.ClientPluginConstants;
import org.eclipse.ecf.internal.example.collab.Messages;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class ConnectionDialog extends TitleAreaDialog {
	private static final int PROVIDER_TABLE_HEIGHT = 200;

	private static final int PROVIDER_TABLE_WIDTH = 150;

	protected static final String CLASSNAME = JoinGroupWizardPage.class
			.getName();

	protected static final String USER_NAME_SYSTEM_PROPERTY = "user.name"; //$NON-NLS-1$

	protected static final String ISSERVER_PROP_NAME = CLASSNAME + ".isServer"; //$NON-NLS-1$

	protected static final String DEFAULTGROUPID_PROP_NAME = CLASSNAME
			+ ".defaultgroupid"; //$NON-NLS-1$

	protected static final String EXAMPLEGROUPID_PROP_NAME = CLASSNAME
			+ ".examplegroupid"; //$NON-NLS-1$

	protected static final String USEPASSWORD_PROP_NAME = CLASSNAME
			+ ".usepassword"; //$NON-NLS-1$

	protected static final String USENICKNAME_PROP_NAME = CLASSNAME
			+ ".usenickname"; //$NON-NLS-1$

	protected static final String URLPREFIX_NAME = CLASSNAME + ".urlprefix"; //$NON-NLS-1$

	protected static final String GROUPIDLABEL_PROP_NAME = CLASSNAME
			+ ".groupIDLabel"; //$NON-NLS-1$

	protected static final String NAMESPACE_PROP_NAME = CLASSNAME
			+ ".namespace"; //$NON-NLS-1$

	protected static final String PAGE_DESCRIPTION = Messages.ConnectionDialog_SELECT_PROVIDER_DESCRIPTION;

	protected static final String JOINGROUP_FIELDNAME = Messages.ConnectionDialog_JOIN_GROUP_FIELD_TEXT;

	protected static final String NICKNAME_FIELDNAME = Messages.ConnectionDialog_NICKNAME_TEXT;

	protected static final String ECF_DEFAULT_URL = "ecftcp://localhost:3282/server"; //$NON-NLS-1$

	protected static final String ECF_TEMPLATE_URL = Messages.ConnectionDialog_TEMPLATE_URL;

	protected static final String PAGE_TITLE = Messages.ConnectionDialog_CONNECT_PAGE_TITLE;

	protected static final String DEFAULT_CLIENT = "ecf.generic.client"; //$NON-NLS-1$

	private static final String DIALOG_SETTINGS = CLASSNAME;

	private Composite paramComp;

	private Text joingroup_text;

	private String joinGroup = ""; //$NON-NLS-1$

	private Text nickname_text;

	private String nickname = ""; //$NON-NLS-1$

	private Text password_text;

	private String password = ""; //$NON-NLS-1$

	private String urlPrefix;

	private String namespace = null;

	private String containerType = ""; //$NON-NLS-1$

	private TableViewer viewer;

	private IDialogSettings dialogSettings;

	private GlobalModifyListener listener = new GlobalModifyListener();

	private Button autoLogin = null;

	private boolean autoLoginFlag = false;

	public ConnectionDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.TITLE | SWT.BORDER | SWT.CLOSE | SWT.RESIZE);
	}

	protected Control createDialogArea(Composite parent) {
		Composite main = new Composite((Composite) super
				.createDialogArea(parent), SWT.NONE);
		main.setLayout(new GridLayout());
		main.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label providerLabel = new Label(main, SWT.NONE);
		providerLabel.setText(Messages.ConnectionDialog_PROTOCOL_TEXT);

		Composite providerComp = new Composite(main, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		providerComp.setLayout(layout);
		providerComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		viewer = new TableViewer(providerComp, SWT.BORDER | SWT.FULL_SELECTION);
		viewer.setContentProvider(new ECFProviderContentProvider());
		viewer.setLabelProvider(new ECFProviderLabelProvider());
		viewer.addSelectionChangedListener(new ProviderSelector());

		Table table = viewer.getTable();
		GridData gData = new GridData(GridData.FILL_VERTICAL);
		gData.widthHint = PROVIDER_TABLE_WIDTH;
		gData.heightHint = PROVIDER_TABLE_HEIGHT;
		table.setLayoutData(gData);

/*
 * table.setHeaderVisible(true); TableColumn tc = new TableColumn(table,
 * SWT.NONE); tc.setText("Name"); tc = new TableColumn(table, SWT.NONE);
 * tc.setText("Classname");
 * 
 */
		viewer.setInput(ContainerFactory.getDefault().getDescriptions());

		paramComp = new Composite(providerComp, SWT.NONE);
		GridLayout glayout = new GridLayout();
		glayout.marginTop = 0;
		glayout.marginBottom = 0;
		paramComp.setLayout(glayout);
		paramComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		autoLogin = new Button(main, SWT.CHECK);
		autoLogin.setText(Messages.ConnectionDialog_AUTOLOGIN_TEXT);
		autoLogin.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		autoLogin.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				autoLoginFlag = autoLogin.getSelection();
			}
		});

		new Label(main, SWT.NONE);
		Label sep = new Label(main, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.setTitle(Messages.ConnectionDialog_CONNECTION_TITLE);
		this
				.setMessage(Messages.ConnectionDialog_CHOOSE_PROVIDER_MESSAGE);

		this.getShell().setText(Messages.ConnectionDialog_CONNECT_TEXT);
		return parent;
	}

	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);

		try {
			restoreDialogSettings();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return control;
	}

	protected Point getInitialSize() {
		return new Point(500, 400);
	}

	private class ECFProviderContentProvider implements
			IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			List rawDescriptions = (List) inputElement;
			List elements = new ArrayList();

			for (Iterator i = rawDescriptions.iterator(); i.hasNext();) {
				final ContainerTypeDescription desc = (ContainerTypeDescription) i
						.next();
				if (!desc.isServer() && !desc.isHidden())
					elements.add(desc);
			}

			return elements.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	public String getJoinGroupText() {
		String textValue = joinGroup.trim();
		String namespace = getNamespace();
		if (namespace != null) {
			return textValue;
		} else {
			if (!urlPrefix.equals("") && !textValue.startsWith(urlPrefix)) { //$NON-NLS-1$
				textValue = urlPrefix + textValue;
			}
			return textValue;
		}
	}

	public String getNicknameText() {
		return nickname;
	}

	public String getPasswordText() {
		return password;
	}

	public String getContainerType() {
		return containerType;
	}

	public String getNamespace() {
		return namespace;
	}

	public boolean getAutoLoginFlag() {
		return autoLoginFlag;
	}

	private void restoreDialogSettings() throws IOException {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null) {
			IDialogSettings pageSettings = dialogSettings
					.getSection(DIALOG_SETTINGS);
			if (pageSettings != null) {

				int intVal = pageSettings.getInt("provider"); //$NON-NLS-1$
				viewer.getTable().setSelection(intVal);
				viewer.setSelection(viewer.getSelection());
				String strVal = pageSettings.get("url"); //$NON-NLS-1$
				if (strVal != null && joingroup_text != null) {
					joingroup_text.setText(strVal);
				}

				strVal = pageSettings.get("nickname"); //$NON-NLS-1$
				if (strVal != null && nickname_text != null) {
					nickname_text.setText(strVal);
				}

				if (savePassword()) {
					strVal = pageSettings.get("password"); //$NON-NLS-1$
					if (strVal != null && password_text != null) {
						password_text.setText(strVal);
					}
				}

				listener.modifyText(null);
			}

		}
	}

	private void saveDialogSettings() {
		IDialogSettings dialogSettings = this.getDialogSettings();
		if (dialogSettings != null) {
			IDialogSettings pageSettings = dialogSettings
					.getSection(DIALOG_SETTINGS);
			if (pageSettings == null)
				pageSettings = dialogSettings.addNewSection(DIALOG_SETTINGS);

			pageSettings.put("url", this.getJoinGroupText()); //$NON-NLS-1$
			pageSettings.put("nickname", this.getNicknameText()); //$NON-NLS-1$
			pageSettings.put("password", this.getPasswordText()); //$NON-NLS-1$

			int i = viewer.getTable().getSelectionIndex();
			if (i >= 0)
				pageSettings.put("provider", i); //$NON-NLS-1$

/*
 * try { dialogSettings.save(this.getClass().toString()); } catch (IOException
 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
 */		}
	}

	private boolean savePassword() {
		return ClientPlugin.getDefault().getPluginPreferences().getBoolean(
				ClientPlugin.PREF_STORE_PASSWORD);
	}

	protected void okPressed() {
		saveDialogSettings();
		super.okPressed();
	}

	private IDialogSettings getDialogSettings() {
		if (dialogSettings == null) {
			dialogSettings = ClientPlugin.getDefault().getDialogSettings();
		}

		return dialogSettings;
	}

	private class ECFProviderLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				// TODO: If the container description contains an image for the
				// provider, display it here.
				return ClientPlugin.getDefault().getImageRegistry().get(
						ClientPluginConstants.DECORATION_DEFAULT_PROVIDER);
			}

			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			ContainerTypeDescription desc = (ContainerTypeDescription) element;
			switch (columnIndex) {
			case 0:
				return desc.getDescription();
			case 1:
				return desc.getName();
			}

			return ""; //$NON-NLS-1$
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

	}

	private class ProviderSelector implements ISelectionChangedListener {

		public void selectionChanged(SelectionChangedEvent event) {
			StructuredSelection selection = (StructuredSelection) event
					.getSelection();
			ContainerTypeDescription desc = (ContainerTypeDescription) selection
					.getFirstElement();
			if (desc != null) {
				containerType = desc.getName();
				// XXX replace with access to desc.getParameters()
				createPropertyComposite(paramComp, null);
				// createPropertyComposite(paramComp, desc.getProperties());
			}
		}

		protected void createPropertyComposite(Composite parent, Map properties) {
			if (properties != null) {
				String usePassword = (String) properties
						.get(USEPASSWORD_PROP_NAME);
				String examplegroupid = (String) properties
						.get(EXAMPLEGROUPID_PROP_NAME);
				String useNickname = (String) properties
						.get(USENICKNAME_PROP_NAME);
				urlPrefix = (String) properties.get(URLPREFIX_NAME);
				namespace = (String) properties.get(NAMESPACE_PROP_NAME);

				if (urlPrefix == null) {
					urlPrefix = ""; //$NON-NLS-1$
				}

				removeChildren(parent);

				String groupLabel = (String) properties
						.get(GROUPIDLABEL_PROP_NAME);

				Label groupIDLabel = new Label(parent, SWT.NONE);

				if (groupLabel != null) {
					groupIDLabel.setText(groupLabel);
				} else {
					groupIDLabel.setText(JOINGROUP_FIELDNAME);
				}

				joingroup_text = new Text(parent, SWT.BORDER);
				joingroup_text.setLayoutData(new GridData(
						GridData.FILL_HORIZONTAL));
				joingroup_text.addModifyListener(listener);

				if (examplegroupid != null) {
					Label example_label = new Label(parent, SWT.NONE);
					// set examplegroupid text
					example_label
							.setText((examplegroupid != null) ? examplegroupid
									: ""); //$NON-NLS-1$
					example_label.setLayoutData(new GridData(
							GridData.HORIZONTAL_ALIGN_END));
					// joingroup_text.setText((defaultgroupid != null) ?
					// defaultgroupid : "");
				}

				// turn off password unless used
				if (usePassword != null) {
					Label password_label = new Label(parent, SWT.NONE);
					password_label.setText(Messages.ConnectionDialog_PASSWORD_TEXT);
					password_text = new Text(parent, SWT.BORDER);
					password_text.setLayoutData(new GridData(
							GridData.FILL_HORIZONTAL));
					password_text.setEchoChar('*');
					password_text.addModifyListener(listener);
				}

				// turn off nickname unless used
				if (useNickname != null) {
					Label nickname_label = new Label(parent, SWT.NONE);
					nickname_label.setText(NICKNAME_FIELDNAME);

					nickname_text = new Text(parent, SWT.BORDER);
					nickname_text.setLayoutData(new GridData(
							GridData.FILL_HORIZONTAL));
					nickname_text.addModifyListener(listener);
				}
				autoLogin.setSelection(false);
				parent.layout();
			}
		}

		private void removeChildren(Composite composite) {
			if (composite != null && composite.getChildren() != null) {
				while (composite.getChildren().length > 0) {
					Control child = composite.getChildren()[0];
					if (child instanceof Composite) {
						removeChildren((Composite) child);
					}
					child.dispose();
				}
			}
		}
	}

	private class GlobalModifyListener implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			if (password_text != null && !password_text.isDisposed()) {
				password = password_text.getText();
			}

			if (nickname_text != null && !nickname_text.isDisposed()) {
				nickname = nickname_text.getText();
			}

			if (joingroup_text != null && !joingroup_text.isDisposed()) {
				joinGroup = joingroup_text.getText();
			}
		}

	}
}
