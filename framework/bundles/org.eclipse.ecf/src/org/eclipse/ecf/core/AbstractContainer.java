/*******************************************************************************
 * Copyright (c) 2006 IBM, Inc and Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Chris Aniszczyk <zx@us.ibm.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

import java.util.Iterator;

import java.util.Vector;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.core.events.IContainerEvent;

public abstract class AbstractContainer implements IContainer {

	private Vector containerListeners = new Vector();

	public void addListener(IContainerListener l, String filter) {
		containerListeners.add(l);
	}

	public void removeListener(IContainerListener l) {
		containerListeners.remove(l);
	}

	public void dispose() {}
	
	/**
	 * Fires a container event
	 * 
	 * @param event
	 */
	public void fireContainerEvent(IContainerEvent event) {
		for (Iterator i = containerListeners.iterator(); i.hasNext();) {
			IContainerListener l = (IContainerListener) i.next();
			l.handleEvent(event);
		}
	}

	public Object getAdapter(Class serviceType) {
		return Platform.getAdapterManager().getAdapter(this, serviceType);
	}
	
}
