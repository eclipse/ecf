/*******************************************************************************
* Copyright (c) 2008 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.core.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

public class ImmediateExecutor implements IExecutor, IRunnableExecutor {

	public IFuture execute(IProgressRunnable runnable, IProgressMonitor monitor) {
		Assert.isNotNull(runnable);
		SingleOperationFuture sof = new SingleOperationFuture(monitor);
		try {
			sof.set(runnable.run(sof.getProgressMonitor()));
		} catch (Throwable t) {
			sof.setException(t);
		}
		return sof;
	}

	public void execute(final Runnable runnable) {
		Assert.isNotNull(runnable);
		execute(new IProgressRunnable() {
			public Object run(IProgressMonitor pm) {
				runnable.run();
				return null;
			}
		}, null);
	}
}
