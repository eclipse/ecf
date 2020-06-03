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
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

/**
 * Error dialog to show when an {@link ContainerConnectException} is thrown.
 */
public class IDCreateErrorDialog extends ErrorDialog {

	public IDCreateErrorDialog(Shell parentShell, String targetID, IDCreateException createException) {
		super(parentShell, "ID Create Error", //$NON-NLS-1$
				NLS.bind("Could not create ID with {0}", //$NON-NLS-1$
						targetID), createException.getStatus(), IStatus.ERROR);

	}

}