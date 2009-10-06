/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This

 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Andre Dietisheim - support declaratively registered resource representations
 *******************************************************************************/
package org.eclipse.ecf.internal.remoteservice.rest;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethod;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.remoteservice.rest.IRestCall;
import org.eclipse.ecf.remoteservice.rest.IRestResourceRepresentationFactory;
import org.eclipse.ecf.remoteservice.rest.resource.IRestResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A factory for creating ResourceRepresentation objects.
 */
public class ResourceRepresentationFactory implements IRestResourceRepresentationFactory, IAdaptable {

	/** The resources. */
	private List resources = new ArrayList();

	/**
	 * The Class RestResourceTracker.
	 */
	private class RestResourceTracker extends ServiceTracker {

		/**
		 * Instantiates a new rest resource tracker.
		 * 
		 * @param context
		 *            the context
		 */
		public RestResourceTracker(BundleContext context) {
			super(context, IRestResource.class.getName(), null);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework
		 * .ServiceReference)
		 */
		public Object addingService(ServiceReference reference) {
			Object service = context.getService(reference);
			if (service instanceof IRestResource) {
				addResourceRepresentation((IRestResource) service);
			}
			return service;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework
		 * .ServiceReference, java.lang.Object)
		 */
		public void removedService(ServiceReference reference, Object service) {
			if (service instanceof IRestResource) {
				removeResourceRepresentation((IRestResource) service);
				context.ungetService(reference);
			}
		}
	}

	/**
	 * Instantiates a new resource representation factory.
	 */
	public ResourceRepresentationFactory() {
		init();
	}

	/**
	 * Inits the.
	 */
	private void init() {
		Activator activator = Activator.getDefault();
		BundleContext context = activator.getContext();
		try {
			addResourceRepresentations(context);
		} catch (InvalidSyntaxException e) {
			// ignore
		}
		initServiceTracker(activator, context);
	}

	/**
	 * Inits the service tracker.
	 * 
	 * @param activator
	 *            the activator
	 * @param context
	 *            the context
	 */
	private void initServiceTracker(Activator activator, BundleContext context) {
		final ServiceTracker resourceTracker = new RestResourceTracker(context);
		resourceTracker.open();

		activator.getContext().addBundleListener(new BundleListener() {

			public void bundleChanged(BundleEvent event) {
				if (event.getType() == BundleEvent.STOPPING)
					resourceTracker.close();
			}
		});
	}

	/**
	 * Adds the resource representations.
	 * 
	 * @param context
	 *            the context
	 * 
	 * @throws InvalidSyntaxException
	 *             the invalid syntax exception
	 */
	private void addResourceRepresentations(BundleContext context) throws InvalidSyntaxException {
		ServiceReference[] references = context.getServiceReferences(IRestResource.class.getName(), null);
		if (references != null) {
			for (int i = 0; i < references.length; i++) {
				Object service = context.getService(references[i]);
				if (service instanceof IRestResource)
					addResourceRepresentation((IRestResource) service);
			}
		}
	}

	/**
	 * Creates a resource representation for the resource defined in
	 * {@link IRestCall}'s getEstimatedResourceIdentifier() Method. This will be
	 * compared with all registered services of the type {@link IRestResource}
	 * by calling their getIdentifier() methods. If a service matches the
	 * estimated identifier it's parse method will be invoked to parse the
	 * content of the resource.
	 */
	public Object createResourceRepresentation(HttpMethod method, IRestCall restCall) throws ParseException,
			IOException {
		for (int i = 0; i < resources.size(); i++) {
			IRestResource resource = (IRestResource) resources.get(i);
			if (resource.getIdentifier().equals(restCall.getEstimatedResourceIdentifier()))
				return resource.createRepresentation(method.getResponseBodyAsString());
		}
		return null;
	}

	/**
	 * Adds the resource representation.
	 * 
	 * @param restResource
	 *            the rest resource
	 */
	private void addResourceRepresentation(IRestResource restResource) {
		synchronized (resources) {
			if (!resources.contains(restResource)) {
				this.resources.add(restResource);
			}
		}
	}

	/**
	 * Removes the resource representation.
	 * 
	 * @param restResource
	 *            the rest resource
	 */
	private void removeResourceRepresentation(IRestResource restResource) {
		synchronized (resources) {
			this.resources.remove(restResource);
		}
	}

	public Object getAdapter(Class adapter) {
		if(adapter == List.class)
			return resources;
		return null;
	}
}
