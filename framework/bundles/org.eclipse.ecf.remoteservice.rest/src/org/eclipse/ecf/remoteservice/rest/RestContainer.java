/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.remoteservice.rest;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ecf.core.AbstractContainer;
import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.events.ContainerConnectedEvent;
import org.eclipse.ecf.core.events.ContainerConnectingEvent;
import org.eclipse.ecf.core.events.ContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.ContainerDisconnectingEvent;
import org.eclipse.ecf.core.events.ContainerDisposeEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.jobs.JobsExecutor;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.remoteservice.rest.RestServiceRegistry;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceID;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.RemoteServiceContainer;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceRegisteredEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceUnregisteredEvent;
import org.eclipse.ecf.remoteservice.rest.identity.RestID;
import org.eclipse.ecf.remoteservice.util.RemoteFilterImpl;
import org.eclipse.equinox.concurrent.future.IExecutor;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.osgi.framework.InvalidSyntaxException;

/**
 * A container for REST services. This was implemented using the ECF#RemoteServiceAPI.
 */
public class RestContainer extends AbstractContainer implements IRemoteServiceContainerAdapter, IRemoteServiceContainer {

	public static final String NAME = "ecf.rest.client";
	private ID connectedId;
	private ID id;
	private List remoteServiceListeners = new ArrayList();
	private List containerListeners = new ArrayList();
	private RestServiceRegistry registry;
	private List referencesInUse = new ArrayList();
	private RemoteServiceContainer remoteServiceContainer;
	private IConnectContext connectContext;
	private URL originBaseUrl;
	private Map restCalls = new HashMap();

	public RestContainer(ID id) {
		this.id = id;
		remoteServiceContainer = new RemoteServiceContainer(this, this);
		registry = new RestServiceRegistry(this);
	}
	
	public RestContainer() {
	}

	public void connect(ID targetID, IConnectContext connectContext) throws ContainerConnectException {
		fireContainerEvent(new ContainerConnectingEvent(id, targetID));
		if(targetID instanceof RestID){
			URL baseURL = ((RestID)targetID).getBaseURL();
			if(this.id instanceof RestID) {				
				RestID restId = (RestID)this.id;
				originBaseUrl = restId.getBaseURL();
				restId.setBaseUrl(baseURL);
			}
		}
		connectedId = targetID;
		this.connectContext = connectContext;
		fireContainerEvent(new ContainerConnectedEvent(id, targetID));
	}

	public void disconnect() {
		ID oldId = connectedId;
		fireContainerEvent(new ContainerDisconnectingEvent(id, oldId));
		connectedId = null;
		connectContext = null;
		if(id instanceof RestID) 
			((RestID)id).setBaseUrl(originBaseUrl);
		fireContainerEvent(new ContainerDisconnectedEvent(id, oldId));
	}

	public void dispose() {
		disconnect();	
		fireContainerEvent(new ContainerDisposeEvent(id));
		containerListeners.clear();
		remoteServiceListeners.clear();
		
	}
	
	void fireRemoteServiceEvent(IRemoteServiceEvent event) {
		synchronized (remoteServiceListeners) {
			for (int i = 0; i < remoteServiceListeners.size(); i++) {
				((IRemoteServiceListener) remoteServiceListeners.get(i)).handleServiceEvent(event);				
			}
		}
	}

	public Namespace getConnectNamespace() {
		if( connectedId != null ) 
			return connectedId.getNamespace();
		return null;
	}

	public ID getConnectedID() {
		return connectedId;
	}


	public ID getID() {
		return id;
	}

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
		return null;
	}

	public IRemoteService getRemoteService(IRemoteServiceReference reference) {
		IRemoteService service = registry.findService(reference);
		if(service != null)
			referencesInUse.add(reference);
		return service;
	}

	public IRemoteServiceID getRemoteServiceID(ID containerID, long containerRelativeID) {
		return registry.getRemoteServiceID( containerID, containerRelativeID);
	}

	public Namespace getRemoteServiceNamespace() {
		if(id == null)
			return null;
		return id.getNamespace();
	}

	public IRemoteServiceReference getRemoteServiceReference(IRemoteServiceID serviceID) {
		return registry.findServiceReference(serviceID);
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter, String clazz, String filter) throws InvalidSyntaxException {
		return registry.getRemoteServiceReferences(idFilter, clazz, filter);
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID target, String clazz, String filter) throws InvalidSyntaxException, ContainerConnectException {
		return registry.getRemoteServiceReferences( target, clazz, filter);
	}

	public IRemoteServiceRegistration registerRemoteService(final String[] clazzes, Object service, Dictionary properties) {			
		final RestServiceRegistration registration = new RestServiceRegistration(clazzes, service, properties, registry);
		fireRemoteServiceEvent(new IRemoteServiceRegisteredEvent() {
			
			public IRemoteServiceReference getReference() {
				return registration.getReference();
			}
			
			public ID getLocalContainerID() {
				return registration.getContainerID();
			}
			
			public ID getContainerID() {
				return id;
			}
			
			public String[] getClazzes() {
				return clazzes;
			}
		});
		if(service instanceof RestService)
			((RestService)service).setReference(registration.getReference());
		registry.registerRegistration(registration);
		return registration;
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
				return id;
			}
			
			public ID getContainerID() {
				return id;
			}
			
			public String[] getClazzes() {
				return registry.getClazzes( reference );
			}
		});
		return result;
	}

	public IContainer getContainer() {
		return remoteServiceContainer.getContainer();
	}

	public IRemoteServiceContainerAdapter getContainerAdapter() {
		return remoteServiceContainer.getContainerAdapter();
	}

	public IRemoteService getRemoteService(String targetLocation, String serviceInterfaceClass, String filter) throws ContainerConnectException, InvalidSyntaxException {
		return remoteServiceContainer.getRemoteService(targetLocation, serviceInterfaceClass, filter);
	}

	public IRemoteService getRemoteService(String targetLocation, String serviceInterfaceClass) throws ContainerConnectException {
		return remoteServiceContainer.getRemoteService(targetLocation, serviceInterfaceClass);
	}

	public IRemoteService getRemoteService(String serviceInterfaceClass) {
		return remoteServiceContainer.getRemoteService(serviceInterfaceClass);
	}
	
	IConnectContext getConnectContext() {
		return connectContext;
	}

	public IRestCall lookupRestCall(IRemoteCall call) {
		String key = call.getMethod();
		return (IRestCall) restCalls.get(key);
	}
	
	/**
	 * Registers a POJO object as a REST service. The service object has to implement
	 * {@link IRestResponseProcessor} to get notified about an incoming response.
	 * This method also registers a {@link IRemoteService} object for the rest service.
	 * To associate a {@link IRemoteCall} to the rest service object the 
	 * {@link IRemoteCall#getMethod()} will be used. Therefore a {@link Map} has to
	 * passed to this method with a {@link String} as key and an {@link IRestCall} object
	 * as value. 
	 * 
	 * @param clazzes a String array woth classnames which the service object has to implement.
	 * @param service the service object as a POJO
	 * @param restCalls a {@link Map} with String as keys and {@link IRestCall} objects as value.
	 * @param properties to be associated with service.
	 * 
	 * @return The service registration for the registered {@link IRemoteService}.
	 * Will not return <code>null</code>.
	 */
	public RestServiceRegistration registerRestService( String[] clazzes, Object service, Map restCalls, Dictionary properties) throws ECFException {
		if(checkServiceClass(clazzes, service) == null && service instanceof IRestResponseProcessor) {
			// map the keys to the restCalls
			Object[] keys = restCalls.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				Object restCall = restCalls.get(keys[i]);
				if(!(restCall instanceof IRestCall))
					throw new ECFException("registered calls must be instacnes of IRestCall");
				this.restCalls.put(keys[i], restCall);
			}
			RestService restService = new RestService(service);
			return (RestServiceRegistration) registerRemoteService(new String[]{RestService.class.getName()}, restService, properties);
		} else 
			throw new IllegalArgumentException("service does not implement all classes or IRestResponseProcessor");
	}
	
	/** 
	 * Return the name of the class that is not satisfied by the service object. 
	 * @param clazzes Array of class names.
	 * @param serviceObject Service object.
	 * @return The name of the class that is not satisfied by the service object.
	 */
	private String checkServiceClass(final String[] clazzes, final Object serviceObject) {
		ClassLoader cl = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				return serviceObject.getClass().getClassLoader();
			}
		});
		for (int i = 0, len = clazzes.length; i < len; i++) {
			try {
				Class serviceClazz = cl == null ? Class.forName(clazzes[i]) : cl.loadClass(clazzes[i]);
				if (!serviceClazz.isInstance(serviceObject))
					return clazzes[i];
			} catch (ClassNotFoundException e) {
				//This check is rarely done
				if (extensiveCheckServiceClass(clazzes[i], serviceObject.getClass()))
					return clazzes[i];
			}
		}
		return null;
	}
	
	private static boolean extensiveCheckServiceClass(String clazz, Class serviceClazz) {
		if (clazz.equals(serviceClazz.getName()))
			return false;
		Class[] interfaces = serviceClazz.getInterfaces();
		for (int i = 0, len = interfaces.length; i < len; i++)
			if (!extensiveCheckServiceClass(clazz, interfaces[i]))
				return false;
		Class superClazz = serviceClazz.getSuperclass();
		if (superClazz != null)
			if (!extensiveCheckServiceClass(clazz, superClazz))
				return false;
		return true;
	}

}
