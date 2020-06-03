/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.sharedobject;

import java.util.Map;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.IIdentifiable;

/**
 * Configuration information associated with ISharedObjectContainer.
 * 
 */
public interface ISharedObjectContainerConfig extends IIdentifiable, IAdaptable {
	/**
	 * The properties associated with the owner ISharedObjectContainer
	 * 
	 * @return Map the properties associated with owner ISharedObjectContainer
	 */
	public Map<String, ?> getProperties();

}