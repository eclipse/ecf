/****************************************************************************
 * Copyright (c) 2006 Remy Suen, Composent, Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.ui.wizards;

import java.net.URI;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A abstract <tt>WizardPage</tt> subclass that can be customized to request
 * arbitrary login information from the user for connecting to an
 * {@link IContainer}.
 */
public abstract class AbstractConnectWizardPage extends WizardPage {

	private static final String PAGE_NAME = "ECF Connect Wizard Page"; //$NON-NLS-1$

	private Text connectText;

	private Text usernameText;

	private Text passwordText;

	private ModifyListener inputVerifier = new InputVerifier();

	private Button autoLoginBtn;

	private URI uri;
	
	protected AbstractConnectWizardPage() {
		super(PAGE_NAME);
		setTitle(getProviderTitle());
		setDescription(getProviderDescription());
		setPageComplete(false);
	}

	protected AbstractConnectWizardPage(URI uri) {
		this();
		this.uri = uri;
	}

	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		GridData span = new GridData(SWT.END, SWT.CENTER, true, false, 2, 1);

		Label label = new Label(parent, SWT.LEFT);
		label.setText("Connect ID:");
		connectText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		connectText.setLayoutData(data);
		connectText.addModifyListener(inputVerifier);

		String defaultConnectText = getDefaultConnectText();
		if (defaultConnectText != null && !defaultConnectText.equals("")) {
			connectText.setText(defaultConnectText);
		}
		
		String exampleID = getExampleID();
		if (exampleID != null && !exampleID.equals("")) { //$NON-NLS-1$
			label = new Label(parent, SWT.RIGHT);
			label.setText(getExampleID());
			label.setLayoutData(span);
		}

		if (shouldRequestUsername()) {
			label = new Label(parent, SWT.LEFT);
			label.setText("Username:");
			usernameText = new Text(parent, SWT.SINGLE | SWT.BORDER);
			usernameText.setLayoutData(data);
			usernameText.addModifyListener(inputVerifier);
		}

		if (shouldRequestUsername()) {
			label = new Label(parent, SWT.LEFT);
			label.setText("Password:");
			passwordText = new Text(parent, SWT.SINGLE | SWT.BORDER);
			passwordText.setLayoutData(data);
			passwordText.setEchoChar('*');
			passwordText.addModifyListener(inputVerifier);
		}

		autoLoginBtn = new Button(parent, SWT.CHECK);
		autoLoginBtn.setText("Login &automatically at startup");
		autoLoginBtn.setLayoutData(new GridData(SWT.END, SWT.CENTER, true,
				false, 2, 1));

		if (uri != null) {
			connectText.setText(uri.toString());
			usernameText.setFocus();
		}
		setControl(parent);
	}

	/**
	 * Returns whether the created {@link IContainer} needs a username for
	 * authentication purposes upon connecting. A text control will be displayed
	 * on the wizard dialog to allow the user to input a username.
	 * 
	 * @return <tt>true</tt> if a username is required to connect to the
	 *         selected <tt>IContainer</tt>, <tt>false</tt> otherwise
	 */
	public abstract boolean shouldRequestUsername();

	/**
	 * Checks
	 * @return boolean true if page should request password
	 */
	public abstract boolean shouldRequestPassword();

	public abstract String getExampleID();

	protected String getDefaultConnectText(){
		return "";
	}
	
	protected String getProviderTitle() {
		return "New Provider Connection";
	}

	protected String getProviderDescription() {
		return null;
	}

	public String getConnectID() {
		return connectText.getText();
	}

	public String getUsername() {
		return usernameText.getText();
	}

	public String getPassword() {
		return passwordText.getText();
	}

	boolean shouldAutoLogin() {
		return autoLoginBtn.getSelection();
	}
	
	public void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	private class InputVerifier implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			if (connectText.getText().equals("")) { //$NON-NLS-1$
				updateStatus("A connect ID must be specified.");
			} else {
				updateStatus(null);
			}
		}

	}
}
