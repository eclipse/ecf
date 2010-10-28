/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.remoteserviceadmin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IDiscoveryLocator;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants;
import org.eclipse.ecf.tests.ECFAbstractTestCase;
import org.osgi.framework.Constants;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractMetadataFactoryTest extends ECFAbstractTestCase {

	private static final String DEFAULT_ENDPOINT_ID = "ecftcp://localhost:3282/server";
	private static final String DEFAULT_SERVICE_IMPORTED_CONFIG = "ecf.generic.server";
	protected IDiscoveryAdvertiser discoveryAdvertiser;
	protected IDiscoveryLocator discoveryLocator;

	protected IDiscoveryLocator getDiscoveryLocator() {
		ServiceTracker serviceTracker = new ServiceTracker(Activator.getContext(),IDiscoveryLocator.class.getName(), null);
		serviceTracker.open();
		IDiscoveryLocator result = (IDiscoveryLocator) serviceTracker.getService();
		serviceTracker.close();
		return result;
	}
	
	protected IDiscoveryAdvertiser getDiscoveryAdvertiser() {
		ServiceTracker serviceTracker = new ServiceTracker(Activator.getContext(),IDiscoveryAdvertiser.class.getName(), null);
		serviceTracker.open();
		IDiscoveryAdvertiser result = (IDiscoveryAdvertiser) serviceTracker.getService();
		serviceTracker.close();
		return result;
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		discoveryAdvertiser = null;
		discoveryLocator = null;
		super.tearDown();
	}
	
	protected String[] createOSGiObjectClass() {
		return new String[] { "com.foo.IFoo" };
	}
	
	protected String createOSGiEndpointFrameworkUUID() {
		return UUID.randomUUID().toString();
	}
	
	protected String createOSGiEndpointId() {
		return DEFAULT_ENDPOINT_ID;
	}
	
	protected Long createOSGiEndpointServiceId() {
		return new Long(1);
	}
	
	protected EndpointDescription createRequiredEndpointDescription() {
		Map<String,Object> props = new HashMap<String,Object>();
		// Add required OSGi properties
		addRequiredOSGiProperties(props);
		// Add required ECF properties
		addRequiredECFProperties(props);
		return new EndpointDescription(props);
	}
	
	protected String createOSGiServiceImportedConfig() {
		return DEFAULT_SERVICE_IMPORTED_CONFIG;
	}
	
	protected ID createECFContainerID(Map<String,Object> props) {
		return getIDFactory().createStringID(createOSGiEndpointId());
	}
	
	protected Long createECFRemoteServiceId(Map<String,Object> props) {
		return new Long(101);
	}
	
	protected void addRequiredOSGiProperties(Map<String,Object> props) {
		// OBJECTCLASS
		props.put(Constants.OBJECTCLASS,createOSGiObjectClass());
		// endpoint.service.id
		props.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_SERVICE_ID, createOSGiEndpointServiceId());
		// endpoint.framework.id
		props.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_FRAMEWORK_UUID, createOSGiEndpointFrameworkUUID());
		// endpoint.id
		props.put(org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID, createOSGiEndpointId());
		// service imported configs
		props.put(org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_IMPORTED_CONFIGS,createOSGiServiceImportedConfig());
	}
	
	
	protected void addRequiredECFProperties(Map<String,Object> props) {
		// ecf.endpoint.id
		props.put(RemoteConstants.ENDPOINT_ID,createECFContainerID(props));
		// ecf.endpoint.
		props.put(RemoteConstants.ENDPOINT_REMOTESERVICE_ID, createECFRemoteServiceId(props));
	}


}
