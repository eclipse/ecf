/****************************************************************************
 * Copyright (c) 2004, 2009 Composent, Inc. and others.
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

package org.eclipse.ecf.ui;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Required interface for implementing <b>org.eclipse.ecf.ui.connectWizards</b>
 * extension point. Extensions for extension point
 * <b>org.eclipse.ecf.ui.connectWizards</b> must provide a class implementing
 * this interface.
 */
public interface IConnectWizard extends IWizard {

	/**
	 * Initialize the connect wizard.
	 * 
	 * @param workbench
	 *            the currently working workbench instance. Will not be null.
	 * @param container
	 *            the container that is to be connected. Will not be null.
	 */
	public void init(IWorkbench workbench, IContainer container);

	/**
	 * Performs actions in response to the user canceling the wizard and returns
	 * <code>true</code> if it was allowed, <code>false</code> if refused.
	 * Implementations should dispose of the container provided via
	 * {@link #init(IWorkbench, IContainer)} in this method if the cancelation
	 * is allowed.
	 *
	 * @return <code>true</code> if the cancel request was allowed, <code>false</code> otherwise
	 */
	public boolean performCancel();
}
