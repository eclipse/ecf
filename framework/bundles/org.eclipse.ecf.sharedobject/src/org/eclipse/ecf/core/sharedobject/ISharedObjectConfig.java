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
import org.eclipse.ecf.core.identity.ID;

/**
 * Configuration information provided to ISharedObject via its enclosing
 * ISharedObjectContainer
 * 
 * @see ISharedObject#init(ISharedObjectConfig)
 */
public interface ISharedObjectConfig {
	/**
	 * Get the ID associated with this ISharedObject by its container.
	 * Containers must provide an implementation of this configuration that
	 * provides a non-null ID instance in response to this method call.
	 * 
	 * @return ID that ISharedObject can use for imlementing its own
	 *         ISharedObject.getID(). Will not be null.
	 */
	public ID getSharedObjectID();

	/**
	 * Get the ID of the container that is the home of the primary copy of the
	 * ISharedObject instance.
	 * 
	 * @return the ID of the container that is the home of the primary copy of
	 *         the ISharedObject instance. Will not be null.
	 */
	public ID getHomeContainerID();

	/**
	 * Get the ISharedObjectContext instance for this ISharedObject. The
	 * ISharedObjectContext provides access to container-provided services,
	 * including messaging to remote containers and to remote replicas of the
	 * ISharedObject, as well as access to OSGI-platform services.
	 * 
	 * @return ISharedObjectContext for the ISharedObject to use to access
	 *         container and associated services. <b>Will</b> return null if
	 *         context is no longer valid.
	 */
	public ISharedObjectContext getContext();

	/**
	 * Get properties associated with with this ISharedObject
	 * 
	 * @return Map with properties associated with this ISharedObject instance.
	 *         Will not be null.
	 */
	public Map<String, ?> getProperties();
}