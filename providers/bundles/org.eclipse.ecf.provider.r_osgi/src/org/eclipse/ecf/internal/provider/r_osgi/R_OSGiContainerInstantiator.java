/****************************************************************************
 * Copyright (c) 2008 Jan S. Rellermeyer, and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Jan S. Rellermeyer - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.r_osgi;

import ch.ethz.iks.r_osgi.RemoteOSGiService;
import java.net.*;
import java.util.*;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.provider.*;
import org.eclipse.ecf.provider.r_osgi.identity.*;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

/**
 * The container instantiator creates new container instances of type
 * <i>ecf.r_osgi.peer</i> through the
 * <code>org.eclipse.ecf.ContainerFactory</code> extension point.
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 */
public final class R_OSGiContainerInstantiator implements IContainerInstantiator, IRemoteServiceContainerInstantiator {

	public static final String[] r_OSGiIntents = {"osgi.basic", "osgi.async", "osgi.private", "passByValue", "exactlyOnce", "ordered"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	public static R_OSGiID createR_OSGiID(Namespace namespace, String uriStr) {
		return (R_OSGiID) IDFactory.getDefault().createID(namespace, uriStr);
	}

	public static final String ID_PROP = "id"; //$NON-NLS-1$

	private static final String WS_PROTOCOL = "http"; //$NON-NLS-1$
	private static final String WSS_PROTOCOL = "https"; //$NON-NLS-1$
	private static final int WS_DEFAULT_PORT = 80;
	private static final int WSS_DEFAULT_PORT = 443;

	final boolean useHostname = Boolean.valueOf(System.getProperty("org.eclipse.ecf.provider.r_osgi.useHostName", "true")).booleanValue(); //$NON-NLS-1$ //$NON-NLS-2$

	private R_OSGiID createROSGiID(ContainerTypeDescription description, Map properties) throws ContainerCreateException {
		String idStr = null;
		if (properties != null)
			idStr = (String) properties.get(ID_PROP);
		String hostname = null;
		if (idStr != null) {
			try {
				URI uri = new URI(idStr);
				hostname = uri.getHost();
			} catch (URISyntaxException e) {
				throw new ContainerCreateException("Invalid syntax for R_OSGI id=" + idStr, e); //$NON-NLS-1$
			}
		}
		// See if private intent is set in properties
		boolean privateIntent = ContainerInstantiatorUtils.containsPrivateIntent(properties);

		InetAddress hostAddress = null;
		if (hostname == null || "".equals(hostname) || "localhost".equals(hostname)) { //$NON-NLS-1$ //$NON-NLS-2$
			hostname = "localhost"; //$NON-NLS-1$
			try {
				hostAddress = InetAddress.getLocalHost();
				if (useHostname)
					hostname = hostAddress.getCanonicalHostName();
			} catch (UnknownHostException e) {
				// If address can't be found for hostname, then if private, we throw
				// If not private we ignore
				if (privateIntent)
					throw new ContainerIntentException(Constants.OSGI_PRIVATE_INTENT, "Cannot get localhost address for private ROSGI container", e); //$NON-NLS-1$ 
			}
		} else if ("127.0.0.1".equals(hostname)) { //$NON-NLS-1$
			if (privateIntent)
				try {
					hostAddress = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					// If address can't be found for hostname, then if private, we throw
					// If not private we ignore
					throw new ContainerIntentException(Constants.OSGI_PRIVATE_INTENT, "Cannot get localhost address for private ROSGI container", e); //$NON-NLS-1$ 
				}
		}
		if (privateIntent) {
			if (hostAddress == null)
				try {
					hostAddress = InetAddress.getByName(hostname);
				} catch (UnknownHostException e) {
					throw new ContainerIntentException(Constants.OSGI_PRIVATE_INTENT, "Cannot get inetaddress for ROSGI container with hostname '" + hostname + "'", e); //$NON-NLS-1$ //$NON-NLS-2$ 
				}
			ContainerInstantiatorUtils.checkPrivate(hostAddress);
		}
		String descriptionName = description.getName();
		boolean wss = descriptionName.equals(ROSGI_WEBSOCKETSS_CONFIG);
		boolean ws = (descriptionName.equals(ROSGI_WEBSOCKETS_CONFIG) || wss);
		Namespace ns = (wss ? R_OSGiWSSNamespace.getDefault() : ((ws) ? R_OSGiWSNamespace.getDefault() : R_OSGiNamespace.getDefault()));

		final String nsScheme = ns.getScheme();
		final String wsProtocol = (wss ? WSS_PROTOCOL : (ws ? WS_PROTOCOL : null));
		final RemoteOSGiService remoteOSGiService = Activator.getDefault().getRemoteOSGiService();
		int listeningPort = remoteOSGiService.getListeningPort((wsProtocol != null) ? wsProtocol : nsScheme);
		int idPort = -1;
		if (WSS_PROTOCOL.equals(wsProtocol) && listeningPort != WSS_DEFAULT_PORT)
			idPort = listeningPort;
		else if (WS_PROTOCOL.equals(wsProtocol) && listeningPort != WS_DEFAULT_PORT)
			idPort = listeningPort;
		String portStr = (idPort > 0 ? (":" + idPort) : ""); //$NON-NLS-1$ //$NON-NLS-2$
		return createR_OSGiID(ns, new String(nsScheme + "://" + hostname + portStr)); //$NON-NLS-1$ 
	}

	/**
	 * creates a new container instance.
	 * 
	 * @param description
	 *            the container type description.
	 * @param parameters
	 *            the parameter passed to the container constructor.
	 * @return the factored container instance.
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#createInstance(org.eclipse.ecf.core.ContainerTypeDescription,
	 *      java.lang.Object[])
	 */
	public IContainer createInstance(final ContainerTypeDescription description, final Object[] parameters) throws ContainerCreateException {
		try {
			String descriptionName = description.getName();
			boolean wss = descriptionName.equals(ROSGI_WEBSOCKETSS_CONFIG);
			boolean ws = (descriptionName.equals(ROSGI_WEBSOCKETS_CONFIG) || wss);
			Namespace ns = (wss ? R_OSGiWSSNamespace.getDefault() : ((ws) ? R_OSGiWSNamespace.getDefault() : R_OSGiNamespace.getDefault()));

			ID containerID = null;
			if (parameters == null)
				containerID = createROSGiID(description, null);
			else if (parameters.length >= 1) {
				if (parameters[0] instanceof Map)
					containerID = createROSGiID(description, (Map) parameters[0]);
				else if (parameters[0] instanceof ID)
					containerID = (ID) parameters[0];
				else if (parameters[0] instanceof String)
					containerID = createR_OSGiID(ns, (String) parameters[0]);
			}
			if (containerID == null)
				throw new ContainerCreateException("Unsupported arguments " //$NON-NLS-1$
						+ Arrays.asList(parameters));
			final RemoteOSGiService remoteOSGiService = Activator.getDefault().getRemoteOSGiService();

			if (wss)
				return new R_OSGiWSSRemoteServiceContainer(remoteOSGiService, containerID);
			else if (ws)
				return new R_OSGiWSRemoteServiceContainer(remoteOSGiService, containerID);
			else
				return new R_OSGiRemoteServiceContainer(remoteOSGiService, containerID);
		} catch (ContainerCreateException e) {
			throw e;
		} catch (Exception e) {
			throw new ContainerCreateException("Could not create ROSGI Container instance", e); //$NON-NLS-1$
		}
	}

	/**
	 * get the adapter types that are supported by this container instantiator.
	 * 
	 * @param description
	 *            the container type description.
	 * @return a string array of the supported classes to which the factored
	 *         containers can provide adapters.
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedAdapterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public String[] getSupportedAdapterTypes(final ContainerTypeDescription description) {
		return new String[] {IRemoteServiceContainerAdapter.class.getName(), IContainer.class.getName()};
	}

	/**
	 * get the supported parameter types which the constructor of the container
	 * takes.
	 * 
	 * @param description
	 *            the container type description.
	 * @return an array of class arrays. Each array entry describes one sequence
	 *         of supported parameter.
	 * @see org.eclipse.ecf.core.provider.IContainerInstantiator#getSupportedParameterTypes(org.eclipse.ecf.core.ContainerTypeDescription)
	 */
	public Class[][] getSupportedParameterTypes(final ContainerTypeDescription description) {
		return new Class[][] {new Class[] {}, new Class[] {ID.class}};
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		List<String> intents = new ArrayList<String>(Arrays.asList(r_OSGiIntents));
		if (description.getName().equals(ROSGI_WEBSOCKETSS_CONFIG))
			intents.add(Constants.OSGI_CONFIDENTIAL_INTENT);
		return intents.toArray(new String[intents.size()]);
	}

	private static final String ROSGI_CONFIG = "ecf.r_osgi.peer"; //$NON-NLS-1$
	private static final String ROSGI_WEBSOCKETS_CONFIG = "ecf.r_osgi.peer.ws"; //$NON-NLS-1$
	private static final String ROSGI_WEBSOCKETSS_CONFIG = "ecf.r_osgi.peer.wss"; //$NON-NLS-1$
	public static final String NAME = ROSGI_CONFIG;
	public static final String NAME_HTTP = ROSGI_WEBSOCKETS_CONFIG;
	public static final String NAME_HTTPS = ROSGI_WEBSOCKETSS_CONFIG;

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		return new String[] {description.getName()};
	}

	public String[] getImportedConfigs(ContainerTypeDescription description, String[] exporterSupportedConfigs) {
		if (exporterSupportedConfigs == null)
			return null;
		List results = new ArrayList();
		for (int i = 0; i < exporterSupportedConfigs.length; i++) {
			if (exporterSupportedConfigs[i].equals(ROSGI_CONFIG))
				results.add(ROSGI_CONFIG);
			if (exporterSupportedConfigs[i].equals(ROSGI_WEBSOCKETS_CONFIG))
				results.add(ROSGI_WEBSOCKETS_CONFIG);
			if (exporterSupportedConfigs[i].equals(ROSGI_WEBSOCKETSS_CONFIG))
				results.add(ROSGI_WEBSOCKETSS_CONFIG);
		}
		return (results.size() == 0) ? null : (String[]) results.toArray(new String[results.size()]);
	}

	public Dictionary getPropertiesForImportedConfigs(ContainerTypeDescription description, String[] importedConfigs, Dictionary exportedProperties) {
		return null;
	}

}
