/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.internal.remoteservice.rest;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethod;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.remoteservice.rest.IRestCall;
import org.eclipse.ecf.remoteservice.rest.resource.IRestResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class ResourceRepresentationFactory implements IAdaptable {
	
	private static ResourceRepresentationFactory factory;
	private List resources = new ArrayList();
	
	private class RestResourceTracker extends ServiceTracker {

		public RestResourceTracker(BundleContext context) {
			super(context, IRestResource.class.getName(), null);			
		}
		
		public Object addingService(ServiceReference reference) {
			Object service = context.getService(reference);
			if(service != null && service instanceof IRestResource) {
				if(!resources.contains(service))
					resources.add(service);
			}
			return service;
		}
		
		public void removedService(ServiceReference reference, Object service) {
			resources.remove(service);
			context.ungetService(reference);
		}
		
	}
	
	private ResourceRepresentationFactory() {
		
	}

	public static ResourceRepresentationFactory getDefault() {
		if(factory == null) {
			factory = new ResourceRepresentationFactory();
			factory.init();
		}
		return factory;
	}

	private void init() {
		Activator activator = Activator.getDefault();
		BundleContext context = activator.getContext();
		try {
			ServiceReference[] references = context.getServiceReferences(IRestResource.class.getName(), null);
			for (int i = 0; i < references.length; i++) {
				Object service = context.getService(references[i]);
				if(service instanceof IRestResource)
					resources.add((IRestResource)service);
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
		final ServiceTracker resourceTracker = new RestResourceTracker(context);
		resourceTracker.open();
		activator.getContext().addBundleListener(new BundleListener() {
			
			public void bundleChanged(BundleEvent event) {
				if(event.getType() == BundleEvent.STOPPING)
					resourceTracker.close();
			}
		});
	}

	/**
	 * Creates a resource representation for the resource defined in {@link IRestCall}'s 
	 * getEstimatedResourceIdentifier() Method. This will be compared with all
	 * registered services of the type {@link IRestResource} by calling their getIdentifier()
	 * methods. If a service matches the estimated identifier it's parse method will
	 * be invoked to parse the content of the resource.
	 */
	public Object createResourceRepresentation(HttpMethod method, IRestCall restCall) throws ParseException, IOException {
		for (int i = 0; i < resources.size(); i++) {
			IRestResource resource = (IRestResource)resources.get(i);			
			if(resource.getIdentifier().equals(restCall.getEstimatedResourceIdentifier()))
				return resource.createRepresentation(method.getResponseBodyAsString());
		}
		return null;
	}

	public Object getAdapter(Class adapter) {
		if(adapter == List.class)
			return resources;
		return null;
	}

}
