/****************************************************************************
 * Copyright (c) 2009 EclipseSource and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.util;

import org.eclipse.ecf.core.identity.IIDFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Service tracker customized to handle tracking the ECF id factory service
 * (singleton).
 * 
 * @since 3.1
 */
@SuppressWarnings("rawtypes")
public class IDFactoryTracker extends ServiceTracker {

	@SuppressWarnings("unchecked")
	public IDFactoryTracker(BundleContext context) {
		super(context, IIDFactory.class.getName(), null);
	}

	public IIDFactory getIDFactory() {
		return (IIDFactory) getService();
	}

}
