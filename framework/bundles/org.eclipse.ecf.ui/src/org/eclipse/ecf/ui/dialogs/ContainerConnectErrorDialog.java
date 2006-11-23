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
package org.eclipse.ecf.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;

public class ContainerConnectErrorDialog extends ErrorDialog {

	public ContainerConnectErrorDialog(Shell parentShell, int errorCode, String message, String targetID, Throwable exception) {
		super(parentShell, "Could Not Connect", "Could not connect to "+targetID, new Status(IStatus.ERROR,
				Activator.PLUGIN_ID,
				errorCode,
				message,
				exception),
				IStatus.ERROR);
		this.shouldShowDetailsButton();
	}

}