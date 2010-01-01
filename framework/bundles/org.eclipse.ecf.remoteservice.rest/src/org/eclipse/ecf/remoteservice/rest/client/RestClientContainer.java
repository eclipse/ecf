/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.client;

import org.eclipse.ecf.remoteservice.rest.RestCallableFactory;

import java.util.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ecf.core.AbstractContainer;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.jobs.JobsExecutor;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.internal.remoteservice.rest.RestClientServiceReference;
import org.eclipse.ecf.internal.remoteservice.rest.RestServiceRegistry;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.events.*;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.ecf.remoteservice.rest.identity.RestNamespace;
import org.eclipse.ecf.remoteservice.rest.resource.IRestResourceProcessor;
import org.eclipse.ecf.remoteservice.rest.resource.XMLResource;
import org.eclipse.ecf.remoteservice.util.*;
import org.eclipse.equinox.concurrent.future.*;
import org.osgi.framework.InvalidSyntaxException;

/**
 * A container for REST services. 
 */
public class RestClientContainer extends AbstractContainer implements IRestClientContainerAdapter {

	protected static IRestResourceProcessor defaultResourceProcessor = new XMLResource();
	protected static IRemoteCallParameterSerializer defaultParameterSerializer = new RemoteCallParameterStringSerializer();

	// The container's containerID
	protected RestID containerID;
	// The ID we've been assigned to connect to
	protected RestID connectedID;
	protected Object connectLock = new Object();

	protected IConnectContext connectContext;

	protected RestServiceRegistry registry;
	protected List remoteServiceListeners = new ArrayList();

	protected List referencesInUse = new ArrayList();

	protected Object resourceProcessorLock = new Object();
	protected IRestResourceProcessor resourceProcessor = null;

	protected Object parameterSerializerLock = new Object();
	protected IRemoteCallParameterSerializer parameterSerializer = null;

	public RestClientContainer(RestID id) {
		this.containerID = id;
		registry = new RestServiceRegistry(this);
	}

	// Implementation of IRemoteServiceContainerAdapter (super interface for IRestClientContainerAdapter)
	public void addRemoteServiceListener(IRemoteServiceListener listener) {
		remoteServiceListeners.add(listener);
	}

	public IFuture asyncGetRemoteServiceReferences(final ID[] idFilter, final String clazz, final String filter) {
		IExecutor executor = new JobsExecutor("asyncGetRemoteServiceReferences"); //$NON-NLS-1$
		return executor.execute(new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) throws Exception {
				return getRemoteServiceReferences(idFilter, clazz, filter);
			}
		}, null);
	}

	public IFuture asyncGetRemoteServiceReferences(final ID target, final String clazz, final String filter) {
		IExecutor executor = new JobsExecutor("asyncGetRemoteServiceReferences"); //$NON-NLS-1$
		return executor.execute(new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) throws Exception {
				return getRemoteServiceReferences(target, clazz, filter);
			}
		}, null);
	}

	public IRemoteFilter createRemoteFilter(String filter) throws InvalidSyntaxException {
		return new RemoteFilterImpl(filter);
	}

	public IRemoteServiceReference[] getAllRemoteServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
		return registry.getAllRemoteServiceReferences(clazz, (filter == null) ? null : createRemoteFilter(filter));
	}

	public IRemoteService getRemoteService(IRemoteServiceReference reference) {
		if (reference == null || !(reference instanceof RestClientServiceReference))
			return null;
		RestClientServiceRegistration registration = registry.findServiceRegistration((RestClientServiceReference) reference);
		if (registration == null)
			return null;
		IRemoteService result = (registration == null) ? null : createRestClientService(registration);
		if (result != null)
			referencesInUse.add(reference);
		return result;
	}

	public IRemoteServiceID getRemoteServiceID(ID containerID1, long containerRelativeID) {
		return registry.getRemoteServiceID(containerID1, containerRelativeID);
	}

	public Namespace getRemoteServiceNamespace() {
		if (containerID == null)
			return null;
		return containerID.getNamespace();
	}

	public IRemoteServiceReference getRemoteServiceReference(IRemoteServiceID serviceID) {
		return registry.findServiceReference(serviceID);
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter, String clazz, String filter) throws InvalidSyntaxException {
		return registry.getRemoteServiceReferences(idFilter, clazz, (filter == null) ? null : createRemoteFilter(filter));
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID target, String clazz, String filter) throws InvalidSyntaxException, ContainerConnectException {
		return registry.getRemoteServiceReferences(target, clazz, (filter == null) ? null : createRemoteFilter(filter));
	}

	public IRemoteServiceRegistration registerRemoteService(final String[] clazzes, Object service, Dictionary properties) {
		if (service instanceof List) {
			List callables = (List) service;
			return registerRemoteService(clazzes, callables, properties);
		}
		throw new RuntimeException("registerRemoteService cannot be used with rest client container"); //$NON-NLS-1$
	}

	public void removeRemoteServiceListener(IRemoteServiceListener listener) {
		remoteServiceListeners.remove(listener);
	}

	public void setConnectContextForAuthentication(IConnectContext connectContext) {
		this.connectContext = connectContext;
	}

	public boolean ungetRemoteService(final IRemoteServiceReference reference) {
		boolean result = referencesInUse.contains(reference);
		referencesInUse.remove(reference);
		fireRemoteServiceEvent(new IRemoteServiceUnregisteredEvent() {

			public IRemoteServiceReference getReference() {
				return reference;
			}

			public ID getLocalContainerID() {
				return containerID;
			}

			public ID getContainerID() {
				return containerID;
			}

			public String[] getClazzes() {
				return registry.getClazzes(reference);
			}
		});
		return result;
	}

	// Implementation of IRestClientContainerAdapter
	public void setResourceProcessor(IRestResourceProcessor resource) {
		synchronized (resourceProcessorLock) {
			this.resourceProcessor = resource;
		}
	}

	public IRestResourceProcessor getResourceProcessor() {
		synchronized (resourceProcessorLock) {
			return this.resourceProcessor;
		}
	}

	public void setRemoteCallParameterSerializer(IRemoteCallParameterSerializer serializer) {
		synchronized (parameterSerializerLock) {
			this.parameterSerializer = serializer;
		}
	}

	public IRemoteCallParameterSerializer getRemoteCallParameterSerializer() {
		synchronized (parameterSerializerLock) {
			return this.parameterSerializer;
		}
	}

	public IRemoteServiceRegistration registerRemoteCallables(IRemoteCallable[] restCallables, Dictionary properties) {
		Assert.isNotNull(restCallables);
		final RestClientServiceRegistration registration = createRestServiceRegistration(restCallables, properties);
		// notify
		fireRemoteServiceEvent(new IRemoteServiceRegisteredEvent() {

			public IRemoteServiceReference getReference() {
				return registration.getReference();
			}

			public ID getLocalContainerID() {
				return registration.getContainerID();
			}

			public ID getContainerID() {
				return containerID;
			}

			public String[] getClazzes() {
				return registration.getClazzes();
			}
		});
		this.registry.registerRegistration(registration);
		return registration;
	}

	public IRemoteServiceRegistration registerRemoteCallables(String[] clazzes, IRemoteCallable[][] restCallables, Dictionary properties) {
		final RestClientServiceRegistration registration = createRestServiceRegistration(clazzes, restCallables, properties);
		// notify
		fireRemoteServiceEvent(new IRemoteServiceRegisteredEvent() {

			public IRemoteServiceReference getReference() {
				return registration.getReference();
			}

			public ID getLocalContainerID() {
				return registration.getContainerID();
			}

			public ID getContainerID() {
				return containerID;
			}

			public String[] getClazzes() {
				return registration.getClazzes();
			}
		});
		this.registry.registerRegistration(registration);
		return registration;
	}

	public IRemoteServiceRegistration registerRemoteCallables(Class[] clazzes, List callables, Dictionary properties) {
		Assert.isNotNull(clazzes);
		IRemoteCallable[][] restCallables = RestCallableFactory.createCallablesFromClasses(clazzes, callables);
		Assert.isNotNull(restCallables);
		Assert.isTrue(restCallables.length > 0);
		final String[] classNames = new String[clazzes.length];
		for (int i = 0; i < clazzes.length; i++) {
			classNames[i] = clazzes[i].getName();
		}
		return registerRemoteCallables(classNames, restCallables, properties);
	}

	public IRemoteServiceRegistration registerCallable(String[] clazzes, List callables, Dictionary properties) {
		Assert.isNotNull(clazzes);
		Assert.isNotNull(callables);
		return registerRemoteCallables(RestCallableFactory.getClazzesFromStrings(clazzes), callables, properties);
	}

	protected IRemoteService createRestClientService(RestClientServiceRegistration registration) {
		return new RestClientService(registration);
	}

	protected IConnectContext getRestConnectContext() {
		return connectContext;
	}

	protected IRestResourceProcessor getResourceProcessor(IRemoteCall call, IRemoteCallable callable, Map responseHeaders) {
		IRestResourceProcessor result = null;
		synchronized (resourceProcessorLock) {
			result = resourceProcessor;
		}
		// If no resourceProcessor explicitly set, we return default of XMLResource
		return (result == null) ? defaultResourceProcessor : result;
	}

	protected IRemoteCallParameterSerializer getParameterSerializer(IRemoteCallParameter parameter, Object value) {
		IRemoteCallParameterSerializer result = null;
		synchronized (parameterSerializerLock) {
			result = parameterSerializer;
		}
		return (result == null) ? defaultParameterSerializer : result;
	}

	protected RestID getTargetRestID() {
		synchronized (connectLock) {
			if (connectedID != null)
				return connectedID;
			return containerID;
		}
	}

	protected RestClientServiceRegistration createRestServiceRegistration(String[] clazzes, IRemoteCallable[][] callables, Dictionary properties) {
		return new RestClientServiceRegistration(clazzes, callables, properties, registry);
	}

	protected RestClientServiceRegistration createRestServiceRegistration(IRemoteCallable[] callables, Dictionary properties) {
		return new RestClientServiceRegistration(callables, properties, registry);
	}

	protected void logException(String string, Throwable e) {
		// XXX log properly
		if (string != null)
			System.out.println(string);
		if (e != null)
			e.printStackTrace();
	}

	// IContainer implementation methods
	public void connect(ID targetID, IConnectContext connectContext1) throws ContainerConnectException {
		if (targetID == null)
			throw new ContainerConnectException("targetID cannot be null"); //$NON-NLS-1$
		if (!(targetID instanceof RestID))
			throw new ContainerConnectException("targetID must be of RestID type"); //$NON-NLS-1$
		RestID targetRestID = (RestID) targetID;
		fireContainerEvent(new ContainerConnectingEvent(containerID, targetRestID));
		synchronized (connectLock) {
			if (connectedID != null) {
				// If we're being asked to connect to a target we are already
				// connected to then
				if (!connectedID.equals(targetRestID))
					return;
				throw new ContainerConnectException("Already connected to " + connectedID.getName()); //$NON-NLS-1$
			}
			connectedID = targetRestID;
			this.connectContext = connectContext1;
		}
		fireContainerEvent(new ContainerConnectedEvent(containerID, targetID));
	}

	public void disconnect() {
		ID oldId = connectedID;
		fireContainerEvent(new ContainerDisconnectingEvent(containerID, oldId));
		synchronized (connectLock) {
			connectedID = null;
			connectContext = null;
		}
		fireContainerEvent(new ContainerDisconnectedEvent(containerID, oldId));
	}

	public void dispose() {
		disconnect();
		remoteServiceListeners.clear();
		super.dispose();
	}

	void fireRemoteServiceEvent(IRemoteServiceEvent event) {
		List toNotify = null;
		// Copy array
		synchronized (remoteServiceListeners) {
			toNotify = new ArrayList(remoteServiceListeners);
		}
		for (Iterator i = toNotify.iterator(); i.hasNext();) {
			((IRemoteServiceListener) i.next()).handleServiceEvent(event);
		}
	}

	public Namespace getConnectNamespace() {
		return IDFactory.getDefault().getNamespaceByName(RestNamespace.NAME);
	}

	public ID getConnectedID() {
		synchronized (connectLock) {
			return connectedID;
		}
	}

	public ID getID() {
		return containerID;
	}

}
