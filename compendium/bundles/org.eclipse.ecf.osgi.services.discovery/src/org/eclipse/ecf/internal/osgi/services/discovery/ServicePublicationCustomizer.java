/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.internal.osgi.services.discovery;

import org.osgi.framework.ServiceReference;
import org.osgi.service.discovery.ServicePublication;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ServicePublicationCustomizer implements ServiceTrackerCustomizer {

	public Object addingService(ServiceReference reference) {
		ServicePublication servicePublication = (ServicePublication) Activator.getDefault().getContext().getService(reference);
		addServicePublication(reference,servicePublication);
		return servicePublication;
	}

	private void addServicePublication(ServiceReference reference,
			ServicePublication servicePublication) {
		// TODO Auto-generated method stub
		
	}

	public void modifiedService(ServiceReference reference, Object service) {
	}

	public void removedService(ServiceReference reference, Object service) {
		removeServicePublication(reference,(ServicePublication) service);
	}

	private void removeServicePublication(ServiceReference reference,
			ServicePublication service) {
		// TODO Auto-generated method stub
		
	}

}
