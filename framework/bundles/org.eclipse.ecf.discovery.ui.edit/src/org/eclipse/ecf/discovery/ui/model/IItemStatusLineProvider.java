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
package org.eclipse.ecf.discovery.ui.model;

public interface IItemStatusLineProvider {

	/**
	 * This fetches the status line text specific to this object instance.
	 * 
	 * @param object
	 * @return String the status line text
	 */
	public String getStatusLineText(Object object);
}
