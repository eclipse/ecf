/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.IDUtil;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IAsyncRemoteServiceProxy;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * ECF remote service endpoint description. Instances of this class, typically
 * created via discovery, allow the import of an ECF remote service. The
 * superclass of this class is the
 * {@link org.osgi.service.remoteserviceadmin.EndpointDescription} class which
 * is specified by the Remote Service Admin (chap 122) from the <a
 * href="http://www.osgi.org/download/r4v42/r4.enterprise.pdf">OSGi 4.2
 * Enterprise Specification</a>.
 * <p>
 * <p>
 * ECF remote services have capabilities beyond typical OSGi remote services. To
 * expose these capabilities, this EndpointDescription adds <b>optional</b>
 * meta-data. This meta-data may then be used by the remote service consumer to
 * customize ECF remote services import. Specifically, to customize the behavior
 * of the ECF implementation of
 * {@link RemoteServiceAdmin#importService(org.osgi.service.remoteserviceadmin.EndpointDescription)}.
 * <p>
 */
public class EndpointDescription extends
		org.osgi.service.remoteserviceadmin.EndpointDescription {

	private String ecfid;
	private Long timestamp;
	private String idNamespace;
	private ID containerID;
	private Long rsId;
	private List<String> asyncInterfaces;
	
	private ID connectTargetID;
	private ID[] idFilter;
	private String rsFilter;

	private Map overrides;

	/**
	 * 
	 * @param reference
	 *            A service reference that can be exported.
	 * @param properties
	 *            Map of properties. This argument can be <code>null</code>. The
	 *            keys in the map must be type <code>String</code> and, since
	 *            the keys are case insensitive, there must be no duplicates
	 *            with case variation.
	 * @throws IllegalArgumentException
	 *             When the properties are not proper for an Endpoint
	 *             Description
	 * 
	 * @see org.osgi.service.remoteserviceadmin.EndpointDescription#EndpointDescription(ServiceReference,
	 *      Map)
	 */
	public EndpointDescription(final ServiceReference reference,
			final Map<String, Object> properties) {
		super(reference, properties);
		verifyECFProperties();
	}

	/**
	 * 
	 * @param properties
	 *            The map from which to create the Endpoint Description. The
	 *            keys in the map must be type <code>String</code> and, since
	 *            the keys are case insensitive, there must be no duplicates
	 *            with case variation.
	 * @throws IllegalArgumentException
	 *             When the properties are not proper for an Endpoint
	 *             Description.
	 * 
	 * @see org.osgi.service.remoteserviceadmin.EndpointDescription#EndpointDescription(Map)
	 */
	public EndpointDescription(Map<String, Object> properties) {
		super(properties);
		verifyECFProperties();
	}

	private void verifyECFProperties() {
		this.ecfid = verifyStringProperty(RemoteConstants.ENDPOINT_ID);
		if (this.ecfid == null) {
			LogUtility
					.logWarning(
							"verifyECFProperties", DebugOptions.ENDPOINT_DESCRIPTION_READER, EndpointDescription.class, "ECFEndpointDescription property " + RemoteConstants.ENDPOINT_ID + " not set.  Using OSGI endpoint.id value"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			this.ecfid = getId();
		}
		this.timestamp = verifyLongProperty(RemoteConstants.ENDPOINT_TIMESTAMP);
		if (this.timestamp == null) {
			LogUtility
					.logWarning(
							"verifyECFProperties", DebugOptions.ENDPOINT_DESCRIPTION_READER, EndpointDescription.class, "ECFEndpointDescription property " + RemoteConstants.ENDPOINT_TIMESTAMP + " not set.  Using OSGI endpoint.service.id"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			this.timestamp = getServiceId();
		}
		this.idNamespace = verifyStringProperty(RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE);
		this.containerID = verifyIDProperty(idNamespace, this.ecfid);
		this.rsId = verifyLongProperty(Constants.SERVICE_ID);
			// if null, then set to service.id
		if (this.rsId == null) 
				this.rsId = getServiceId();
		
		this.connectTargetID = verifyIDProperty(RemoteConstants.ENDPOINT_CONNECTTARGET_ID);
		this.idFilter = verifyIDFilter();
		this.rsFilter = verifyStringProperty(RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);
		
		this.asyncInterfaces = verifyAsyncInterfaces();
	}

	private List<String> verifyAsyncInterfaces() {
		// Check to see that async proxy has not been disabled
		List<String> resultInterfaces = new ArrayList<String>();
		Object noAsyncProxy = getProperties().get(Constants.SERVICE_PREVENT_ASYNCPROXY);
		if (noAsyncProxy == null) {
			// Get service.exported.async.objectClass property value
			Object asyncObjectClass = getProperties().get(
					RemoteConstants.SERVICE_EXPORTED_ASYNC_INTERFACES);
			// If present
			if (asyncObjectClass != null) {
				List<String> originalInterfaces = getInterfaces();
				String[] matchingInterfaces = PropertiesUtil
						.getMatchingInterfaces(
								originalInterfaces
										.toArray(new String[originalInterfaces
												.size()]), asyncObjectClass);
				if (matchingInterfaces != null)
					for (int i = 0; i < matchingInterfaces.length; i++) {
						String asyncInterface = convertInterfaceToAsync(matchingInterfaces[i]);
						if (asyncInterface != null
								&& !resultInterfaces.contains(asyncInterface))
							resultInterfaces.add(asyncInterface);
					}
			}
		}
		return Collections.unmodifiableList(resultInterfaces);
	}
	
	private Long verifyLongProperty(String propName) {
		Object r = getProperties().get(propName);
		try {
			return (Long) r;
		} catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"property value is not a Long: " + propName); //$NON-NLS-1$
			iae.initCause(e);
			throw iae;
		}
	}

	private String verifyStringProperty(String propName) {
		Object r = getProperties().get(propName);
		try {
			return (String) r;
		} catch (ClassCastException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"property value is not a String: " + propName); //$NON-NLS-1$
			iae.initCause(e);
			throw iae;
		}
	}

	private ID verifyIDProperty(String idNamespace, String idName) {
		if (idName == null)
			return null;
		try {
			return IDUtil.createID(idNamespace, idName);
		} catch (IDCreateException e) {
			return IDFactory.getDefault().createStringID(idName);
		}
	}

	private ID verifyIDProperty(String namePropName) {
		return verifyIDProperty(idNamespace, verifyStringProperty(namePropName));
	}

	private ID[] verifyIDFilter() {
		List<String> idNames = PropertiesUtil.getStringPlusProperty(
				getProperties(), RemoteConstants.ENDPOINT_IDFILTER_IDS);
		if (idNames.size() == 0)
			return null;
		List<ID> results = new ArrayList();
		String idNamespace = getIdNamespace();
		for (String idName : idNames) {
			try {
				results.add(IDUtil.createID(idNamespace, idName));
			} catch (IDCreateException e) {
				IllegalArgumentException iae = new IllegalArgumentException(
						"cannot create ID[]: idNamespace=" + idNamespace //$NON-NLS-1$
								+ " idName=" + idName); //$NON-NLS-1$
				iae.initCause(e);
				throw iae;
			}
		}
		return (ID[]) results.toArray(new ID[results.size()]);
	}

	private void addInterfaceVersions(List<String> interfaces, Map<String,Version> result) {
		if (interfaces == null) return;
		for (String intf : interfaces) {
			int index = intf.lastIndexOf('.');
			if (index == -1) continue;
			String packageName = intf.substring(0, index);
			result.put(intf, getPackageVersion(packageName));
		}
	}
	/**
	 * Get a map of the service interface name -> Version information for all
	 * the service interfaces exposed by this endpoint description (i.e. those
	 * returned by {@link #getInterfaces()} which have a
	 * 
	 * @return Map<String,Version> of interface versions for all our service
	 *         interfaces. Every service interface returned by
	 *         {@link #getInterfaces()} will have an associated Version, but it
	 *         may have value {@value Version#emptyVersion}
	 */
	public Map<String, Version> getInterfaceVersions() {
		Map<String, Version> result = new HashMap<String, Version>();
		addInterfaceVersions(getInterfaces(),result);
		addInterfaceVersions(getAsyncInterfaces(),result);
		return result;
	}

	/**
	 * @since 4.0
	 */
	public String getEndpointId() {
		return ecfid;
	}
	
	/**
	 * @since 4.0
	 */
	public Long getTimestamp() {
		return this.timestamp;
	}
	
	/**
	 * @since 4.0
	 */
	public Long getRemoteServiceId() {
		return this.rsId;
	}
	
	public ID getContainerID() {
		return containerID;
	}

	public String getIdNamespace() {
		return idNamespace;
	}

	public ID getConnectTargetID() {
		return connectTargetID;
	}

	public ID[] getIDFilter() {
		return idFilter;
	}

	public String getRemoteServiceFilter() {
		return rsFilter;
	}

	void setPropertiesOverrides(Map propertiesOverrides) {
		this.overrides = PropertiesUtil.mergeProperties(super.getProperties(),
				propertiesOverrides);
	}

	@Override
	public boolean isSameService(
			org.osgi.service.remoteserviceadmin.EndpointDescription other) {
		// If same ed instance then they are for same service
		if (this == other)
			return true;
		// Like superclass, check to see that the framework id is not null
		String frameworkId = getFrameworkUUID();
		if (frameworkId == null)
			return false;
		// The id, the service id and the frameworkid have to be identical
		// to be considered the same service
		return (getId().equals(other.getId())
				&& getServiceId() == other.getServiceId() && frameworkId
					.equals(other.getFrameworkUUID()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.remoteserviceadmin.EndpointDescription#getProperties()
	 */
	@Override
	public Map<String, Object> getProperties() {
		if (overrides != null)
			return overrides;
		return super.getProperties();
	}

	private String convertInterfaceToAsync(String interfaceName) {
		if (interfaceName == null)
			return null;
		String asyncProxyName = (String) getProperties().get(Constants.SERVICE_ASYNC_RSPROXY_CLASS_ + interfaceName);
		if (asyncProxyName != null)
			return asyncProxyName;
		if (interfaceName.endsWith(IAsyncRemoteServiceProxy.ASYNC_INTERFACE_SUFFIX))
			return interfaceName;
		return interfaceName + IAsyncRemoteServiceProxy.ASYNC_INTERFACE_SUFFIX;
	}
	
	/**
	 * @since 4.0
	 */
	public List<String> getAsyncInterfaces() {
		return asyncInterfaces;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("ECFEndpointDescription["); //$NON-NLS-1$
		sb.append(getProperties()).append("]"); //$NON-NLS-1$
		return sb.toString();
	}
}
