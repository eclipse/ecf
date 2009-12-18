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
	 * Get the container factory names that are expected to be supported by the remote
	 * importing container.  Note that the returned values specify the container
	 * type name of the remote container...and that a container matching the given
	 * name should be able to communicate with the container represented by the given description.  <p>
	 * For example, for the 
	 * container type description with name: "ecf.generic.server", the result of 
	 * this method should be ["ecf.generic.client"], since the generic client is 
	 * the expected importer.</p><p> Another example is r-OSGi...since
	 * r-OSGi is peer based, the importer will be another peer, so the "ecf.r-osgi.peer"
	 * will respond with "ecf.r-osgi.peer".</p>
	 * 
	 * @param description the container type description for the local container.
	 * 
	 * @return String[] the container factory names of compatible remote containers
	 */
	public String[] getCompatibleRemoteContainerFactoryNames(ContainerTypeDescription description);

}
