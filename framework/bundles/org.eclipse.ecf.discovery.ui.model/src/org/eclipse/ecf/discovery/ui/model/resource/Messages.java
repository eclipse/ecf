/****************************************************************************
 * Copyright (c) 2008 Versant Corp. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Versant Corp. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.discovery.ui.model.resource;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ecf.discovery.ui.model.resource.messages"; //$NON-NLS-1$
	public static String ServiceResource_NO_DISCOVERY_CONTAINER_AVAILABLE;
	public static String ServiceResource_NoEMFServiceModel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {}
}
