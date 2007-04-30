/****************************************************************************
 * Copyright (c) 2007 Remy Suen, Composent Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.irc.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

final class IRCConnectWizardPage extends WizardPage {

	private Text connectText;

	private Text passwordText;

	private String authorityAndPath;
	
	IRCConnectWizardPage() {
		super("");
		setTitle("IRC Connection Wizard");
		setDescription("Specify a user and IRC server to connect to.");
		setPageComplete(false);
	}
	
	IRCConnectWizardPage(String authorityAndPath) {
		this();
		this.authorityAndPath = authorityAndPath;
	}
	
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout());
		GridData fillData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		GridData endData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);

		Label label = new Label(parent, SWT.LEFT);
		label.setText("Connect ID:");

		connectText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		connectText.setLayoutData(fillData);
		connectText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = connectText.getText();
				if (text.equals("")) { //$NON-NLS-1$
					updateStatus("A valid connect ID must be specified.");
				} else if (text.indexOf('@') == -1) {
					updateStatus("The connect ID is malformed.");
				} else {
					updateStatus(null);
				}
			}
		});
		label = new Label(parent, SWT.RIGHT);
		label.setText("<user>@<ircserver>[:port][/<channel>,<channel2>,...]");
		label.setLayoutData(endData);

		label = new Label(parent, SWT.LEFT);
		label.setText("Password:");
		passwordText = new Text(parent, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
		passwordText.setLayoutData(fillData);
		label = new Label(parent, SWT.RIGHT | SWT.WRAP);
		label.setText("This password is for password-protected IRC servers.");
		label.setLayoutData(endData);

		if (authorityAndPath != null) {
			connectText.setText(authorityAndPath);
			passwordText.setFocus();
		} 
		
		setControl(parent);
	}

	String getConnectID() {
		return connectText.getText();
	}

	String getPassword() {
		return passwordText.getText();
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

}
