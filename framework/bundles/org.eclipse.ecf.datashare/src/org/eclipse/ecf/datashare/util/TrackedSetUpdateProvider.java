/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.datashare.util;

import java.util.Map;

import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.ISharedData;
import org.eclipse.ecf.datashare.IUpdateProvider;
import org.eclipse.ecf.datashare.IUpdateProviderFactory;

/**
 * @author pnehrer
 */
public class TrackedSetUpdateProvider implements IUpdateProvider {

	private final Factory factory;

	private TrackedSetUpdateProvider(Factory factory) {
		this.factory = factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IUpdateProvider#getFactory()
	 */
	public IUpdateProviderFactory getFactory() {
		return factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IUpdateProvider#createUpdate(org.eclipse.ecf.datashare.ISharedData)
	 */
	public Object createUpdate(ISharedData graph) throws ECFException {
		return ((TrackedSet) graph.getData()).getChanges();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IUpdateProvider#applyUpdate(org.eclipse.ecf.datashare.ISharedData,
	 *      java.lang.Object)
	 */
	public void applyUpdate(ISharedData graph, Object data) throws ECFException {
		((TrackedSet) graph.getData()).apply((NotifyingSet.ChangeDelta[]) data);
	}

	public static class Factory implements IUpdateProviderFactory {

		public static final String ID = "org.eclipse.ecf.datashare.util.TrackedSet";

		public String getID() {
			return ID;
		}

		public IUpdateProvider createProvider(Map params) {
			return new TrackedSetUpdateProvider(this);
		}
	}
}
