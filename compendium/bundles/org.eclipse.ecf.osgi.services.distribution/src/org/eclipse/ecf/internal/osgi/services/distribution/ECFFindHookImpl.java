/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.distribution;

import java.util.Collection;
import org.eclipse.ecf.core.util.Trace;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.service.FindHook;

public class ECFFindHookImpl implements FindHook {

	private final DistributionProviderImpl distributionProvider;

	public ECFFindHookImpl(DistributionProviderImpl distributionProvider) {
		this.distributionProvider = distributionProvider;
	}

	public void find(BundleContext context, String name, String filter,
			boolean allServices, Collection references) {
		Trace.entering(Activator.PLUGIN_ID, DebugOptions.FINDHOOKDEBUG, this
				.getClass(), "find", new Object[] {
				context.getBundle().getSymbolicName(), name, filter,
				new Boolean(allServices), references });
	}

}
