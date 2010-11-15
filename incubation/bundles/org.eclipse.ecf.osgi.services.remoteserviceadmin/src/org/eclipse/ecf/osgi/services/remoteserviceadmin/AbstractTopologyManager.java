/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.osgi.framework.BundleContext;

public abstract class AbstractTopologyManager {

	protected BundleContext context;
	
	public AbstractTopologyManager(BundleContext context) {
		this.context = context;
	}
	public void close() {
		this.context = null;
	}
}
