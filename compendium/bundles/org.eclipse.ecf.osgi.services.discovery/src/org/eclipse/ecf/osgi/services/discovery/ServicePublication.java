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

import org.osgi.framework.ServiceReference;

/**
 * Register a service implementing the <code>ServicePublication</code> interface
 * in order to publish metadata of a particular service (endpoint) via
 * Discovery. Metadata which has to be published is given in form of properties
 * at registration.
 * <p>
 * In order to update published service metadata, update the properties
 * registered with the <code>ServicePublication</code> service. Depending on
 * Discovery's implementation and underlying protocol it may result in an update
 * or new re-publication of the service.
 * <p>
 * In order to unpublish the previously published service metadata, unregister
 * the <code>ServicePublication</code> service.
 * <p>
 * Please note that providing the {@link #SERVICE_INTERFACE_NAME} property is
 * mandatory when a <code>ServicePublication</code> service is registered. Note
 * also that a Discovery implementation may require provision of additional
 * properties, e.g. some of the standard properties defined below, or may make
 * special use of them in case they are provided. For example an SLP-based
 * Discovery might use the value provided with the {@link #ENDPOINT_LOCATION}
 * property for construction of a SLP-URL used to publish the service.
 * <p>
 * Also important is that it's not guaranteed that after registering a
 * <code>ServicePublication</code> object its service metadata is actually
 * published. Beside the fact that at least one Discovery service has to be
 * present, the provided properties have to be valid, e.g. shouldn't contain
 * case variants of the same key name, and the actual publication via Discovery
 * mechanisms has to succeed.
 * 
 * @ThreadSafe
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
public interface ServicePublication {

	/**
	 * Mandatory ServiceRegistration property which contains a collection of
	 * full qualified interface names offered by the advertised service
	 * endpoint.
	 * <p>
	 * Value of this property is of type
	 * <code>Collection (&lt;String&gt;)</code>.
	 */
	public static final String SERVICE_INTERFACE_NAME = "osgi.remote.service.interfaces"; //$NON-NLS-1$

	/**
	 * Optional ServiceRegistration property which contains a collection of
	 * interface names with their associated version attributes separated by
	 * {@link #SEPARATOR} e.g. ["my.company.foo|1.3.5", "my.company.zoo|2.3.5"].
	 * In case no version has been provided for an interface, Discovery may use
	 * the String-value of <code>org.osgi.framework.Version.emptyVersion</code>
	 * constant.
	 * <p>
	 * Value of this property is of type
	 * <code>Collection (&lt;String&gt;)</code>, may be <code>null</code> or
	 * empty.
	 */
	public static final String SERVICE_INTERFACE_VERSION = "osgi.remote.service.interfaces.version"; //$NON-NLS-1$

	/**
	 * Optional ServiceRegistration property which contains a collection of
	 * interface names with their associated (non-Java) endpoint interface names
	 * separated by {@link #SEPARATOR} e.g.:<br>
	 * ["my.company.foo|MyWebService", "my.company.zoo|MyWebService"].
	 * <p>
	 * This (non-Java) endpoint interface name is usually a communication
	 * protocol specific interface, for instance a web service interface name.
	 * Though this information is usually contained in accompanying properties
	 * e.g. a wsdl file, Discovery usually doesn't read and interprets such
	 * service meta-data. Providing this information explicitly, might allow
	 * external non-Java applications find services based on this endpoint
	 * interface.
	 * <p>
	 * Value of this property is of type
	 * <code>Collection (&lt;String&gt;)</code>, may be <code>null</code> or
	 * empty.
	 */
	public static final String ENDPOINT_INTERFACE_NAME = "osgi.remote.endpoint.interfaces"; //$NON-NLS-1$

	/**
	 * Optional ServiceRegistration property which contains a map of properties
	 * of the published service.
	 * <p>
	 * Property keys are handled in a case insensitive manner (as OSGi Framework
	 * does).
	 * <p>
	 * Value of this property is of type <code>Map (String, Object)</code>, may
	 * be <code>null</code> or empty.
	 */
	public static final String SERVICE_PROPERTIES = "osgi.remote.discovery.publication.service.properties"; //$NON-NLS-1$

	/**
	 * Optional property of the published service identifying its location. This
	 * property is provided as part of the service property map referenced by
	 * the {@link #SERVICE_PROPERTIES} ServiceRegistration property.
	 * <p>
	 * Value of this property is of type <code>java.net.URI</code>, may be
	 * <code>null</code>.
	 */
	public static final String ENDPOINT_LOCATION = "osgi.remote.endpoint.location"; //$NON-NLS-1$

	/**
	 * Optional property of the published service uniquely identifying its
	 * endpoint. This property is provided as part of the service property map
	 * referenced by the {@link #SERVICE_PROPERTIES} ServiceRegistration
	 * property.
	 * <p>
	 * Value of this property is of type <code>String</code>, may be
	 * <code>null</code>.
	 */
	public static final String ENDPOINT_ID = "osgi.remote.endpoint.id"; //$NON-NLS-1$

	/**
	 * Separator constant for association of interface-specific values with the
	 * particular interface name. See also {@link #SERVICE_INTERFACE_VERSION}
	 * and {@link #ENDPOINT_INTERFACE_NAME} properties which describe such
	 * interface-specific values.
	 */
	public static final String SEPARATOR = "|"; //$NON-NLS-1$

	/**
	 * Returns the <code>ServiceReference</code> this publication metadata is
	 * associated with.
	 * 
	 * @return the <code>ServiceReference</code> being published. Is never
	 *         <code>null</code>.
	 */
	ServiceReference getReference();
}
