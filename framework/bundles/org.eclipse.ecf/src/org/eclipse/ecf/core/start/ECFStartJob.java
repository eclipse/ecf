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
package org.eclipse.ecf.core.start;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Start job for running extensions of the org.eclipse.ecf.start extension point
 * 
 */
public class ECFStartJob extends Job {

	IECFStart start;

	public ECFStartJob(String name, IECFStart start) {
		super(name);
		this.start = start;
	}

	protected IStatus run(IProgressMonitor monitor) {
		return start.run(monitor);
	}
}
