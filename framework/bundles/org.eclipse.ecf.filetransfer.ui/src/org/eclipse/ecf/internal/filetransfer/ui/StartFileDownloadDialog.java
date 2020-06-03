/****************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.filetransfer.ui;

import java.io.File;
import java.net.URL;
import org.eclipse.jface.dialogs.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class StartFileDownloadDialog extends InputDialog {

	private Text useridText;
	private Text passwordText;
	public String userid;
	public String passwd;
	public String filename;
	protected Text fileLocation;

	String filePath;

	public StartFileDownloadDialog(Shell parentShell, String startURL) {
		super(parentShell, Messages.getString("StartFileDownloadDialog.FileTransfer"), Messages.getString("StartFileDownloadDialog.Source"), startURL, null); //$NON-NLS-1$ //$NON-NLS-2$
		filePath = Activator.getDefault().getPreferenceStore().getString(Activator.DOWNLOAD_PATH_PREFERENCE);
		filePath = (filePath == null) ? "" : filePath; //$NON-NLS-1$
	}

	String getFullDownloadPath(String path, String fileName) {
		if (fileName == null)
			return filePath;
		String result = filePath;
		if (!result.endsWith(File.separator)) {
			result = result + File.separator;
		}
		return result + fileName;
	}

	public StartFileDownloadDialog(Shell parentShell) {
		this(parentShell, null);
	}

	Text getInputText() {
		return getText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	public void create() {
		super.create();
		Button okButton = getButton(IDialogConstants.OK_ID);
		okButton.setText(Messages.getString("StartFileDownloadDialog.DOWNLOAD_BUTTON")); //$NON-NLS-1$
		okButton.setToolTipText(Messages.getString("StartFileDownloadDialog.DOWNLOAD_BUTTON_TOOLTIP")); //$NON-NLS-1$
		URL url = validateURL();
		if (url != null) {
			String fileName = getFileNameFromURL();
			if (!"".equals(fileName)) { //$NON-NLS-1$
				fileLocation.setText(getFullDownloadPath(filePath, fileName));
				useridText.setFocus();
			}
			String user = url.getUserInfo();
			if (user != null) {
				useridText.setText(user);
				passwordText.setFocus();
			}
			okButton.setEnabled(true);
		} else
			okButton.setEnabled(false);
	}

	URL validateURL() {
		String text = getInputText().getText();
		URL result = null;
		try {
			if (!"".equals(text)) { //$NON-NLS-1$
				result = new URL(text);
			}
		} catch (Exception e) {
			setErrorMessage((NLS.bind(Messages.getString("StartFileDownloadDialog.MalformedURLException"), text))); //$NON-NLS-1$
			return null;
		}
		setErrorMessage(null);
		return result;
	}

	String getFileNameFromURL() {
		String scp = getInputText().getText();
		String fileName = ""; //$NON-NLS-1$
		if (scp != null && scp.length() > 0) {
			fileName = scp.substring(scp.lastIndexOf('/') + 1);
		}
		return fileName;
	}

	String getCurrentDirectory() {
		String currentDirectory = fileLocation.getText();
		return currentDirectory.substring(0, currentDirectory.lastIndexOf(File.separator));
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Label label = new Label(composite, SWT.WRAP);
		label.setText(Messages.getString("StartFileDownloadDialog.OutputFile")); //$NON-NLS-1$
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		label.setLayoutData(data);
		label.setFont(parent.getFont());

		fileLocation = new Text(composite, SWT.SINGLE | SWT.BORDER);
		fileLocation.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		fileLocation.setText(filePath);
		fileLocation.setSelection(fileLocation.getText().length());

		Text text = getInputText();
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateURL();
			}
		});

		text.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				// nothing
			}

			public void focusLost(FocusEvent e) {
				fileLocation.setText(getFullDownloadPath(getCurrentDirectory(), getFileNameFromURL()));
				fileLocation.setSelection(fileLocation.getText().length());
			}
		});

		final Button fileBrowse = new Button(composite, SWT.PUSH);
		fileBrowse.setText(Messages.getString("StartFileDownloadDialog.Browse")); //$NON-NLS-1$
		fileBrowse.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.type == SWT.Selection) {
					String fileName = getFileNameFromURL();
					FileDialog fd = new FileDialog(fileBrowse.getShell(), SWT.SAVE);
					fd.setText(Messages.getString("StartFileDownloadDialog.OutputFile")); //$NON-NLS-1$
					fd.setFileName(fileName);
					fd.setFilterPath(getCurrentDirectory());
					String fname = fd.open();
					if (fname != null) {
						fileLocation.setText(fname);
						fileLocation.setSelection(fileLocation.getText().length());
					}
				}
			}
		});

		label = new Label(composite, SWT.WRAP);
		label.setText(Messages.getString("StartFileDownloadDialog.Userid")); //$NON-NLS-1$
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
		label.setLayoutData(data);
		label.setFont(parent.getFont());
		useridText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		useridText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		useridText.setText(System.getProperty("user.name")); //$NON-NLS-1$
		useridText.setSelection(useridText.getText().length());

		label = new Label(composite, SWT.WRAP);
		label.setText(Messages.getString("StartFileDownloadDialog.Password")); //$NON-NLS-1$
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
			File f = new File(filename);
			if (f.exists()) {
				if (MessageDialog.openQuestion(getShell(), Messages.getString("StartFileDownloadDialog.FILE_EXISTS_TITLE"), NLS.bind(Messages.getString("StartFileDownloadDialog.FILE_EXISTS_MESSAGE"), filename))) { //$NON-NLS-1$ //$NON-NLS-2$
					super.buttonPressed(buttonId);
				} else
					fileLocation.setFocus();
				return;
			}
		}
		super.buttonPressed(buttonId);
	}
}
