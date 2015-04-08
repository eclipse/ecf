/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.remoteservices.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionLocator;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.remoteserviceadmin.EndpointEvent;
import org.osgi.service.remoteserviceadmin.EndpointEventListener;

public class DiscoveryComponent implements EndpointEventListener {

	private static final String RSA_SYMBOLICNAME = "org.eclipse.ecf.osgi.services.remoteserviceadmin"; //$NON-NLS-1$

	private static DiscoveryComponent instance;

	private BundleContext context;
	private RemoteServiceAdmin rsa;

	void bindRemoteServiceAdmin(RemoteServiceAdmin r) {
		rsa = r;
	}

	void unbindRemoteServiceAdmin(RemoteServiceAdmin rsa) {
		rsa = null;
	}

	private EndpointDiscoveryView discoveryView;

	private IEndpointDescriptionLocator edLocator;

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

	void setView(EndpointDiscoveryView edv) {
		synchronized (this) {
			discoveryView = edv;
		}
	}

	RemoteServiceAdmin getRSA() {
		return rsa;
	}

	void activate(BundleContext context) throws Exception {
		history = new ArrayList<EndpointEvent>();
		synchronized (this) {
			instance = this;
			this.context = context;
		}
	}

	void deactivate() {
		synchronized (this) {
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

	void startRSA() throws BundleException {
		Bundle rsaBundle = null;
		BundleContext ctxt = null;
		synchronized (this) {
			ctxt = this.context;
			if (ctxt == null)
				return;
		}
		for (Bundle b : ctxt.getBundles())
			if (b.getSymbolicName().equals(RSA_SYMBOLICNAME))
				rsaBundle = b;
		if (rsaBundle == null)
			throw new BundleException(Messages.DiscoveryComponent_ERROR_MSG_CANNOT_FIND_RSA_BUNDLE);
		rsaBundle.start();
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

}
