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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

final class MSNConnectWizardPage extends WizardPage {

	private Text emailText;

	private Text passwordText;

	MSNConnectWizardPage() {
		super(MSNConnectWizardPage.class.getName());
		setTitle(Messages.MSNConnectWizardPage_Title);
		setPageComplete(false);
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
		emailText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		emailText.setLayoutData(data);

		label = new Label(parent, SWT.LEFT);
		label.setText(Messages.MSNConnectWizardPage_PasswordLabel);
		passwordText = new Text(parent, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
		passwordText.setLayoutData(data);

		addListeners();
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

}
