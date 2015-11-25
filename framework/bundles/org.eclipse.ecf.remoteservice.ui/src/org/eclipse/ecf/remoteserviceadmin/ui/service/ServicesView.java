/*******************************************************************************
 * Copyright (c) 2015 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Scott Lewis - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ecf.internal.remoteservices.ui.DiscoveryComponent;
import org.eclipse.ecf.remoteserviceadmin.ui.service.model.AbstractServicesContentProvider;
import org.eclipse.ecf.remoteserviceadmin.ui.service.model.ServicesContentProvider;
import org.eclipse.ecf.remoteserviceadmin.ui.service.model.ServiceNode;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IViewSite;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;

/**
 * @since 3.3
 */
public class ServicesView extends AbstractServicesView {

	public static final String ID_VIEW = "org.eclipse.ecf.remoteserviceadmin.ui.views.ServiceView"; //$NON-NLS-1$

	public ServicesView() {
	}

	@Override
	public void dispose() {
		super.dispose();
		DiscoveryComponent discovery = DiscoveryComponent.getDefault();
		if (discovery != null) {
			BundleContext ctxt = discovery.getContext();
			if (ctxt != null)
				ctxt.removeServiceListener(serviceListener);
			discovery = null;
		}
	}

	@Override
	protected void updateModel() {
		final BundleContext ctxt = DiscoveryComponent.getDefault().getContext();
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<ServiceNode> snds = new ArrayList<ServiceNode>();
				for (ServiceReferenceDTO sr : getServiceDTOs(ctxt))
					snds.add(createServiceNode(sr.id, sr.bundle, sr.usingBundles, sr.properties));
				addServiceNodes(snds);
			}
		}).start();
	}

	@Override
	protected AbstractServicesContentProvider createContentProvider(IViewSite viewSite) {
		return new ServicesContentProvider(viewSite);
	}

	private List<ServiceReferenceDTO> getServiceDTOs(BundleContext ctxt) {
		return ctxt.getBundle(0).adapt(FrameworkDTO.class).services;
	}

	private ServiceReferenceDTO getServiceDTO(BundleContext ctxt, ServiceReference<?> sr) {
		long serviceId = (Long) sr.getProperty(Constants.SERVICE_ID);
		for (ServiceReferenceDTO ref : getServiceDTOs(ctxt))
			if (serviceId == ref.id)
				return ref;
		return null;
	}

	private ServiceListener serviceListener = new ServiceListener() {
		@Override
		public void serviceChanged(ServiceEvent event) {
			if (viewer == null)
				return;
			BundleContext ctxt = DiscoveryComponent.getDefault().getContext();
			if (ctxt == null)
				return;
			ServiceReferenceDTO srDTO = getServiceDTO(ctxt, event.getServiceReference());
			if (srDTO != null)
				updateService(event.getType(), srDTO.id, srDTO.bundle, srDTO.usingBundles, srDTO.properties);
		}
	};

	@Override
	protected void setupListeners() {
		DiscoveryComponent.getDefault().getContext().addServiceListener(serviceListener);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
	}

	@Override
	protected void makeActions() {
	}

}
