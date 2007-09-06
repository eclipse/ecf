/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.internal.example.collab.ui;

import org.eclipse.ecf.discovery.IDiscoveryContainerAdapter;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.IServiceTypeListener;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.ui.views.DiscoveryView;
import org.eclipse.ecf.discovery.ui.views.IDiscoveryController;
import org.eclipse.ecf.internal.example.collab.ClientPlugin;

public class CollabDiscoveryView extends DiscoveryView {

	public static final String VIEW_ID = "org.eclipse.ecf.example.collab.discoveryview"; //$NON-NLS-1$

	protected static final int SERVICE_REQUEST_TIMEOUT = 3000;

	public CollabDiscoveryView() {
		super();
		setShowTypeDetails(false);
		this.setDiscoveryController(ClientPlugin.getDefault().getDiscoveryController());
	}

	public void setDiscoveryController(final IDiscoveryController controller) {
		super.setDiscoveryController(controller);
		if (controller != null) {
			final IDiscoveryContainerAdapter dc = controller.getDiscoveryContainer();
			if (dc != null) {
				// setup listeners
				dc.addServiceTypeListener(new IServiceTypeListener() {
					public void serviceTypeAdded(IServiceEvent event) {
						final IServiceID svcID = event.getServiceInfo().getServiceID();
						addServiceTypeInfo(svcID.getServiceTypeID().getName());
						dc.addServiceListener(event.getServiceInfo().getServiceID().getServiceTypeID(), new IServiceListener() {
							public void serviceAdded(IServiceEvent evt) {
								addServiceInfo(evt.getServiceInfo().getServiceID());
								dc.requestServiceInfo(evt.getServiceInfo().getServiceID(), SERVICE_REQUEST_TIMEOUT);
							}

							public void serviceRemoved(IServiceEvent evt) {
								removeServiceInfo(evt.getServiceInfo());
							}

							public void serviceResolved(IServiceEvent evt) {
								addServiceInfo(evt.getServiceInfo());
							}
						});
						dc.registerServiceType(svcID.getServiceTypeID());
					}
				});
			}
		}
	}

	public void dispose() {
		final IDiscoveryController c = getController();
		if (c != null && c.isDiscoveryStarted()) {
			c.stopDiscovery();
		}
		super.dispose();
	}
}
