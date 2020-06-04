/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.examples.updatesite;

import java.util.Properties;
import org.eclipse.ecf.discovery.IServiceProperties;

public class UpdateSiteProperties {
	String name;

	public static final String NAME_PROPERTY = "name"; //$NON-NLS-1$

	public UpdateSiteProperties(String name) {
		this.name = name;
	}

	public UpdateSiteProperties(IServiceProperties serviceProperties) {
		this.name = serviceProperties.getPropertyString(NAME_PROPERTY);
	}

	public String getName() {
		return name;
	}

	public Properties toProperties() {
		final Properties props = new Properties();
		props.put(NAME_PROPERTY, this.name);
		return props;
	}
}