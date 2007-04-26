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
package org.eclipse.ecf.internal.provider.xmpp.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class XMPPConnectWizardPage extends WizardPage {

	Text connectText;

	Text passwordText;

	XMPPConnectWizardPage() {
		super(""); //$NON-NLS-1$
		setTitle(Messages.XMPPConnectWizardPage_WIZARD_TITLE);
		setDescription(Messages.XMPPConnectWizardPage_WIZARD_DESCRIPTION);
		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout());
		GridData fillData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		GridData endData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);

		Label label = new Label(parent, SWT.LEFT);
		label.setText(Messages.XMPPConnectWizardPage_LABEL_USERID);

		connectText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		connectText.setLayoutData(fillData);
		connectText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = connectText.getText();
				if (text.equals("")) { //$NON-NLS-1$
					updateStatus(Messages.XMPPConnectWizardPage_WIZARD_STATUS);
				} else if (text.indexOf('@') == -1) {
					updateStatus(Messages.XMPPConnectWizardPage_WIZARD_STATUS_INCOMPLETE);
				} else {
					updateStatus(null);
				}
			}
		});

		label = new Label(parent, SWT.RIGHT);
		label.setText(Messages.XMPPConnectWizardPage_USERID_TEMPLATE);
		label.setLayoutData(endData);

		label = new Label(parent, SWT.LEFT);
		label.setText(Messages.XMPPConnectWizardPage_WIZARD_PASSWORD);
		passwordText = new Text(parent, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
		passwordText.setLayoutData(fillData);

		setControl(parent);
	}

	String getConnectID() {
		return connectText.getText();
	}

	String getPassword() {
		return passwordText.getText();
	}

	protected void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

}
