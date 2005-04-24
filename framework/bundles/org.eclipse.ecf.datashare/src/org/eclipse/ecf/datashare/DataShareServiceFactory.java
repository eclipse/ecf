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
package org.eclipse.ecf.datashare;

import java.util.Hashtable;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.datashare.DataSharePlugin;

/**
 * @author pnehrer
 */
public class DataShareServiceFactory {

	public static final String TRACE_TAG = "DataShareServiceFactory";

	private static final Hashtable managers = new Hashtable();

	private DataShareServiceFactory() {
	}

	public static final IDataShareService getDataShareService(
			ISharedObjectContainer container, String name) throws ECFException {
		IDataShareServiceManager instantiator = (IDataShareServiceManager) managers
				.get(name);
		if (instantiator == null)
			return null;
		else
			return instantiator.getInstance(container);
	}

	public static void registerManager(String name,
			IDataShareServiceManager manager) {
		if (DataSharePlugin.isTracing(TRACE_TAG))
			DataSharePlugin.getTraceLog().println("registerManager: " + name);

		managers.put(name, manager);
	}

	public static void unregisterManager(String name) {
		if (DataSharePlugin.isTracing(TRACE_TAG))
			DataSharePlugin.getTraceLog().println("unregisterManager: " + name);

		managers.remove(name);
	}

	public static void unregisterAllManagers() {
		if (DataSharePlugin.isTracing(TRACE_TAG))
			DataSharePlugin.getTraceLog().println("unregisterAllManagers");

		managers.clear();
	}
}
