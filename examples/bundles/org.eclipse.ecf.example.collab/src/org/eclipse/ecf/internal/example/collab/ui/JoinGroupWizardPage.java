/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.example.collab.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.ui.SharedImages;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class JoinGroupWizardPage extends WizardPage {

	protected static final String CLASSNAME = JoinGroupWizardPage.class
			.getName();

	protected static final String USER_NAME_SYSTEM_PROPERTY = "user.name";

	protected static final String PAGE_DESCRIPTION = "Complete account info and choose 'Finish' to login.";
	protected static final String JOINGROUP_FIELDNAME = "Group ID:";
	protected static final String NICKNAME_FIELDNAME = "Nickname:";
	protected static final String ECF_DEFAULT_URL = "ecftcp://ecf.eclipse.org:3282/server";
	protected static final String ECF_TEMPLATE_URL = "ecftcp://<server>:<port>/<groupname>";
	protected static final String PAGE_TITLE = "Connect Generic Client";

	protected static final String DEFAULT_CLIENT = "ecf.generic.client";

	private static final String DIALOG_SETTINGS = CLASSNAME;

	public JoinGroupWizardPage() {
		super("wizardPage");
		setTitle(PAGE_TITLE);
		setDescription(PAGE_DESCRIPTION);
		setImageDescriptor(SharedImages
				.getImageDescriptor(SharedImages.IMG_COLLABORATION_WIZARD));
	}

	protected String template_url = ECF_TEMPLATE_URL;
	protected String default_url = ECF_DEFAULT_URL;

	protected Text nicknameText;
	protected Text joinGroupText;
	protected Combo combo;
	protected List containerDescriptions = new ArrayList();
	protected String urlPrefix = "";

// private Button autoLogin = null;
	private boolean autoLoginFlag = false;

	public boolean getAutoLoginFlag() {
		return autoLoginFlag;
	}

	protected void fillCombo() {
		ContainerTypeDescription desc = ContainerFactory.getDefault()
				.getDescriptionByName(DEFAULT_CLIENT);
		if (desc != null) {
			String name = desc.getName();
			String description = desc.getDescription();
			combo.add(description + " - " + name);
			containerDescriptions.add(desc);
			combo.select(0);
		}
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(2, false);
		container.setLayout(gridLayout);
		setControl(container);

		final Label label_4 = new Label(container, SWT.NONE);
		label_4.setText("Protocol:");

		combo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		combo.setLayoutData(data);

		Label groupIDLabel = new Label(container, SWT.NONE);
		groupIDLabel.setText(JOINGROUP_FIELDNAME);

		joinGroupText = new Text(container, SWT.BORDER);
		joinGroupText.setText(default_url);
		joinGroupText.setLayoutData(data);

		Label exampleLabel = new Label(container, SWT.NONE);
		exampleLabel.setText(template_url);
		exampleLabel.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false,
				false, 2, 1));

		joinGroupText.setLayoutData(data);
		joinGroupText.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				joinGroupText.selectAll();
			}
		});

		Label nicknameLabel = new Label(container, SWT.NONE);
		nicknameLabel.setLayoutData(new GridData());
		nicknameLabel.setText(NICKNAME_FIELDNAME);

		nicknameText = new Text(container, SWT.BORDER);
		nicknameText.setLayoutData(data);
		nicknameText.setText(System.getProperty(USER_NAME_SYSTEM_PROPERTY));
		nicknameText.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				nicknameText.selectAll();
			}
		});
		fillCombo();
		restoreDialogSettings();
	}

	private void restoreDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null) {
			IDialogSettings pageSettings = dialogSettings
					.getSection(DIALOG_SETTINGS);
			if (pageSettings != null) {
				String strVal = pageSettings.get("provider");
				if (strVal != null) {
					String[] items = combo.getItems();
					for (int i = 0; i < items.length; ++i)
						if (strVal.equals(items[i])) {
							break;
						}
				}

				strVal = pageSettings.get("url");
				if (strVal != null)
					joinGroupText.setText(strVal);

				strVal = pageSettings.get("nickname");
				if (strVal != null)
					nicknameText.setText(strVal);
			}
		}
	}

	public void saveDialogSettings() {
		IDialogSettings dialogSettings = getDialogSettings();
		if (dialogSettings != null) {
			IDialogSettings pageSettings = dialogSettings
					.getSection(DIALOG_SETTINGS);
			if (pageSettings == null)
				pageSettings = dialogSettings.addNewSection(DIALOG_SETTINGS);

			pageSettings.put("url", joinGroupText.getText());
			pageSettings.put("nickname", nicknameText.getText());
			int i = combo.getSelectionIndex();
			if (i >= 0)
				pageSettings.put("provider", combo.getItem(i));
		}
	}

	public String getJoinGroupText() {
		String textValue = joinGroupText.getText().trim();
		if (!urlPrefix.equals("") && !textValue.startsWith(urlPrefix)) {
			textValue = urlPrefix + textValue;
		}
		return textValue;
	}

	public String getNicknameText() {
		if (nicknameText == null)
			return null;
		return nicknameText.getText().trim();
	}

	public String getContainerType() {
		int index = combo.getSelectionIndex();
		if (index == -1)
			return null;
		else {
			ContainerTypeDescription desc = (ContainerTypeDescription) containerDescriptions
					.get(index);
			return desc.getName();
		}
	}
}
