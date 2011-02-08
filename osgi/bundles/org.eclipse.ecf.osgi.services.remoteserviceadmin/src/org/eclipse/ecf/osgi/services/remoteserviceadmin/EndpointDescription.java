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
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * ECF endpoint description. This endpoint description extends the OSGi
 * {@link org.osgi.service.remoteserviceadmin.EndpointDescription} class to
 * provide access to meta-data for accessing an ECF remote services endpoint.
 * <p>
 * <p>
 * ECF remote service containers have some extra capabilities, that add endpoint
 * description meta-data. Specifically, the methods {@link #getContainerID()},
 * {@link #getIDFilter()}, {@link #getRemoteServiceFilter()} provide access
 * (respectively) to the target remote service container {@link ID}, an array of
 * {@link ID}s used to filter the lookup/search for remote service references,
 * and a remote service properties filter. These values are used in a call to
 * the consumer container adapter
 * {@link IRemoteServiceContainerAdapter#getRemoteServiceReferences(ID, ID[], String, String)}
 * with the containerID, idFilter, and remote service filter used as the 1st,
 * 2nd, and fourth parameters respectively.
 * <p>
 * <p>
 * UNDER CONSTRUCTION 
 * <p>
 * This meta-data is:
 * <ul>
 * <li>Namespace name of endpoint container id (String) - This is the namespace
 * name of the endpoint container ID. The id value is given by the OSGi
 * EndointDescription endpoint id (which is accessed via
 * {@link org.osgi.service.remoteserviceadmin.EndpointDescription#getId()}. The
 * namespace name is optional, as in many cases it can/will be dynamically
 * determined by examining the protocol for the endpoint id...i.e. the value
 * returned from
 * {@link org.osgi.service.remoteserviceadmin.EndpointDescription#getId()} (e.g.
 * 'ecftcp' in 'ecftcp://localhost:3282/server'). The value is read from the
 * initial properties, with name RemoteConstants.ENDPOINT_CONTAINER_ID_NAMESPACE
 * and type String</li>
 * <li>connectTargetID (String) - This is an optional target ID to connect to,
 * that is not necessarily the same as the endpoint id. For example, if the ECF
 * consumer container should connect to 'ecftcp://foo:3282/server' (a group
 * manager) and the host endpoint is a client group member that has id
 * 'ecftcp:A4rgterr8hyJJ99==' then the container can connect to the group, but
 * access the remote service that is exposed by one of the other group members.</li>
 * <li></li>
 * </ul>
 */
public class EndpointDescription extends
		org.osgi.service.remoteserviceadmin.EndpointDescription {

	private String idNamespace;
	private ID containerID;
	private ID connectTargetID;
	private ID[] idFilter;
	private String rsFilter;

	public EndpointDescription(ServiceReference reference, Map osgiProperties) {
		super(reference, osgiProperties);
		verifyECFProperties();
	}

	public EndpointDescription(Map osgiProperties) {
		super(osgiProperties);
		verifyECFProperties();
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

	public String toString() {
		return "ECFEndpointDescription[containerID=" + containerID //$NON-NLS-1$
				+ ",connectTargetID=" + connectTargetID + ",idFilter=" //$NON-NLS-1$ //$NON-NLS-2$
				+ Arrays.toString(idFilter) + ",rsFilter=" + rsFilter //$NON-NLS-1$
				+ ",properties=" + getProperties() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
