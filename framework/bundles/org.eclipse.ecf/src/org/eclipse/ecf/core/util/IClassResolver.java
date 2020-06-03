/****************************************************************************
 * Copyright (c) 2015 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.util;

import java.io.ObjectStreamClass;

/**
 * @since 3.7
 */
public interface IClassResolver {

	public static final String BUNDLE_PROP_NAME = "org.eclipse.ecf.core.util.classresolver.bundleSymbolicName"; //$NON-NLS-1$

	public Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException;
}
