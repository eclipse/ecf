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
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.internal.ui.Messages;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

/**
 * Error dialog to show when an {@link ContainerConnectException} is thrown.
 */
public class ContainerConnectErrorDialog extends ErrorDialog {

	public ContainerConnectErrorDialog(Shell parentShell, int errorCode,
			String message, String targetID, Throwable exception) {
		super(
				parentShell,
				Messages.ContainerConnectErrorDialog_ERROR_TITLE,
				NLS.bind(Messages.ContainerConnectErrorDialog_ERROR_MESSAGE,
						targetID),
				new MultiStatus(Activator.PLUGIN_ID,
						IStatus.ERROR,
						new IStatus [] { new Status(IStatus.ERROR,Activator.PLUGIN_ID,IStatus.ERROR,(exception != null) ? exception.getLocalizedMessage()
								: NLS
								.bind(
										Messages.ContainerConnectErrorDialog_ERROR_MESSAGE,
										targetID), exception) },
						(exception != null) ? exception.getLocalizedMessage()
								: NLS
										.bind(
												Messages.ContainerConnectErrorDialog_ERROR_MESSAGE,
												targetID), exception),
				IStatus.ERROR);
	}

	public ContainerConnectErrorDialog(Shell parentShell, String targetID,
			Throwable exception) {
		this(parentShell, IStatus.ERROR,
				Messages.ContainerConnectErrorDialog_ERROR_MESSAGE, targetID,
				exception);
	}
}