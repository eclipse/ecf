/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.discovery;

import java.util.Map;
import org.eclipse.ecf.core.identity.ID;
import org.osgi.service.discovery.ServiceEndpointDescription;

public interface IRemoteServiceEndpointDescription extends
		ServiceEndpointDescription {

	/**
	 * Get the ECF endpoint ID (the ID of the endpoint that exposes the given
	 * remote service). This ID must not be <code>null</code>.
	 * 
	 * @return ID that is the ECF endpoint ID that exposes the service described
	 *         by this description.
	 */
	public ID getEndpointAsID();

	/**
	 * Get the ECF container target ID (the ID of the container that is the
	 * target to connect to). This may return
	 * <code>null<code>, meaning that no target
	 * ID is available, and that the endpoint ID returned from {@link #getEndpointAsID()}
	 * is also the target container ID.  If not <code>null</code>, the ID
	 * returned from this method may be used to connect to an intermediate
	 * target via IContainer connect.
	 * 
	 * @return ID that may be used by local IContainer to connect to remote
	 *         target.
	 */
	public ID getConnectTargetID();

	/**
	 * Get the ECF remote services filter string. May return <code>null</code>.
	 * 
	 * @return String that is to be used as the filter for the call to
	 *         getRemoteServicesReferences(ID,interface,filter);
	 */
	public String getRemoteServicesFilter();

	/**
	 * Set the properties for this endpoint description. This allows clients to
	 * examine and potentially change the service properties given by the
	 * service host (and delivered via discovery), and potentially change them
	 * via this method. The resulting properties will then be used in subsequent
	 * processing of this service endpoint description.
	 * 
	 * @param properties
	 *            the new set of properties. Must not be <code>null</code>.
	 */
	public void setProperties(Map properties);

	/**
	 * Get the remote service id for this service endpoint description.
	 * 
	 * @return long remote service id for the remote service.
	 */
	public long getRemoteServiceId();

}
