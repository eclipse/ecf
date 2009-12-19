/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.core.provider;

import org.eclipse.ecf.core.ContainerTypeDescription;

/**
 * Interface that must be implemented by ECF remote service provider implementations.
 * @since 4.0
 * 
 */
public interface IRemoteServiceContainerInstantiator {

	/**
	 * Get supported configs for the container type handled by this instantiator.
	 * @param description the ContainerTypeDescription to return the supported configs for
	 * @return String[] the supported config types.
	 */
	public String[] getSupportedConfigTypes(ContainerTypeDescription description);

	/**
	 * Get supported intents for the container instantiated by this instantiator.
	 * 
	 * @param description the ContainerTypeDescription to return the intents for
	 * @return String[] supported intents.  <code>null</code> may be returned by
	 *         the provider if no intents are supported for this description.	 
	 */
	public String[] getSupportedIntents(ContainerTypeDescription description);

	/**
	 * Determine whether the given container type description is a remote service importer for 
	 * the given remote config type.  If the description is an importer for the given
	 * remoteConfigType, this method should return <code>true</code>, otherwise <code>false</code>.
	 * Implementations should only return <code>true</code> if a container of the given type
	 * can be created that will be able to communicate with the given remote container (of the
	 * given type).
	 * 
	 * @param description the local description under consideration.  Will not be <code>null</code>.
	 * @param remoteConfigType the remoteConfigType to consider.  Will not be <code>null</code>.
	 * @return <code>true</code> if the description represents a container capable of being a
	 * remote service importer for the given remoteConfigType, <code>false</code> otherwise.
	 */
	public boolean isImporterForRemoteConfigType(ContainerTypeDescription description, String remoteConfigType);
}
