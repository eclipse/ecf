/*******************************************************************************
 * Copyright (c) 2013 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.sharedobject.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.OptimisticSharedObject;

/**
 * @since 2.4
 */
public class SharedModel extends OptimisticSharedObject {

	private List<ISharedModelListener> listeners = new ArrayList<ISharedModelListener>();

	public boolean addListener(ISharedModelListener listener) {
		synchronized (listeners) {
			return listeners.add(listener);
		}
	}

	public boolean removeListener(ISharedModelListener listener) {
		synchronized (listeners) {
			return listeners.remove(listener);
		}
	}

	protected void handleListenerException(ISharedModelListener l, Throwable exception) {
		log(0, "Exception in listener " + l, exception); //$NON-NLS-1$
	}

	protected void fireListeners(final ISharedModelEvent event) {
		List<ISharedModelListener> localCopy = null;
		synchronized (listeners) {
			localCopy = new ArrayList<ISharedModelListener>(listeners);
		}
		for (ISharedModelListener l : localCopy) {
			final ISharedModelListener list = l;
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					handleListenerException(list, exception);
				}

				public void run() throws Exception {
					list.handleEvent(event);
				}
			});
		}
	}

	@Override
	public void dispose(ID containerID) {
		super.dispose(containerID);
		listeners.clear();
	}
}
