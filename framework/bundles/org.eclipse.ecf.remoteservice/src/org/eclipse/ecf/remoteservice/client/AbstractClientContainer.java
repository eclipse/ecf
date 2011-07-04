/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.client;

import java.io.NotSerializableException;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ecf.core.AbstractContainer;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.events.*;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.jobs.JobsExecutor;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.internal.remoteservice.Activator;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.ecf.remoteservice.events.*;
import org.eclipse.ecf.remoteservice.util.RemoteFilterImpl;
import org.eclipse.equinox.concurrent.future.*;
import org.osgi.framework.InvalidSyntaxException;

/**
 * Remote service client abstract superclass.
 * 
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public abstract class AbstractClientContainer extends AbstractContainer implements IRemoteServiceClientContainerAdapter {

	protected ID containerID;
	// The ID we've been assigned to connect to
	protected ID connectedID;
	protected Object connectLock = new Object();

	protected IConnectContext connectContext;

	protected Object remoteResponseDeserializerLock = new Object();
	protected IRemoteResponseDeserializer remoteResponseDeserializer = null;

	protected Object parameterSerializerLock = new Object();
	protected IRemoteCallParameterSerializer parameterSerializer = null;

	protected RemoteServiceClientRegistry registry;
	protected List remoteServiceListeners = new ArrayList();

	private List referencesInUse = new ArrayList();

	/**
	 * @since 4.1
	 */
	protected boolean alwaysSendDefaultParameters;

	public AbstractClientContainer(ID containerID) {
		this.containerID = containerID;
		Assert.isNotNull(this.containerID);
		this.registry = new RemoteServiceClientRegistry(this);
	}

	public void setConnectContextForAuthentication(IConnectContext connectContext) {
		this.connectContext = connectContext;
	}

	public IConnectContext getConnectContextForAuthentication() {
		return connectContext;
	}

	public void setResponseDeserializer(IRemoteResponseDeserializer resource) {
		synchronized (remoteResponseDeserializerLock) {
			this.remoteResponseDeserializer = resource;
		}
	}

	public IRemoteResponseDeserializer getResponseDeserializer() {
		synchronized (remoteResponseDeserializerLock) {
			return this.remoteResponseDeserializer;
		}
	}

	public void setParameterSerializer(IRemoteCallParameterSerializer serializer) {
		synchronized (parameterSerializerLock) {
			this.parameterSerializer = serializer;
		}
	}

	protected IRemoteCallParameterSerializer getParameterSerializer() {
		synchronized (parameterSerializerLock) {
			return this.parameterSerializer;
		}
	}

	protected IRemoteResponseDeserializer getResponseDeserializer(IRemoteCall call, IRemoteCallable callable, Map responseHeaders) {
		synchronized (remoteResponseDeserializerLock) {
			return remoteResponseDeserializer;
		}
	}

	protected IRemoteCallParameterSerializer getParameterSerializer(IRemoteCallParameter parameter, Object value) {
		synchronized (parameterSerializerLock) {
			return parameterSerializer;
		}
	}

	/**
	 * Set the flag to <code>true</code> to include default parameters (which are specified when the callables are created) with
	 * every request to the remote service.
	 * <p>
	 * Setting to <code>false</code> will only send those parameter specified when the call is invoked.
	 * <p>
	 * Parameters which are specifed with the call override the defaults. Default parameters with a value of <code>null</code>
	 * are not included.
	 * 
	 * @param alwaysSendDefaultParameters whether to send default parameters with every remote call
	 * @since 4.1
	 */
	public void setAlwaysSendDefaultParameters(boolean alwaysSendDefaultParameters) {
		this.alwaysSendDefaultParameters = alwaysSendDefaultParameters;
	}

	public void addRemoteServiceListener(IRemoteServiceListener listener) {
		synchronized (remoteServiceListeners) {
			remoteServiceListeners.add(listener);
		}
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

	/**
	 * @since 5.0
	 */
	public IFuture asyncGetRemoteServiceReferences(final ID target, final ID[] idFilter, final String clazz, final String filter) {
		IExecutor executor = new JobsExecutor("asyncGetRemoteServiceReferences"); //$NON-NLS-1$
		return executor.execute(new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) throws Exception {
				return getRemoteServiceReferences(target, idFilter, clazz, filter);
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
		if (reference == null || !(reference instanceof RemoteServiceClientReference))
			return null;
		RemoteServiceClientRegistration registration = registry.findServiceRegistration((RemoteServiceClientReference) reference);
		if (registration == null)
			return null;
		IRemoteService result = (registration == null) ? null : createRemoteService(registration);
		if (result != null)
			referencesInUse.add(reference);
		return result;
	}

	public IRemoteServiceID getRemoteServiceID(ID containerID1, long containerRelativeID) {
		return registry.getRemoteServiceID(containerID1, containerRelativeID);
	}

	public Namespace getRemoteServiceNamespace() {
		return getConnectNamespace();
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

	/**
	 * @since 5.0
	 */
	public IRemoteServiceReference[] getRemoteServiceReferences(ID target, ID[] idFilter, String clazz, String filter) throws InvalidSyntaxException, ContainerConnectException {
		return registry.getRemoteServiceReferences(target, idFilter, clazz, (filter == null) ? null : createRemoteFilter(filter));
	}

	public IRemoteServiceRegistration registerRemoteService(final String[] clazzes, Object service, Dictionary properties) {
		throw new RuntimeException("registerRemoteService cannot be used with client container"); //$NON-NLS-1$
	}

	public void removeRemoteServiceListener(IRemoteServiceListener listener) {
		synchronized (remoteServiceListeners) {
			remoteServiceListeners.remove(listener);
		}
	}

	public boolean ungetRemoteService(final IRemoteServiceReference reference) {
		boolean result = referencesInUse.contains(reference);
		referencesInUse.remove(reference);
		fireRemoteServiceEvent(new IRemoteServiceUnregisteredEvent() {

			public IRemoteServiceReference getReference() {
				return reference;
			}

			public ID getLocalContainerID() {
				return getID();
			}

			public ID getContainerID() {
				return getID();
			}

			public String[] getClazzes() {
				return registry.getClazzes(reference);
			}
		});
		return result;
	}

	// Implementation of IRestClientContainerAdapter
	public IRemoteServiceRegistration registerCallables(IRemoteCallable[] callables, Dictionary properties) {
		Assert.isNotNull(callables);
		final RemoteServiceClientRegistration registration = createRestServiceRegistration(callables, properties);
		this.registry.registerRegistration(registration);
		// notify
		fireRemoteServiceEvent(new IRemoteServiceRegisteredEvent() {

			public IRemoteServiceReference getReference() {
				return registration.getReference();
			}

			public ID getLocalContainerID() {
				return registration.getContainerID();
			}

			public ID getContainerID() {
				return getID();
			}

			public String[] getClazzes() {
				return registration.getClazzes();
			}
		});
		return registration;
	}

	public IRemoteServiceRegistration registerCallables(String[] clazzes, IRemoteCallable[][] callables, Dictionary properties) {
		final RemoteServiceClientRegistration registration = createRestServiceRegistration(clazzes, callables, properties);
		this.registry.registerRegistration(registration);
		// notify
		fireRemoteServiceEvent(new IRemoteServiceRegisteredEvent() {

			public IRemoteServiceReference getReference() {
				return registration.getReference();
			}

			public ID getLocalContainerID() {
				return registration.getContainerID();
			}

			public ID getContainerID() {
				return getID();
			}

			public String[] getClazzes() {
				return registration.getClazzes();
			}
		});
		return registration;
	}

	// IContainer implementation methods
	public void connect(ID targetID, IConnectContext connectContext1) throws ContainerConnectException {
		if (targetID == null)
			throw new ContainerConnectException("targetID cannot be null"); //$NON-NLS-1$
		Namespace targetNamespace = targetID.getNamespace();
		Namespace connectNamespace = getConnectNamespace();
		if (connectNamespace == null)
			throw new ContainerConnectException("targetID namespace cannot be null"); //$NON-NLS-1$
		if (!(targetNamespace.getName().equals(connectNamespace.getName())))
			throw new ContainerConnectException("targetID of incorrect type"); //$NON-NLS-1$
		fireContainerEvent(new ContainerConnectingEvent(containerID, targetID));
		synchronized (connectLock) {
			if (connectedID == null) {
				connectedID = targetID;
				this.connectContext = connectContext1;
			} else if (!connectedID.equals(targetID))
				throw new ContainerConnectException("Already connected to " + connectedID.getName()); //$NON-NLS-1$
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

	public ID getConnectedID() {
		synchronized (connectLock) {
			return connectedID;
		}
	}

	public ID getID() {
		return containerID;
	}

	public void dispose() {
		disconnect();
		synchronized (remoteServiceListeners) {
			remoteServiceListeners.clear();
		}
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

	protected RemoteServiceClientRegistration createRestServiceRegistration(String[] clazzes, IRemoteCallable[][] callables, Dictionary properties) {
		return new RemoteServiceClientRegistration(getRemoteServiceNamespace(), clazzes, callables, properties, registry);
	}

	protected RemoteServiceClientRegistration createRestServiceRegistration(IRemoteCallable[] callables, Dictionary properties) {
		return new RemoteServiceClientRegistration(getRemoteServiceNamespace(), callables, properties, registry);
	}

	protected void logException(String string, Throwable e) {
		Activator a = Activator.getDefault();
		if (a != null)
			a.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, string, e));
	}

	protected ID getRemoteCallTargetID() {
		// First synchronize on connect lock
		synchronized (connectLock) {
			ID cID = getConnectedID();
			return (cID == null) ? getID() : cID;
		}
	}

	protected IRemoteCallParameter[] prepareParameters(String uri, IRemoteCall call, IRemoteCallable callable) throws NotSerializableException {
		List results = new ArrayList();
		Object[] callParameters = call.getParameters();
		IRemoteCallParameter[] defaultCallableParameters = callable.getDefaultParameters();
		if (callParameters == null)
			return defaultCallableParameters;
		for (int i = 0; i < callParameters.length; i++) {
			Object p = callParameters[i];
			// If the parameter is already a remote call parameter just add
			if (p instanceof IRemoteCallParameter) {
				results.add(p);
				continue;
			}
			if (defaultCallableParameters != null && i < defaultCallableParameters.length) {
				// If the call parameter (p) is null, then add the associated
				// callableParameter
				if (p == null)
					results.add(defaultCallableParameters[i]);
				// If not null, then we need to serialize
				IRemoteCallParameter val = serializeParameter(uri, call, callable, defaultCallableParameters[i], p);
				if (val != null)
					results.add(val);
			}
		}
		// Check if we should send additional default parameters and whether there are more to send
		if (alwaysSendDefaultParameters && (defaultCallableParameters.length > callParameters.length)) {
			// Start with the first parameter that wasn't specified
			for (int i = callParameters.length; i < defaultCallableParameters.length; i++) {
				IRemoteCallParameter param = defaultCallableParameters[i];
				// skip default parameters with null values
				if (param.getValue() == null) {
					continue;
				}
				// serialize the parameter using the container's parameterSerializer
				IRemoteCallParameter serialziedParam = serializeParameter(uri, call, callable, param, param.getValue());
				results.add(serialziedParam);
			}
		}
		return (IRemoteCallParameter[]) results.toArray(new IRemoteCallParameter[] {});
	}

	/**
	 * Serialze the parameter using the container's parameterSerializer. If there is no serializer for this container, return null.
	 * 
	 * @return the serialized parameter or null if there is no parameterSerializer for this container
	 * @see IRemoteCallParameterSerializer#serializeParameter(String, IRemoteCall, IRemoteCallable, IRemoteCallParameter, Object)
	 * @since 4.1
	 */
	protected IRemoteCallParameter serializeParameter(String uri, IRemoteCall call, IRemoteCallable callable, IRemoteCallParameter defaultParameter, Object parameterValue) throws NotSerializableException {
		// Get parameter serializer...and
		IRemoteCallParameterSerializer serializer = getParameterSerializer();
		IRemoteCallParameter val = (serializer == null) ? null : serializer.serializeParameter(uri, call, callable, defaultParameter, parameterValue);
		return val;
	}

	protected Object processResponse(String uri, IRemoteCall call, IRemoteCallable callable, Map responseHeaders, String responseBody) throws NotSerializableException {
		IRemoteResponseDeserializer deserializer = getResponseDeserializer();
		return (deserializer == null) ? null : deserializer.deserializeResponse(uri, call, callable, responseHeaders, responseBody);
	}

	/**
	 * Create an implementer of {@link IRemoteService} for the given registration.
	 * 
	 * @param registration registration from which to create the associated IRemoteService.  Will not be <code>null</code>.
	 * @return IRemoteService the remote service associated with this client container.  Should not return <code>null</code>.
	 */
	protected abstract IRemoteService createRemoteService(RemoteServiceClientRegistration registration);

	/**
	 * Prepare an endpoint address for the given call and callable.
	 * 
	 * @param call to create an endpoint for.  Will not be <code>null</code>.
	 * @param callable to create an endpoing for.  Will not be <code>null</code>.
	 * @return String that represents the endpoing for the given call and callable.  May only return <code>null</code> if the
	 * given call should not be completed (i.e. there is no endpoint associated with the given call).
	 */
	protected abstract String prepareEndpointAddress(IRemoteCall call, IRemoteCallable callable);

}
