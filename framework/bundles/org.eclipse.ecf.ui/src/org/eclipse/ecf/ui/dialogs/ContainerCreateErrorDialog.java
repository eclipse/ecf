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
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.internal.ui.Messages;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Error dialog to show when an {@link ContainerCreateException} is thrown.
 */
public class ContainerCreateErrorDialog extends ErrorDialog {

	public ContainerCreateErrorDialog(Shell parentShell, Throwable exception) {
		super(
				parentShell,
				Messages.ContainerCreateErrorDialog_CREATE_CONTAINER_ERROR_TITLE,
				Messages.ContainerCreateErrorDialog_CREATE_CONTAINER_ERROR_MESSAGE,
				new MultiStatus(
						Activator.PLUGIN_ID,
						IStatus.ERROR,
						new IStatus[] { new Status(
								IStatus.ERROR,
								Activator.PLUGIN_ID,
								IStatus.ERROR,
								(exception != null) ? exception
										.getLocalizedMessage()
										: Messages.ContainerCreateErrorDialog_CREATE_CONTAINER_ERROR_MESSAGE,
								exception) },
						(exception != null) ? exception.getLocalizedMessage()
								: Messages.ContainerCreateErrorDialog_CREATE_CONTAINER_ERROR_MESSAGE,
						exception), IStatus.ERROR);
	}

}