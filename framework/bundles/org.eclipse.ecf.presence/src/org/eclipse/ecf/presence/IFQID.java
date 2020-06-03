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

package org.eclipse.ecf.presence;

/**
 * ID adapter interface that supplies access to 'fully qualified' ID information.
 */
public interface IFQID {
	/**
	 * Get the fully qualified name.  Will not return <code>null</code>.  The result
	 * may be the same as ID.getName(), or may include additional information.  The
	 * result must be longer than or equal to ID.getName().
	 * @return String that is the fully qualified name.  Will not return <code>null</code>.  The result
	 * may be the same as ID.getName(), or may include additional information.  The
	 * result must be longer than or equal to ID.getName().
	 */
	public String getFQName();

	/**
	 * Get resource name.  May return <code>null</code>.
	 * @return String that is the resource for this IFQID.  May be <code>null</code>.
	 */
	public String getResourceName();
}
