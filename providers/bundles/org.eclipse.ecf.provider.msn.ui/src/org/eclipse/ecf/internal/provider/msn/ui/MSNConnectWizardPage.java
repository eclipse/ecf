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
package org.eclipse.ecf.internal.provider.msn.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

final class MSNConnectWizardPage extends WizardPage {

	private Combo emailText;

	private Text passwordText;

	private String username;

	MSNConnectWizardPage() {
		super(MSNConnectWizardPage.class.getName());
		setTitle(Messages.MSNConnectWizardPage_Title);
		setPageComplete(false);
	}

	MSNConnectWizardPage(String username) {
		this();
		this.username = username;
	}

	private void addListeners() {
		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String email = emailText.getText().trim();
				if (email.equals("")) { //$NON-NLS-1$
					setErrorMessage(Messages.MSNConnectWizardPage_EmailAddressRequired);
				} else if (email.indexOf('@') == -1) {
					setErrorMessage(Messages.MSNConnectWizardPage_EmailAddressInvalid);
				} else if (passwordText.getText().trim().equals("")) { //$NON-NLS-1$
					setErrorMessage(Messages.MSNConnectWizardPage_PasswordRequired);
				} else {
					setErrorMessage(null);
				}
			}
		};

		emailText.addModifyListener(listener);
		passwordText.addModifyListener(listener);
	}

	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);

		Label label = new Label(parent, SWT.LEFT);
		label.setText(Messages.MSNConnectWizardPage_EmailAddressLabel);
		emailText = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN);
		emailText.setLayoutData(data);
		
		restoreCombo();

		label = new Label(parent, SWT.LEFT);
		label.setText(Messages.MSNConnectWizardPage_PasswordLabel);
		passwordText = new Text(parent, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
		passwordText.setLayoutData(data);

		addListeners();
		if (username != null) {
			emailText.setText(username);
			passwordText.setFocus();
		}
		setControl(parent);
	}

	String getEmail() {
		return emailText.getText();
	}

	String getPassword() {
		return passwordText.getText();
	}

	public void setErrorMessage(String message) {
		super.setErrorMessage(message);
		setPageComplete(message == null);
	}

	private static final String PAGE_SETTINGS = MSNConnectWizardPage.class
			.getName();
	private static final int MAX_COMBO_VALUES = 40;
	private static final String COMBO_TEXT_KEY = "connectTextValue";
	private static final String COMBO_BOX_ITEMS_KEY = "comboValues";

	protected void saveComboText() {
		IDialogSettings pageSettings = getPageSettings();
		if (pageSettings != null)
			pageSettings.put(COMBO_TEXT_KEY, emailText.getText());
	}

	protected void saveComboItems() {
		IDialogSettings pageSettings = getPageSettings();
		if (pageSettings != null) {
			String connectTextValue = emailText.getText();
			List rawItems = Arrays.asList(emailText.getItems());
			// If existing text item is not in combo box then add it
			List items = new ArrayList();
			if (!rawItems.contains(connectTextValue))
				items.add(connectTextValue);
			items.addAll(rawItems);
			int itemsToSaveLength = items.size();
			if (itemsToSaveLength > MAX_COMBO_VALUES)
				itemsToSaveLength = MAX_COMBO_VALUES;
			String[] itemsToSave = new String[itemsToSaveLength];
			System.arraycopy(items.toArray(new String[] {}), 0, itemsToSave, 0,
					itemsToSaveLength);
			pageSettings.put(COMBO_BOX_ITEMS_KEY, itemsToSave);
		}
	}

	public IDialogSettings getDialogSettings() {
		return Activator.getDefault().getDialogSettings();
	}

	private IDialogSettings getPageSettings() {
		IDialogSettings pageSettings = null;
		IDialogSettings dialogSettings = this.getDialogSettings();
		if (dialogSettings != null) {
			pageSettings = dialogSettings.getSection(PAGE_SETTINGS);
			if (pageSettings == null)
				pageSettings = dialogSettings.addNewSection(PAGE_SETTINGS);
			return pageSettings;
		}
		return null;
	}

	protected void restoreCombo() {
		IDialogSettings pageSettings = getPageSettings();
		if (pageSettings != null) {
			String[] items = pageSettings.getArray(COMBO_BOX_ITEMS_KEY);
			if (items != null)
				emailText.setItems(items);
			String text = pageSettings.get(COMBO_TEXT_KEY);
			if (text != null)
				emailText.setText(text);
		}
	}

}
