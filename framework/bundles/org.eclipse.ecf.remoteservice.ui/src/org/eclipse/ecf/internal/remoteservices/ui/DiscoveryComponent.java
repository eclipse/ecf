/****************************************************************************
 * Copyright (c) 2015 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.remoteservices.ui;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionLocator;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.ITopologyManager;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.eclipse.ecf.remoteserviceadmin.ui.endpoint.EndpointDiscoveryView;
import org.eclipse.ecf.remoteserviceadmin.ui.rsa.RemoteServiceAdminView;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.EndpointEvent;
import org.osgi.service.remoteserviceadmin.EndpointEventListener;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent;
import org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener;

public class DiscoveryComponent implements EndpointEventListener, RemoteServiceAdminListener {

	private static String ENDPOINT_LISTENER_SCOPE = System
			.getProperty("org.eclipse.ecf.remoteservices.ui.endpointListenerScope", "(ecf.endpoint.id.ns=*)");

	private static DiscoveryComponent instance;

	private BundleContext context;
	private RemoteServiceAdmin rsa;

	@SuppressWarnings("unused")
	private ITopologyManager tm;

	void bindRemoteServiceAdmin(org.osgi.service.remoteserviceadmin.RemoteServiceAdmin r) {
		rsa = (RemoteServiceAdmin) r;
	}

	void unbindRemoteServiceAdmin(org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa) {
		this.rsa = null;
	}

	void bindTopologyManager(ITopologyManager tm) {
		this.tm = tm;
	}

	void unbindTopologyManager(ITopologyManager tm) {
		this.tm = null;
	}

	public BundleContext getContext() {
		return this.context;
	}

	private EndpointDiscoveryView discoveryView;
	private RemoteServiceAdminView rsaView;

	private IEndpointDescriptionLocator edLocator;

	private ServiceRegistration<EndpointEventListener> eelRegistration;

	private ServiceRegistration<RemoteServiceAdminListener> rsaadminRegistration;

	void bindEndpointDescriptionLocator(IEndpointDescriptionLocator locator) {
		this.edLocator = locator;
	}

	void unbindEndpointDescriptionLocator(IEndpointDescriptionLocator locator) {
		this.edLocator = null;
	}

	public IEndpointDescriptionLocator getEndpointDescriptionLocator() {
		return this.edLocator;
	}

	public static DiscoveryComponent getDefault() {
		return instance;
	}

	public void setView(EndpointDiscoveryView edv) {
		synchronized (this) {
			discoveryView = edv;
		}
	}

	public RemoteServiceAdmin getRSA() {
		return rsa;
	}

	void activate(BundleContext context) throws Exception {
		history = new ArrayList<EndpointEvent>();
		synchronized (this) {
			instance = this;
			this.context = context;
			// Register this instance as an endpoint event listener
			Hashtable<String, Object> props = new Hashtable<String, Object>();
			props.put(EndpointEventListener.ENDPOINT_LISTENER_SCOPE, ENDPOINT_LISTENER_SCOPE);
			this.eelRegistration = context.registerService(EndpointEventListener.class, this, props);
			// register as remote service admin listener
			this.rsaadminRegistration = context.registerService(RemoteServiceAdminListener.class, this, null);
		}

	}

	void deactivate() {
		synchronized (this) {
			if (this.eelRegistration != null) {
				this.eelRegistration.unregister();
				this.eelRegistration = null;
			}
			if (this.rsaadminRegistration != null) {
				this.rsaadminRegistration.unregister();
				this.rsaadminRegistration = null;
			}
			instance = null;
			discoveryView = null;
			rsa = null;
			context = null;
			if (history != null) {
				history.clear();
				history = null;
			}
		}
	}

	private List<EndpointEvent> history;

	List<EndpointEvent> getHistory() {
		synchronized (this) {
			return history;
		}
	}

	@Override
	public void endpointChanged(EndpointEvent event, String filter) {
		EndpointDiscoveryView view = null;
		List<EndpointEvent> h = null;
		synchronized (this) {
			h = history;
			view = discoveryView;
		}
		if (view != null)
			view.handleEndpointChanged(event);
		else if (h != null)
			h.add(event);
	}

	@Override
	public void remoteAdminEvent(RemoteServiceAdminEvent event) {
		if (rsaView != null)
			rsaView.handleRSAEvent(event);
		if (discoveryView != null)
			discoveryView.handleRSAEent(event);
	}

	public void setRSAView(RemoteServiceAdminView rsaView) {
		this.rsaView = rsaView;
	}

}
