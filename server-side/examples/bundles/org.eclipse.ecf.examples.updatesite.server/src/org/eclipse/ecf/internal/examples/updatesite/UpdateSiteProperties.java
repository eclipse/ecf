/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.examples.updatesite;

import java.util.Properties;
import org.eclipse.ecf.discovery.IServiceProperties;

public class UpdateSiteProperties {
	String path;
	String name;

	public static final String PATH_PROPERTY = "path"; //$NON-NLS-1$
	public static final String NAME_PROPERTY = "name"; //$NON-NLS-1$

	public UpdateSiteProperties(String name, String path) {
		this.path = path;
		this.name = name;
	}

	public UpdateSiteProperties(IServiceProperties serviceProperties) {
		this.path = serviceProperties.getPropertyString(PATH_PROPERTY);
		this.name = serviceProperties.getPropertyString(NAME_PROPERTY);
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public Properties toProperties() {
		final Properties props = new Properties();
		props.put(PATH_PROPERTY, this.path);
		props.put(NAME_PROPERTY, this.name);
		return props;
	}
}