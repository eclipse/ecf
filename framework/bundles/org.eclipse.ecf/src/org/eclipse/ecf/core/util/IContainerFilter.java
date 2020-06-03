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

package org.eclipse.ecf.core.util;

import org.eclipse.ecf.core.IContainer;

/**
 * Container filter contract.  Classes implementing this interface
 * will define specific rules for whether or not a the given container
 * matches some set of criteria...e.g. whether the container is
 * currently connected or not.
 */
public interface IContainerFilter {

	/**
	 * Match a given containerToMatch against some set of implementation-defined criteria.
	 * @param containerToMatch the containerToMatch.  Will not be <code>null</code>.
	 * @return boolean true if the given containerToMatch fulfills some
	 * implementation-dependent criteria.  false if not.
	 */
	public boolean match(IContainer containerToMatch);
}
