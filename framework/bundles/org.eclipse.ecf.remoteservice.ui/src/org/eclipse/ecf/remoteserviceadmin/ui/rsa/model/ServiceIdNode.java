/****************************************************************************
 * Copyright (c) 2015 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.rsa.model;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * @since 3.3
 */
public class ServiceIdNode extends NameValuePropertyNode {

	public ServiceIdNode(ServiceReference sr, String labelPrefix) {
		super(Constants.SERVICE_ID, sr.getProperty(Constants.SERVICE_ID));
		setPropertyAlias(labelPrefix);
	}

	public ServiceIdNode(long sid, String labelPrefix) {
		super(Constants.SERVICE_ID, sid);
		setPropertyAlias(labelPrefix);
	}
}
