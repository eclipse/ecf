/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import java.util.Collection;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ExportRegistration;
import org.osgi.service.remoteserviceadmin.ImportReference;
import org.osgi.service.remoteserviceadmin.ImportRegistration;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdmin;

public class RemoteServiceAdminImpl implements RemoteServiceAdmin {

	private BundleContext context;
	private TopologyManagerImpl topologyManager;

	public RemoteServiceAdminImpl(BundleContext context,
			TopologyManagerImpl topologyManager) {
		this.context = context;
		this.topologyManager = topologyManager;
	}

	public Collection<ExportRegistration> exportService(
			ServiceReference reference, Map<String, Object> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	public ImportRegistration importService(EndpointDescription endpoint) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<ExportReference> getExportedServices() {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<ImportReference> getImportedEndpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() {
		
		this.topologyManager = null;
		this.context = null;
	}
}
