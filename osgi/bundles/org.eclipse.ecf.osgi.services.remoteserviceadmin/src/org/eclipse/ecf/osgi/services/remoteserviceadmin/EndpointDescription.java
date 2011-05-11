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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.IDUtil;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
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

	private String idNamespace;
	private ID containerID;
	private ID connectTargetID;
	private ID[] idFilter;
	private String rsFilter;

	private int hashCode = 7;
	private Map overrides;

	private void computeHashCode() {
		this.hashCode = 31 * this.hashCode + getId().hashCode();
		this.hashCode = 31 * this.hashCode
				+ new Long(getServiceId()).intValue();
		String frameworkUUID = getFrameworkUUID();
		this.hashCode = 31 * this.hashCode
				+ (frameworkUUID == null ? 0 : frameworkUUID.hashCode());
	}

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
		computeHashCode();
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
		computeHashCode();
	}

	private void verifyECFProperties() {
		this.idNamespace = verifyStringProperty(RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE);
		this.containerID = verifyIDProperty(idNamespace, getId());
		this.connectTargetID = verifyIDProperty(RemoteConstants.ENDPOINT_CONNECTTARGET_ID);
		this.idFilter = verifyIDFilter();
		this.rsFilter = verifyStringProperty(RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);
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
		List<String> interfaces = getInterfaces();
		Map<String, Version> result = new HashMap<String, Version>();
		for (String intf : interfaces) {
			int index = intf.lastIndexOf('.');
			if (index == -1) {
				continue;
			}
			String packageName = intf.substring(0, index);
			result.put(intf, getPackageVersion(packageName));
		}
		return result;
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

	public int hashCode() {
		return hashCode;
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (this == other)
			return true;
		if (!(other instanceof EndpointDescription))
			return false;
		EndpointDescription o = (EndpointDescription) other;
		String frameworkUUID = getFrameworkUUID();
		// equals returns true: 1) if getId() returns same String
		return getId().equals(o.getId())
		// 2) getServiceId() returns same value
				&& getServiceId() == o.getServiceId()
				// 3) a non-null frameworkUUID...and frameworkUUIDs equal
				&& (frameworkUUID == null ? true : frameworkUUID.equals(o
						.getFrameworkUUID()));
	}

	public String toString() {
		return "EndpointDescription[containerID=" + containerID //$NON-NLS-1$
				+ ",connectTargetID=" + connectTargetID + ",idFilter=" //$NON-NLS-1$ //$NON-NLS-2$
				+ Arrays.toString(idFilter) + ",rsFilter=" + rsFilter //$NON-NLS-1$
				+ ",properties=" + getProperties() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	void setPropertiesOverrides(Map propertiesOverrides) {
		this.overrides = PropertiesUtil.mergeProperties(super.getProperties(),
				propertiesOverrides);
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
}
