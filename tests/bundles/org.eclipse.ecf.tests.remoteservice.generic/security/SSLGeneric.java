/****************************************************************************
 * Copyright (c) 2013 Composent, Inc. and others.
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

package org.eclipse.ecf.tests.remoteservice.generic;

/**
 *
 */
public interface SSLGeneric {
	public static final String CONSUMER_CONTAINER_TYPE = "ecf.generic.ssl.client";
	public static final String HOST_CONTAINER_TYPE = "ecf.generic.ssl.server";
	public static final String HOST_CONTAINER_ENDPOINT_ID = "ecfssl://localhost:{0}/secureserver";
}
