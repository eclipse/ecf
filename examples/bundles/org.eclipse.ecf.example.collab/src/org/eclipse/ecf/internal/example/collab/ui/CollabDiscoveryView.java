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
import org.eclipse.ecf.discovery.IServiceTypeEvent;
import org.eclipse.ecf.discovery.IServiceTypeListener;
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
					public void serviceTypeDiscovered(IServiceTypeEvent event) {
						addServiceTypeInfo(event.getServiceTypeID().getName());
						dc.addServiceListener(event.getServiceTypeID(), new IServiceListener() {
							public void serviceDiscovered(IServiceEvent anEvent) {
								addServiceInfo(anEvent.getServiceInfo().getServiceID());
								addServiceInfo(anEvent.getServiceInfo());
							}

							public void serviceUndiscovered(IServiceEvent anEvent) {
								removeServiceInfo(anEvent.getServiceInfo());
							}
						});
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
