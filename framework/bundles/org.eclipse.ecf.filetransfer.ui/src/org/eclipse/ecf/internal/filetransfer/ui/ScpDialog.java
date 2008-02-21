/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.filetransfer.ui;

import java.io.File;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class ScpDialog extends InputDialog {

	private Text useridText;
	private Text passwordText;
	public String userid;
	public String passwd;
	public String filename;
	protected Text fileLocation;

	public ScpDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Label label = new Label(composite, SWT.WRAP);
		label.setText(Messages.getString("ScpDialog.OutputFile")); //$NON-NLS-1$
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		label.setLayoutData(data);
		label.setFont(parent.getFont());
		fileLocation = new Text(composite, SWT.SINGLE | SWT.BORDER);
		fileLocation.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		final Button fileBrowse = new Button(composite, SWT.PUSH);
		fileBrowse.setText(Messages.getString("ScpDialog.Browse")); //$NON-NLS-1$
		fileBrowse.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.Selection) {
					String scp = getValue();
					String fileName = ""; //$NON-NLS-1$
					String path = System.getProperty("user.home"); //$NON-NLS-1$
					if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
						path = path + File.separator + "Desktop"; //$NON-NLS-1$
					}
					if (scp != null && scp.length() > 0) {
						fileName = scp.substring(scp.lastIndexOf('/') + 1);
					}
					FileDialog fd = new FileDialog(fileBrowse.getShell(), SWT.SAVE);
					fd.setText(Messages.getString("ScpDialog.OutputFile")); //$NON-NLS-1$
					fd.setFileName(fileName);
					fd.setFilterPath(path);
					String fname = fd.open();
					if (fname != null) {
						fileLocation.setText(fname);
					}
				}
			}
		});

		label = new Label(composite, SWT.WRAP);
		label.setText(Messages.getString("ScpDialog.Userid")); //$NON-NLS-1$
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		label.setLayoutData(data);
		label.setFont(parent.getFont());
		useridText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		useridText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

		label = new Label(composite, SWT.WRAP);
		label.setText(Messages.getString("ScpDialog.Password")); //$NON-NLS-1$
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		label.setLayoutData(data);
		label.setFont(parent.getFont());
		passwordText = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		applyDialogFont(composite);
		return composite;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			userid = useridText.getText();
			passwd = passwordText.getText();
			filename = fileLocation.getText();
		}
		super.buttonPressed(buttonId);
	}
}
