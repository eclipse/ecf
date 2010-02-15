/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.discovery;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * This interface describes an endpoint of a service. This class can be
 * considered as a wrapper around the property map of a published service and
 * its endpoint. It provides an API to conveniently access the most important
 * properties of the service.
 * <p>
 * <code>ServiceEndpointDescription</code> objects are immutable.
 * 
 * @Immutable
 * @version $Revision: 1.2 $
 * 
 * @deprecated This interface is deprecated because at the time of ECF
 *             3.0/Galileo release, it seems likely that this class will be
 *             moved, or renamed, or undergo major changes after the release of
 *             ECF 3.0. This deprecation is therefore intended as a notice to
 *             consumers about these upcoming changes in the RFC119
 *             specification, and the consequent changes to these OSGi-defined
 *             classes.
 */
public interface ServiceEndpointDescription {

	/**
	 * Returns the value of the property with key
	 * {@link ServicePublication#SERVICE_INTERFACE_NAME}.
	 * 
	 * @return <code>Collection (&lt;String&gt;)</code> of service interface
	 *         names provided by the advertised service endpoint. The collection
	 *         is never <code>null</code> or empty but contains at least one
	 *         service interface.
	 */
	Collection /* <String> */getProvidedInterfaces();

	/**
	 * Returns non-Java endpoint interface name associated with the given
	 * interface.
	 * <p>
	 * Value of the property with key
	 * {@link ServicePublication#ENDPOINT_INTERFACE_NAME} is used by this
	 * operation.
	 * 
	 * @param interfaceName
	 *            for which its non-Java endpoint interface name should be
	 *            returned.
	 * @return non-Java endpoint interface name, or <code>null</code> if it
	 *         hasn't been provided or if given interface name is
	 *         <code>null</code>.
	 */
	String getEndpointInterfaceName(String interfaceName);

	/**
	 * Returns version of the given interface.
	 * <p>
	 * Value of the property with key
	 * {@link ServicePublication#SERVICE_INTERFACE_VERSION} is used by this
	 * operation.
	 * 
	 * @param interfaceName
	 *            for which its version should be returned.
	 * @return Version of given service interface, or <code>null</code> if it
	 *         hasn't been provided or if given interface name is
	 *         <code>null</code>.
	 */
	String getVersion(String interfaceName);

	/**
	 * Returns the value of the property with key
	 * {@link ServicePublication#ENDPOINT_LOCATION}.
	 * 
	 * @return The url of the service location, or <code>null</code> if it
	 *         hasn't been provided.
	 */
	URI getLocation();

	/**
	 * Returns the value of the property with key
	 * {@link ServicePublication#ENDPOINT_ID}.
	 * 
	 * @return Unique id of service endpoint, or <code>null</code> if it hasn't
	 *         been provided.
	 */
	String getEndpointID();

	/**
	 * Getter method for the property value of a given key.
	 * 
	 * @param key
	 *            Name of the property
	 * @return The property value, or <code>null</code> if none is found for the
	 *         given key or if provided key is <code>null</code>.
	 */
	Object getProperty(String key);

	/**
	 * Returns all names of service endpoint properties.
	 * 
	 * @return a <code>Collection (&lt;String&gt;)</code> of property names
	 *         available in the ServiceEndpointDescription. The collection is
	 *         never <code>null</code> or empty but contains at least names of
	 *         mandatory <code>ServicePublication</code> properties. Since
	 *         <code>ServiceEndpointDescription</code> objects are immutable,
	 *         the returned collection is also not going to be updated at a
	 *         later point of time.
	 */
	Collection/* <String> */getPropertyKeys();

	/**
	 * Returns all service endpoint properties.
	 * 
	 * @return all properties of the service as a
	 *         <code>Map (&lt;String, Object&gt;)</code>. The map is never
	 *         <code>null</code> or empty but contains at least mandatory
	 *         <code>ServicePublication</code> properties. Since
	 *         <code>ServiceEndpointDescription</code> objects are immutable,
	 *         the returned map is also not going to be updated at a later point
	 *         of time.
	 */
	Map/* <String, Object> */getProperties();
}
