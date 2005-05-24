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
import java.util.Map;

/**
 * @author pnehrer
 */
public class UpdateProviderRegistry {

	private static final Hashtable providers = new Hashtable();

	private UpdateProviderRegistry() {
	}

	public static IUpdateProvider createProvider(String id, Map params) {
		IUpdateProviderFactory f = (IUpdateProviderFactory) providers.get(id);
		if (f == null)
			return null;
		else
			return f.createProvider(params);
	}

	public static void registerFactory(String id, IUpdateProviderFactory f) {
		providers.put(id, f);
	}

	public static void unregisterFactory(String id) {
		providers.remove(id);
	}

	public static void unregisterAllFactories() {
		providers.clear();
	}
}
