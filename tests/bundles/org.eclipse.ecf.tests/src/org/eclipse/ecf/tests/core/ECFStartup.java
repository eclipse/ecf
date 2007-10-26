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

package org.eclipse.ecf.tests.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.start.IECFStart;

/**
 *
 */
public class ECFStartup implements IECFStart {

	static boolean isSet = false;

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.start.IECFStart#startup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) {
		isSet = true;
		int i = 0;
		while (i < 10)
			System.out.println("thread=" + Thread.currentThread().getName() + " i=" + i++);
		return Status.OK_STATUS;
	}

}
