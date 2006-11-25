/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;

public class RemoteServiceRegistryImpl implements Serializable {

	private static long nextServiceId = 0L;

	private static final long serialVersionUID = -291866447335444599L;

	protected static final String REMOTEOBJECTCLASS = "remoteObjectClass";

	protected static final String REMOTESERVICE_ID = "remote.service.id";

	protected static final String REMOTESERVICE_RANKING = "remote.service.ranking";

	protected ID containerID;

	public RemoteServiceRegistryImpl() {
	}

	/**
	 * Published services by class name. Key is a String class name; Value is a
	 * ArrayList of IRemoteServiceRegistrations
	 */
	protected HashMap publishedServicesByClass = new HashMap(50);

	/** All published services */
	protected ArrayList allPublishedServices = new ArrayList(50);

	public RemoteServiceRegistryImpl(ID localContainerID) {
		this();
		this.containerID = localContainerID;
	}

	protected long getNextServiceId() {
		return nextServiceId++;
	}

	public ID getContainerID() {
		return containerID;
	}

	public void publishService(RemoteServiceRegistrationImpl serviceReg) {

		// Add the ServiceRegistration to the list of Services published by
		// Class Name.
		String[] clazzes = (String[]) serviceReg.getReference().getProperty(
				REMOTEOBJECTCLASS);
		int size = clazzes.length;

		for (int i = 0; i < size; i++) {
			String clazz = clazzes[i];

			ArrayList services = (ArrayList) publishedServicesByClass
					.get(clazz);

			if (services == null) {
				services = new ArrayList(10);
				publishedServicesByClass.put(clazz, services);
			}

			services.add(serviceReg);
		}

		// Add the ServiceRegistration to the list of all published Services.
		allPublishedServices.add(serviceReg);
	}

	public void unpublishService(RemoteServiceRegistrationImpl serviceReg) {

		// Remove the ServiceRegistration from the list of Services published by
		// Class Name.
		String[] clazzes = (String[]) serviceReg.getReference().getProperty(
				REMOTEOBJECTCLASS);
		int size = clazzes.length;

		for (int i = 0; i < size; i++) {
			String clazz = clazzes[i];
			ArrayList services = (ArrayList) publishedServicesByClass
					.get(clazz);
			services.remove(serviceReg);
		}

		// Remove the ServiceRegistration from the list of all published
		// Services.
		allPublishedServices.remove(serviceReg);

	}

	public void unpublishServices() {
		publishedServicesByClass.clear();
		allPublishedServices.clear();
	}

	public IRemoteServiceReference[] lookupServiceReferences(String clazz,
			IRemoteFilter filter) {
		int size;
		ArrayList references;
		ArrayList serviceRegs;
		if (clazz == null) /* all services */
			serviceRegs = allPublishedServices;
		else
			/* services registered under the class name */
			serviceRegs = (ArrayList) publishedServicesByClass.get(clazz);

		if (serviceRegs == null)
			return (null);

		size = serviceRegs.size();

		if (size == 0)
			return (null);

		references = new ArrayList(size);
		for (int i = 0; i < size; i++) {
			IRemoteServiceRegistration registration = (IRemoteServiceRegistration) serviceRegs
					.get(i);

			IRemoteServiceReference reference = registration.getReference();
			if ((filter == null) || filter.match(reference)) {
				// Must be RemoteServiceReferenceImpl
				RemoteServiceReferenceImpl impl = (RemoteServiceReferenceImpl) reference;
				impl.setRemoteClass(clazz);
				references.add(reference);
			}
		}

		if (references.size() == 0) {
			return null;
		}

		return (IRemoteServiceReference[]) references
				.toArray(new RemoteServiceReferenceImpl[references.size()]);

	}

	public IRemoteServiceReference[] lookupServiceReferences() {
		int size;
		ArrayList references;
		size = allPublishedServices.size();

		if (size == 0) {
			return (null);
		}

		references = new ArrayList(size);
		for (int i = 0; i < size; i++) {
			IRemoteServiceRegistration registration = (IRemoteServiceRegistration) allPublishedServices
					.get(i);

			IRemoteServiceReference reference = registration.getReference();
			references.add(reference);
		}

		if (references.size() == 0) {
			return null;
		}

		return (IRemoteServiceReference[]) references
				.toArray(new RemoteServiceReferenceImpl[references.size()]);
	}

	protected RemoteServiceRegistrationImpl [] getRegistrations() {
		return (RemoteServiceRegistrationImpl []) allPublishedServices.toArray(new RemoteServiceRegistrationImpl[allPublishedServices.size()]);
	}
	protected RemoteServiceRegistrationImpl findRegistrationForServiceId(
			long serviceId) {
		for(Iterator i=allPublishedServices.iterator(); i.hasNext(); ) {
			RemoteServiceRegistrationImpl reg = (RemoteServiceRegistrationImpl) i.next();
			if (serviceId == reg.getServiceId()) return reg;
		}
		return null;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("RemoteServiceRegistryImpl[");
		buf.append("all=").append(allPublishedServices).append(";").append(
				"byclass=").append(publishedServicesByClass).append("]");
		return buf.toString();
	}

}
