/****************************************************************************
 * Copyright (c) 2014 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.internal.remoteservice.java8;

import org.eclipse.ecf.core.sharedobject.ISharedObjectContainerConfig;
import org.eclipse.ecf.provider.generic.SSLClientSOContainer;

public class J8SSLClientSOContainer extends SSLClientSOContainer {

	public J8SSLClientSOContainer(ISharedObjectContainerConfig config, int ka) {
		super(config, ka);
	}
}