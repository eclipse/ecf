/****************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.provider.ContainerInstantiatorUtils;
import org.eclipse.ecf.core.provider.ContainerIntentException;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.IDUtil;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;
import org.osgi.framework.ServiceReference;

/**
 * Abstract superclass for host container selectors...i.e. implementers of
 * {@link IHostContainerSelector}.
 * @since 4.6
 * 
 */
public abstract class AbstractHostContainerSelector extends
		AbstractContainerSelector {

	private static final String REQUIRE_SERVER_PROP = "org.eclipse.ecf.osgi.service.remoteserviceadmin.hostcontainerselector.requireserver"; //$NON-NLS-1$
	private static final String EXCLUDED_DESCRIPTIONS_PROP = "org.eclipse.ecf.osgi.service.remoteserviceadmin.hostcontainerselector.excludeddescriptions"; //$NON-NLS-1$
	// Default is now to require that ContainerTypeDesription.isServer() returns true
	// If this is set to false (via system property REQUIRE_SERVER_PROP being set to false, or at runtime via
	// setRequireServer(false), then the description isServer() will not be considered when a 
	// container type description is considered for export
	private boolean requireServer = new Boolean(System.getProperty(REQUIRE_SERVER_PROP,"true")); //$NON-NLS-1$
	// It's possible to exclude container type descriptions from comsideration for export by
	// setting excludedDescriptions.  This can be done by setting the system property EXCLUDED_DESCRIPTIONS_PROP
	// to a comma-separated list of container type description names...e.g. -Dorg.eclipse.ecf.osgi.service.remoteserviceadmin.hostcontainerselector.excludeddescriptions=ecf.generic.client,ecf.xmlrpc.client
	private List<String> excludedDescriptions;
	
	protected String[] defaultConfigTypes;

	/**
	 * @since 4.6
	 */
	protected void setExcludedDescriptions(List<String> excludedDescriptions) {
		this.excludedDescriptions = (excludedDescriptions == null)?Collections.EMPTY_LIST:excludedDescriptions;
	}
	
	/**
	 * @since 4.6
	 */
	protected List<String> getExcludedDescriptions() {
		return this.excludedDescriptions;
	}
	
	/**
	 * @since 4.6
	 */
	protected void setRequireServer(boolean requireServerDescriptionForExport) {
		this.requireServer = requireServerDescriptionForExport;
	}
	
	/**
	 * @since 4.6
	 */
	protected boolean getRequireServerDescription() {
		return this.requireServer;
	}
	
	public AbstractHostContainerSelector(String[] defaultConfigTypes) {
		this.defaultConfigTypes = defaultConfigTypes;
		String propValue = System.getProperty(EXCLUDED_DESCRIPTIONS_PROP);
		String[] excludedVals = (propValue==null)?new String[0]:propValue.trim().split(","); //$NON-NLS-1$
		setExcludedDescriptions(Arrays.asList(excludedVals));
	}

	/**
	 * @param serviceReference service reference
	 * @param overridingProperties overriding properties
	 * @param serviceExportedInterfaces service exported interfaces to select for
	 * @param serviceExportedConfigs service exported configs to select for
	 * @param serviceIntents service exported intents to select for 
	 * @return Collection of existing host containers
	 * @since 2.0
	 */
	protected Collection selectExistingHostContainers(
			ServiceReference serviceReference,
			Map<String, Object> overridingProperties,
			String[] serviceExportedInterfaces,
			String[] serviceExportedConfigs, String[] serviceIntents) {
		List results = new ArrayList();
		// Get all existing containers
		IContainer[] containers = getContainers();
		// If nothing there, then return empty array
		if (containers == null || containers.length == 0)
			return results;

		for (int i = 0; i < containers.length; i++) {
			ID cID = containers[i].getID();
			trace("selectExistingHostContainers","Considering existing container="+cID); //$NON-NLS-1$ //$NON-NLS-2$
			// Check to make sure it's a rs container adapter. If it's not go
			// onto next one
			IRemoteServiceContainerAdapter adapter = hasRemoteServiceContainerAdapter(containers[i]);
			if (adapter == null) {
				trace("selectExistingHostContainers","Existing container="+cID+" does not implement IRemoteServiceContainerAdapter"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				continue;
			}
			// Get container type description and intents
			ContainerTypeDescription description = getContainerTypeDescription(containers[i]);
			// If it has no description go onto next
			if (description == null) {
				trace("selectExistingHostContainers","Existing container="+cID+" does not have container type description"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				continue;
			}
			// http://bugs.eclipse.org/331532
			if (!description.isServer()) {
				trace("selectExistingHostContainers","Existing container="+cID+" is not server"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				continue;
			}

			if (matchExistingHostContainer(serviceReference,
					overridingProperties, containers[i], adapter, description,
					serviceExportedConfigs, serviceIntents)) {
				trace("selectExistingHostContainers", "INCLUDING containerID=" //$NON-NLS-1$ //$NON-NLS-2$
						+ containers[i].getID()
						+ " configs=" //$NON-NLS-1$
						+ ((serviceExportedConfigs == null) ? "null" : Arrays //$NON-NLS-1$
								.asList(serviceExportedConfigs).toString())
						+ " intents=" //$NON-NLS-1$
						+ ((serviceIntents == null) ? "null" : Arrays.asList( //$NON-NLS-1$
								serviceIntents).toString()));
				results.add(new RemoteServiceContainer(containers[i], adapter));
			} else {
				trace("selectExistingHostContainers", "EXCLUDING containerID=" //$NON-NLS-1$ //$NON-NLS-2$
						+ containers[i].getID()
						+ " configs=" //$NON-NLS-1$
						+ ((serviceExportedConfigs == null) ? "null" : Arrays //$NON-NLS-1$
								.asList(serviceExportedConfigs).toString())
						+ " intents=" //$NON-NLS-1$
						+ ((serviceIntents == null) ? "null" : Arrays.asList( //$NON-NLS-1$
								serviceIntents).toString()));
			}
		}
		return results;
	}

	/**
	 * @param serviceReference serviceReference
	 * @param properties properties
	 * @param container container to match
	 * @return boolean true if match false otherwise
	 * @since 2.0
	 */
	protected boolean matchHostContainerToConnectTarget(
			ServiceReference serviceReference, Map<String, Object> properties,
			IContainer container) {
		String target = (String) properties
				.get(RemoteConstants.ENDPOINT_CONNECTTARGET_ID);
		if (target == null)
			return true;
		// If a targetID is specified, make sure it either matches what the
		// container
		// is already connected to, or that we connect an unconnected container
		ID connectedID = container.getConnectedID();
		// If the container is not already connected to anything
		// then we connect it to the given target
		if (connectedID == null) {
			// connect to the target and we have a match
			try {
				connectHostContainer(serviceReference, properties, container,
						target);
			} catch (Exception e) {
				logException("doConnectContainer containerID=" //$NON-NLS-1$
						+ container.getID() + " target=" + target, e); //$NON-NLS-1$
				return false;
			}
			return true;
		} else {
			ID targetID = createTargetID(container, target);
			// We check here if the currently connectedID equals the target.
			// If it does we have a match
			if (connectedID.equals(targetID))
				return true;
		}
		return false;
	}

	/**
	 * @param serviceReference service reference
	 * @param properties properties
	 * @param container container
	 * @param adapter remote service container adapter
	 * @param description container type description
	 * @param requiredConfigTypes required config types
	 * @param requiredServiceIntents required service intents
	 * @return boolean true if match, false otherwise
	 * @since 2.0
	 */
	protected boolean matchExistingHostContainer(
			ServiceReference serviceReference, Map<String, Object> properties,
			IContainer container, IRemoteServiceContainerAdapter adapter,
			ContainerTypeDescription description, String[] requiredConfigTypes,
			String[] requiredServiceIntents) {

		return matchRequireServer(description) && matchNotExcluded(description)
				&& matchHostSupportedConfigTypes(requiredConfigTypes, description)
				&& matchHostSupportedIntents(requiredServiceIntents, description, container)
				&& matchHostContainerID(serviceReference, properties, container)
				&& matchHostContainerToConnectTarget(serviceReference, properties, container);
	}

	/**
	 * @param serviceReference serviceReference
	 * @param properties properties
	 * @param container container
	 * @return boolean true if match, false otherwise
	 * @since 2.0
	 */
	protected boolean matchHostContainerID(ServiceReference serviceReference,
			Map<String, Object> properties, IContainer container) {

		ID containerID = container.getID();
		// No match if the container has no ID
		if (containerID == null)
			return false;

		// Then get containerid if specified directly by user in properties
		ID requiredContainerID = (ID) properties
				.get(RemoteConstants.SERVICE_EXPORTED_CONTAINER_ID);
		// If the CONTAINER_I
		if (requiredContainerID != null) {
			return requiredContainerID.equals(containerID);
		}
		// Else get the container factory arguments, create an ID from the
		// arguments
		// and check if the ID matches that
		Namespace ns = containerID.getNamespace();
		Object cid = properties
				.get(RemoteConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGS);
		// If no arguments are present, then any container ID should match
		if (cid == null)
			return true;
		ID cID = null;
		if (cid instanceof ID) {
			cID = (ID) cid;
		} else if (cid instanceof String) {
			cID = IDUtil.createID(ns, (String) cid);
		} else if (cid instanceof Object[]) {
			Object cido = ((Object[]) cid)[0];
			cID = IDUtil.createID(ns, new Object[] { cido });
		}
		if (cID == null)
			return true;
		return containerID.equals(cID);
	}

	/**
	 * @param requiredConfigTypes request config types
	 * @param containerTypeDescription container type description
	 * @return boolean true if match, false otherwise
	 */
	protected boolean matchHostSupportedConfigTypes(
			String[] requiredConfigTypes,
			ContainerTypeDescription containerTypeDescription) {
		// if no config type is set the spec requires to create a default
		// endpoint (see section 122.5.1)
		if (requiredConfigTypes == null)
			return true;
		trace("matchHostSupportedConfigTypes","description="+containerTypeDescription.getName()+" testing for requiredConfigTypes"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Get supported config types for this description
		String[] supportedConfigTypes = getSupportedConfigTypes(containerTypeDescription);
		// If it doesn't support anything, return false
		if (supportedConfigTypes == null || supportedConfigTypes.length == 0) {
			trace("matchHostSupportedConfigTypes","No supported configs found for description="+containerTypeDescription.getName()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
		// Turn supported config types for this description into list
		List supportedConfigTypesList = Arrays.asList(supportedConfigTypes);
		List requiredConfigTypesList = Arrays.asList(requiredConfigTypes);
		// We check all of the required config types and make sure
		// that one or more of them are present in the supportedConfigTypes
		boolean result = false;
		for (Iterator i = requiredConfigTypesList.iterator(); i.hasNext();)
			result |= supportedConfigTypesList.contains(i.next());
		if (!result)
			trace("matchHostSupportedConfigTypes","description="+containerTypeDescription.getName()+" does not support all required config types"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return result;
	}

	/**
	 * @param serviceReference service reference
	 * @param properties overriding properties
	 * @param serviceExportedInterfaces service exported interfaces to select for
	 * @param requiredConfigs service exported configs to select for
	 * @param requiredIntents intents to select for 
	 * @return Collection of host containers
	 * @throws SelectContainerException if container cannot be created or configured
	 * @since 2.0
	 */
	protected Collection createAndConfigureHostContainers(
			ServiceReference serviceReference, Map<String, Object> properties,
			String[] serviceExportedInterfaces, String[] requiredConfigs,
			String[] serviceIntents) throws SelectContainerException {

		List results = new ArrayList();
		ContainerTypeDescription[] descriptions = getContainerTypeDescriptions();
		if (descriptions == null)
			return Collections.EMPTY_LIST;
			// Iterate through all descriptions and see if we have a match
			for (int i = 0; i < descriptions.length; i++) {
				trace("createAndConfigureHostContainers","Considering description="+descriptions[i]); //$NON-NLS-1$ //$NON-NLS-2$
				IRemoteServiceContainer matchingContainer = createMatchingContainer(
						descriptions[i], serviceReference, properties,
						serviceExportedInterfaces, requiredConfigs,
						serviceIntents);
				if (matchingContainer != null)
					results.add(matchingContainer);
			}
		return results;
	}

	protected ContainerTypeDescription[] getContainerTypeDescriptionsForDefaultConfigTypes(
			ContainerTypeDescription[] descriptions) {
		String[] defaultConfigTypes = getDefaultConfigTypes();
		if (defaultConfigTypes == null || defaultConfigTypes.length == 0)
			return null;
		List results = new ArrayList();
		for (int i = 0; i < descriptions.length; i++) {
			// For each description, get supported config types
			String[] supportedConfigTypes = descriptions[i]
					.getSupportedConfigs();
			if (supportedConfigTypes != null
					&& matchDefaultConfigTypes(defaultConfigTypes,
							supportedConfigTypes))
				results.add(descriptions[i]);
		}
		return (ContainerTypeDescription[]) results
				.toArray(new ContainerTypeDescription[] {});
	}

	protected boolean matchDefaultConfigTypes(String[] defaultConfigTypes,
			String[] supportedConfigTypes) {
		List supportedConfigTypesList = Arrays.asList(supportedConfigTypes);
		for (int i = 0; i < defaultConfigTypes.length; i++) {
			if (supportedConfigTypesList.contains(defaultConfigTypes[i]))
				return true;
		}
		return false;
	}

	protected String[] getDefaultConfigTypes() {
		return defaultConfigTypes;
	}

	/**
	 * @since 4.6
	 */
	protected boolean matchRequireServer(ContainerTypeDescription description) {
		boolean result = false;
		boolean require = getRequireServerDescription();
		if (require) {
			result = description.isServer();
			LogUtility.trace("matchRequireServer", DebugOptions.CONTAINER_SELECTOR, this.getClass(), //$NON-NLS-1$
					"Server is required for export, so description=" + description.getName() //$NON-NLS-1$
							+ ((result) ? " isServer() return true" : " IS NOT SERVER")); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			result = true;
			LogUtility.trace("matchRequireServer", DebugOptions.CONTAINER_SELECTOR, this.getClass(), //$NON-NLS-1$
					"Server is not required for export, so description=" + description.getName() //$NON-NLS-1$
							+ " is allowed to export"); //$NON-NLS-1$
			return true;
		}
		return result;
	}
	
	/**
	 * @since 4.6
	 */
	protected boolean matchNotExcluded(ContainerTypeDescription description) {
		boolean result = getExcludedDescriptions().contains(description.getName());
		LogUtility.trace("matchNotExcluded", DebugOptions.CONTAINER_SELECTOR, this.getClass(), "description="+description.getName()+((result)?" EXCLUDED via excludedDescriptions="+this.excludedDescriptions:" not excluded via excludedDescriptions="+this.excludedDescriptions)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		return !result;
	}
	/**
	 * @param containerTypeDescription containerTypeDescription
	 * @param serviceReference reference
	 * @param properties properties
	 * @param serviceExportedInterfaces exported interfaces
	 * @param requiredConfigs configs
	 * @param requiredIntents intents
	 * @return IRemoteServiceContainer matching container created
	 * @throws SelectContainerException container cannot be created or selected
	 * @since 2.0
	 */
	protected IRemoteServiceContainer createMatchingContainer(
			ContainerTypeDescription containerTypeDescription,
			ServiceReference serviceReference, Map<String, Object> properties,
			String[] serviceExportedInterfaces, String[] requiredConfigs,
			String[] serviceIntents) throws SelectContainerException {

		if (matchRequireServer(containerTypeDescription) && matchNotExcluded(containerTypeDescription)
				&& matchHostSupportedConfigTypes(requiredConfigs, containerTypeDescription)
				&& matchHostSupportedIntents(serviceIntents, containerTypeDescription)) {
			return createRSContainer(serviceReference, properties, containerTypeDescription, serviceIntents);
		}
		return null;
	}

	/**
	 * @param serviceReference serviceReference
	 * @param properties properties
	 * @param containerTypeDescription container type description
	 * @return IRemoteServiceContainer created remote service container
	 * @throws SelectContainerException if could not be created
	 * @since 2.0
	 */
	protected IRemoteServiceContainer createRSContainer(
			ServiceReference serviceReference, Map<String, Object> properties,
			ContainerTypeDescription containerTypeDescription)
			throws SelectContainerException {
		return createRSContainer(serviceReference, properties, containerTypeDescription, null);
	}

	/**
	 * @param serviceReference serviceReference
	 * @param properties properties
	 * @param containerTypeDescription container type description
	 * @return IRemoteServiceContainer created remote service container
	 * @throws SelectContainerException if could not be created
	 * @since 4.6
	 */
	protected IRemoteServiceContainer createRSContainer(
			ServiceReference serviceReference, Map<String, Object> properties,
			ContainerTypeDescription containerTypeDescription, String[] intents)
			throws SelectContainerException {
		trace("createRSContainer", //$NON-NLS-1$
				"Creating container instance for ref=" + serviceReference + ";properties=" + properties //$NON-NLS-1$ //$NON-NLS-2$
						+ ";description=" + containerTypeDescription.getName() + ";intents=" //$NON-NLS-1$ //$NON-NLS-2$
						+ ((intents == null) ? "" : Arrays.asList(intents).toString())); //$NON-NLS-1$
		IContainer container = createContainer(serviceReference, properties,
				containerTypeDescription, intents);
		if (container == null) 
			return null;
		IRemoteServiceContainerAdapter adapter = (IRemoteServiceContainerAdapter) container
				.getAdapter(IRemoteServiceContainerAdapter.class);
		if (adapter == null) {
			LogUtility.logError("createRSContainer", DebugOptions.CONTAINER_SELECTOR, this.getClass(), "Container="+container.getID()+" does not implement IRemoteServiceContainerAdapter"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return null;
		}
		return new RemoteServiceContainer(container, adapter);
	}


	/**
	 * @param serviceReference service reference
	 * @param properties properties
	 * @param container container
	 * @param target target
	 * @throws ContainerConnectException if container cannot be connected
	 * @throws IDCreateException thrown if ID cannot be created
	 * @since 2.0
	 */
	protected void connectHostContainer(ServiceReference serviceReference,
			Map<String, Object> properties, IContainer container, Object target)
			throws ContainerConnectException, IDCreateException {
		ID targetID = (target instanceof String) ? IDUtil.createID(
				container.getConnectNamespace(), (String) target) : IDUtil
				.createID(container.getConnectNamespace(),
						new Object[] { target });
		Object context = properties
				.get(RemoteConstants.SERVICE_EXPORTED_CONTAINER_CONNECT_CONTEXT);
		IConnectContext connectContext = null;
		if (context != null) {
			connectContext = createConnectContext(serviceReference, properties,
					container, context);
		}
		// connect the container
		container.connect(targetID, connectContext);
	}

	protected boolean matchHostSupportedIntents(
			String[] serviceRequiredIntents,
			ContainerTypeDescription containerTypeDescription) {
		return matchHostSupportedIntents(serviceRequiredIntents, containerTypeDescription, null);
	}

	/**
	 * @since 4.6
	 */
	protected boolean matchHostSupportedIntents(
			String[] serviceRequiredIntents,
			ContainerTypeDescription containerTypeDescription, IContainer container) {
		// If there are no required intents then we have a match
		if (serviceRequiredIntents == null)
			return true;

		trace("matchHostSupportedIntents","description="+containerTypeDescription.getName()+" testing for serviceRequiredIntents="+Arrays.asList(serviceRequiredIntents)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		String[] supportedIntents = getSupportedIntents(containerTypeDescription);

		if (supportedIntents == null) {
			trace("matchHostSupportedIntents","description="+containerTypeDescription.getName()+" does not have any supported intents"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}
		// checks to see that containerTypeDescription supports requiredIntents
		boolean result = true;
		for (int i = 0; i < serviceRequiredIntents.length; i++) {
			boolean found = false;
			for(String supportedIntent: supportedIntents) 
				if (serviceRequiredIntents[i].equals(supportedIntent) || serviceRequiredIntents[i].startsWith(supportedIntent+"."))  {//$NON-NLS-1$
					found = true;
					break;
				}
			result &= found;
		}

		if (!result) {
			trace("matchHostSupportedIntents","description="+containerTypeDescription.getName()+" does not have all required intents"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}
		
		// If container is non-null,
		// check to see that it's ID is private.  If it's not private, return null
		if (container != null) {
			try {
				if (ContainerInstantiatorUtils.containsPrivateIntent(serviceRequiredIntents))
					ContainerInstantiatorUtils.checkPrivate(container.getID());
			} catch (ContainerIntentException e) {
				trace("matchHostSupportedIntents","container="+container.getID()+" does not have osgi private intent"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return false;
			}
		}
		
		if (!result) {
			trace("matchHostSupportedIntents","container="+container.getID()+" does not have all required intents"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return result;
	}

}
