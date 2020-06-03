/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.start;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * Interface that must be implemented by extensions of the org.eclipse.ecf.start
 * extension point. Such extensions will have their start method called by a new
 * Job upon ECF startup.
 */
public interface IECFStart {
	/**
	 * Run some startup task.
	 * @param monitor 
	 * 
	 * @return IStatus the status of the start
	 */
	public IStatus run(IProgressMonitor monitor);
}
