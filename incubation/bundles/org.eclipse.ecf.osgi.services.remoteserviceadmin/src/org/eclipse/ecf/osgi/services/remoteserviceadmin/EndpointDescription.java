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
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.IDUtil;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.PropertiesUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

public class EndpointDescription extends
		org.osgi.service.remoteserviceadmin.EndpointDescription {

	private String idNamespace;
	private ID containerID;
	private long remoteServiceId;
	private ID connectTargetID;
	private ID[] idFilter;
	private String rsFilter;

	private int hashCode;

	public EndpointDescription(ServiceReference reference, Map osgiProperties) {
		super(reference, osgiProperties);
		verifyECFProperties();
		computeHashCode();
	}

	public EndpointDescription(Map osgiProperties) {
		super(osgiProperties);
		verifyECFProperties();
		computeHashCode();
	}

	private void verifyECFProperties() {
		this.idNamespace = verifyStringProperty(RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE);
		this.containerID = verifyIDProperty(idNamespace, getId());
		this.remoteServiceId = verifyLongProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		this.connectTargetID = verifyIDProperty(RemoteConstants.ENDPOINT_CONNECTTARGET_ID);
		this.idFilter = verifyIDFilter();
		this.rsFilter = verifyStringProperty(RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);
	}

	private long verifyLongProperty(String propName) {
		Object r = getProperties().get(propName);
		if (r == null) {
			return 0l;
		}
		try {
			return ((Long) r).longValue();
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
			IllegalArgumentException iae = new IllegalArgumentException(
					"cannot create a valid ID: idNamespace=" + idNamespace //$NON-NLS-1$
							+ ", idName=" + idName); //$NON-NLS-1$
			iae.initCause(e);
			throw iae;
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

	private void computeHashCode() {
		this.hashCode = super.hashCode();
		long remoteServiceId = getRemoteServiceId();
		this.hashCode = 31 * hashCode
				+ (int) (remoteServiceId ^ (remoteServiceId >>> 32));
	}

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

	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (other == this)
			return true;
		if (!(other instanceof EndpointDescription))
			return false;
		EndpointDescription o = (EndpointDescription) other;
		return super.equals(other)
				&& (o.getRemoteServiceId() == this.getRemoteServiceId());
	}

	public int hashCode() {
		return hashCode;
	}

	public boolean isSameService(
			org.osgi.service.remoteserviceadmin.EndpointDescription other) {
		if (!(other instanceof EndpointDescription))
			return false;
		EndpointDescription o = (EndpointDescription) other;
		return (super.isSameService(other) && o.getRemoteServiceId() == this
				.getRemoteServiceId());
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

	public long getRemoteServiceId() {
		return remoteServiceId;
	}

	public ID[] getIDFilter() {
		return idFilter;
	}

	public String getRemoteServiceFilter() {
		return rsFilter;
	}

	public String toString() {
		return "ECFEndpointDescription[containerID=" + containerID //$NON-NLS-1$
				+ ", remoteServiceId=" + getRemoteServiceId() //$NON-NLS-1$
				+ ", connectTargetID=" + connectTargetID + ", idFilter=" //$NON-NLS-1$ //$NON-NLS-2$
				+ Arrays.toString(idFilter) + ", rsFilter=" + rsFilter //$NON-NLS-1$
				+ ", hashCode=" + hashCode + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
