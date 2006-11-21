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

public class GenericClientConnectWizardPage extends AbstractConnectWizardPage {

	protected boolean shouldRequestUsername() {
		return true;
	}

	protected boolean shouldRequestPassword() {
		return true;
	}

	protected String getExampleID() {
		return "<protocol>://<machinename>:<port>/<servicename>";
	}

	protected String getProviderTitle() {
		return "ECF Generic Client Connection";
	}

	protected String getProviderDescription() {
		return "Creates a connection to the specified ECF Generic Server.";
	}

}
