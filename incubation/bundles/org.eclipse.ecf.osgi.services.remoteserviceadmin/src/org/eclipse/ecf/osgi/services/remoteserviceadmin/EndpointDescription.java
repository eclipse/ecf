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

	private long remoteServiceId;

	private String idNamespace;
	private ID connectTargetID;
	private ID[] idFilter;
	private String rsFilter;

	private int hashCode;

	public EndpointDescription(ServiceReference reference, Map osgiProperties) {
		super(reference, osgiProperties);
		this.remoteServiceId = verifyLongProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		this.idNamespace = verifyStringProperty(RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE);
		this.connectTargetID = verifyIDProperty(RemoteConstants.ENDPOINT_CONNECTTARGET_ID);
		this.idFilter = verifyIDFilter();
		this.rsFilter = verifyStringProperty(RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);
		computeHashCode();
	}

	public EndpointDescription(Map osgiProperties) {
		super(osgiProperties);
		this.remoteServiceId = verifyLongProperty(org.eclipse.ecf.remoteservice.Constants.SERVICE_ID);
		this.idNamespace = verifyStringProperty(RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE);
		this.connectTargetID = verifyIDProperty(RemoteConstants.ENDPOINT_CONNECTTARGET_ID);
		this.idFilter = verifyIDFilter();
		this.rsFilter = verifyStringProperty(RemoteConstants.ENDPOINT_REMOTESERVICE_FILTER);
		computeHashCode();
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
					"property value is not a Long: " + propName);
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
					"property value is not a String: " + propName);
			iae.initCause(e);
			throw iae;
		}
	}

	private ID verifyIDProperty(String namePropName) {
		String idName = verifyStringProperty(namePropName);
		if (idName == null)
			return null;
		try {
			return IDUtil.createID(idNamespace, idName);
		} catch (IDCreateException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"property value is not an ID: " + namePropName);
			iae.initCause(e);
			throw iae;
		}
	}

	private ID[] verifyIDFilter() {
		List<String> idNames = PropertiesUtil.getStringPlusProperty(
				getProperties(), RemoteConstants.ENDPOINT_IDFILTER_IDS);
		if (idNames.size() == 0)
			return null;
		List<ID> results = new ArrayList();
		String idNamespace = getIdNamespace();
		try {
			for (String idName : idNames)
				results.add(IDUtil.createID(idNamespace, idName));
		} catch (IDCreateException e) {
			IllegalArgumentException iae = new IllegalArgumentException(
					"property value is not an ID[]: "
							+ RemoteConstants.ENDPOINT_IDFILTER_IDS);
			iae.initCause(e);
			throw iae;

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
		return "ECFEndpointDescription[properties=" + super.toString()
				+ ",idNamespace=" + idNamespace + ", remoteServiceId="
				+ getRemoteServiceId() + ", connectTargetID=" + connectTargetID
				+ ", idFilter=" + Arrays.toString(idFilter) + ", rsFilter="
				+ rsFilter + ", hashCode=" + hashCode + "]";
	}

}
