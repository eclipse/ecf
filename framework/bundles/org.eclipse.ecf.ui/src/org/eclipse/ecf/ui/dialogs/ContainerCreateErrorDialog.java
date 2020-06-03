/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.internal.ui.Messages;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Error dialog to show when an {@link ContainerCreateException} is thrown.
 */
public class ContainerCreateErrorDialog extends ErrorDialog {

	public ContainerCreateErrorDialog(Shell parentShell, ContainerCreateException createException) {
		super(
				parentShell,
				Messages.ContainerCreateErrorDialog_CREATE_CONTAINER_ERROR_TITLE,
				Messages.ContainerCreateErrorDialog_CREATE_CONTAINER_ERROR_MESSAGE,
				createException.getStatus(), IStatus.ERROR);	
	}
	
	public ContainerCreateErrorDialog(Shell parentShell, IStatus status) {
		super(
				parentShell,
				Messages.ContainerCreateErrorDialog_CREATE_CONTAINER_ERROR_TITLE,
				Messages.ContainerCreateErrorDialog_CREATE_CONTAINER_ERROR_MESSAGE,
				status, IStatus.ERROR);
	}

}