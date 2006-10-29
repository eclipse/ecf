/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.BaseSharedObject;
import org.eclipse.ecf.core.sharedobject.SharedObjectInitException;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.remoteservice.Activator;
import org.eclipse.ecf.internal.provider.remoteservice.IRemoteServiceProviderDebugOptions;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallStartEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceRegisteredEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceUnregisteredEvent;

public class RegistrySharedObject extends BaseSharedObject implements
		IRemoteServiceContainerAdapter {

	protected RemoteServiceRegistryImpl localRegistry;

	protected Map remoteRegistrys = Collections.synchronizedMap(new HashMap());

	protected List serviceListeners = new ArrayList();

	public RegistrySharedObject() {
	}

	/* Begin implementation of IRemoteServiceContainerAdapter public interface */
	public void addRemoteServiceListener(IRemoteServiceListener listener) {
		trace("addRemoteServiceListener(" + listener + ")");
		synchronized (serviceListeners) {
			serviceListeners.add(listener);
		}
	}

	public void removeRemoteServiceListener(IRemoteServiceListener listener) {
		trace("removeRemoteServiceListener(" + listener + ")");
		synchronized (serviceListeners) {
			serviceListeners.remove(listener);
		}
	}

	public IRemoteService getRemoteService(IRemoteServiceReference reference) {
		trace("getRemoteService(" + reference + ")");
		RemoteServiceRegistrationImpl registration = getRemoteServiceRegistrationImpl(reference);
		if (registration == null)
			return null;
		trace("getRemoteService.registration found=" + registration);
		return new RemoteServiceImpl(this, registration);
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter,
			String clazz, String filter) {
		IRemoteFilter remoteFilter = createRemoteFilterFromString(filter);
		List references = new ArrayList();
		synchronized (remoteRegistrys) {
			if (idFilter == null) {
				ArrayList registrys = new ArrayList(remoteRegistrys.values());
				for (Iterator i = registrys.iterator(); i.hasNext();) {
					RemoteServiceRegistryImpl registry = (RemoteServiceRegistryImpl) i
							.next();
					// Add IRemoteServiceReferences from each remote registry
					addReferencesFromRegistry(clazz, remoteFilter, registry,
							references);
				}
			} else {
				for (int i = 0; i < idFilter.length; i++) {
					RemoteServiceRegistryImpl registry = (RemoteServiceRegistryImpl) remoteRegistrys
							.get(idFilter[i]);
					if (registry != null)
						addReferencesFromRegistry(clazz, remoteFilter,
								registry, references);
				}
			}
		}
		return (IRemoteServiceReference[]) references
				.toArray(new IRemoteServiceReference[references.size()]);
	}

	public IRemoteServiceRegistration registerRemoteService(String[] clazzes,
			Object service, Dictionary properties) {
		if (service == null)
			throw new NullPointerException("service cannot be null");
		int size = clazzes.length;

		if (size == 0)
			throw new IllegalArgumentException("service classes list is empty");

		String[] copy = new String[clazzes.length];
		for (int i = 0; i < clazzes.length; i++) {
			copy[i] = new String(clazzes[i].getBytes());
		}
		clazzes = copy;

		String invalidService = checkServiceClass(clazzes, service);
		if (invalidService != null)
			throw new IllegalArgumentException("Service is not valid: "
					+ invalidService);

		RemoteServiceRegistrationImpl reg = new RemoteServiceRegistrationImpl();
		reg.publish(this, localRegistry, service, clazzes, properties);

		sendAddRegistration(reg);
		return reg;
	}

	public boolean ungetRemoteService(IRemoteServiceReference ref) {
		return true;
	}

	/* End implementation of IRemoteServiceContainerAdapter public interface */

	public void initialize() throws SharedObjectInitException {
		super.initialize();
		super.addEventProcessor(new IEventProcessor() {
			public boolean processEvent(Event arg0) {
				if (arg0 instanceof IContainerConnectedEvent)
					handleContainerConnectedEvent((IContainerConnectedEvent) arg0);
				else if (arg0 instanceof IContainerDisconnectedEvent)
					handleContainerDisconnectedEvent((IContainerDisconnectedEvent) arg0);
				return false;
			}
		});
		localRegistry = new RemoteServiceRegistryImpl(getLocalContainerID());
	}

	protected void handleContainerDisconnectedEvent(
			IContainerDisconnectedEvent event) {
		trace("handleContainerDisconnectedEvent(" + event + ")");
		ID targetID = event.getTargetID();
		synchronized (remoteRegistrys) {
			RemoteServiceRegistryImpl registry = getRemoteRegistry(targetID);
			if (registry != null) {
				removeRemoteRegistry(targetID);
				RemoteServiceRegistrationImpl registrations[] = registry
						.getRegistrations();
				if (registrations != null) {
					for (int i = 0; i < registrations.length; i++) {
						trace("handleContainerDisconnectedEvent.unregistering serviceid="
								+ registrations[i].getServiceId());
						registry.unpublishService(registrations[i]);
						fireRemoteServiceListeners(createUnregisteredEvent(registrations[i]));
					}
				}
			}
		}
	}

	protected void handleContainerConnectedEvent(IContainerConnectedEvent event) {
		// If we're a group manager or the newly connected container is the
		// group manager
		if (getContext().isGroupManager()
				|| event.getTargetID().equals(getGroupID())) {
			trace("local containerID=" + getLocalContainerID()
					+ " connected to groupID=" + getGroupID());
			// No local registry changes while this is going on
			synchronized (localRegistry) {
				RemoteServiceRegistrationImpl registrations[] = localRegistry
						.getRegistrations();
				if (registrations != null)
					for (int i = 0; i < registrations.length; i++) {
						RemoteServiceRegistrationImpl registration = registrations[i];
						sendAddRegistration(registration);
					}
			}
		}
	}

	private Request createRequest(
			RemoteServiceRegistrationImpl remoteRegistration, IRemoteCall call,
			IRemoteCallListener listener) {
		RemoteServiceReferenceImpl refImpl = (RemoteServiceReferenceImpl) remoteRegistration
				.getReference();
		RemoteCallImpl remoteCall = RemoteCallImpl.createRemoteCall(refImpl
				.getRemoteClass(), call.getMethod(), call.getParameters(), call
				.getTimeout());
		return new Request(this.getLocalContainerID(), remoteRegistration
				.getServiceId(), remoteCall, listener);
	}

	protected void fireRemoteServiceListeners(IRemoteServiceEvent event) {
		synchronized (serviceListeners) {
			for (Iterator i = serviceListeners.iterator(); i.hasNext();) {
				IRemoteServiceListener l = (IRemoteServiceListener) i.next();
				l.handleServiceEvent(event);
			}
		}
	}

	private RemoteServiceRegistrationImpl getRemoteServiceRegistrationImpl(
			IRemoteServiceReference reference) {
		if (reference instanceof RemoteServiceReferenceImpl) {
			RemoteServiceReferenceImpl ref = (RemoteServiceReferenceImpl) reference;
			if (!ref.isActive())
				return null;
			else
				return ref.getRegistration();
		} else
			return null;
	}

	IRemoteServiceReference[] getRemoteSeifrviceReferencesForRegistry(
			RemoteServiceRegistryImpl registry, String clazz, String filter) {
		return registry.lookupServiceReferences(clazz,
				createRemoteFilterFromString(filter));
	}

	IRemoteServiceReference[] getRemoteServiceReferencesForRegistry(
			RemoteServiceRegistryImpl registry) {
		return registry.lookupServiceReferences();
	}

	private IRemoteFilter createRemoteFilterFromString(String filter) {
		// XXX make remote filter
		return null;
	}

	private void addReferencesFromRegistry(String clazz,
			IRemoteFilter remoteFilter, RemoteServiceRegistryImpl registry,
			List references) {
		IRemoteServiceReference[] rs = registry.lookupServiceReferences(clazz,
				remoteFilter);
		if (rs != null)
			for (int j = 0; j < rs.length; j++)
				references.add(rs[j]);
	}

	protected Object callSynch(RemoteServiceRegistrationImpl registration,
			IRemoteCall call) throws ECFException {
		boolean doneWaiting = false;
		Response response = null;
		try {
			// First send request
			long requestId = sendCallRequest(registration, call);
			// Then get the specified timeout and calculate when we should
			// timeout in real time
			long timeout = call.getTimeout() + System.currentTimeMillis();
			// Now loop until timeout time has elapsed
			while ((timeout - System.currentTimeMillis()) > 0 && !doneWaiting) {
				Request request = findRequestForId(requestId);
				if (request == null)
					throw new NullPointerException(
							"No pending request found id " + requestId);
				synchronized (request) {
					if (request.isDone()) {
						removeRequest(request);
						trace("request/response DONE: " + request);
						doneWaiting = true;
						response = request.getResponse();
						if (response == null)
							throw new NullPointerException(
									"Response to request is null");
					} else {
						trace("Waiting " + RESPONSE_WAIT_INTERVAL
								+ " for response to request: " + request);
						request.wait(RESPONSE_WAIT_INTERVAL);
					}
				}
			}
			if (!doneWaiting)
				throw new ECFException("Request timed out after "
						+ call.getTimeout() + " ms");
		} catch (IOException e) {
			logSendError(CALL_REQUEST_ERROR_CODE, CALL_REQUEST_ERROR_MESSAGE, e);
			throw new ECFException("Exception sending request", e);
		} catch (InterruptedException e) {
			logSendError(CALL_REQUEST_TIMEOUT_ERROR_CODE,
					CALL_REQUEST_TIMEOUT_ERROR_MESSAGE, e);
			throw new ECFException("Wait for response interrupted", e);
		}
		// Success...now get values and return
		Object result = null;
		if (response.hadException())
			throw new ECFException("Exception in remote call", response
					.getException());
		else
			result = response.getResponse();

		trace("request " + response.getRequestId() + " returning " + result);
		return result;

	}

	protected void fireCallStartEvent(IRemoteCallListener listener,
			final long requestId, final IRemoteServiceReference reference,
			final IRemoteCall call) {
		if (listener != null) {
			listener.handleEvent(new IRemoteCallStartEvent() {
				public long getRequestId() {
					return requestId;
				}

				public IRemoteCall getCall() {
					return call;
				}

				public IRemoteServiceReference getReference() {
					return reference;
				}

				public String toString() {
					StringBuffer buf = new StringBuffer(
							"IRemoteCallStartEvent[");
					buf.append(";reference=").append(reference)
							.append(";call=").append(call).append("]");
					return buf.toString();
				}
			});
		}
	}

	protected void fireCallCompleteEvent(IRemoteCallListener listener,
			final long requestId, final Object response,
			final boolean hadException, final Throwable exception) {
		if (listener != null)
			listener.handleEvent(new IRemoteCallCompleteEvent() {
				public long getRequestId() {
					return requestId;
				}

				public Throwable getException() {
					return exception;
				}

				public Object getResponse() {
					return response;
				}

				public boolean hadException() {
					return hadException;
				}

				public String toString() {
					StringBuffer buf = new StringBuffer(
							"IRemoteCallCompleteEvent[");
					buf.append(";response=").append(response).append(
							";hadException=").append(hadException).append(
							";exception=").append(exception).append("]");
					return buf.toString();
				}
			});
	}

	static String checkServiceClass(final String[] clazzes,
			final Object serviceObject) {
		ClassLoader cl = (ClassLoader) AccessController
				.doPrivileged(new PrivilegedAction() {
					public Object run() {
						return serviceObject.getClass().getClassLoader();
					}
				});
		for (int i = 0; i < clazzes.length; i++) {
			try {
				Class serviceClazz = cl == null ? Class.forName(clazzes[i])
						: cl.loadClass(clazzes[i]);
				if (!serviceClazz.isInstance(serviceObject))
					return clazzes[i];
			} catch (ClassNotFoundException e) {
				// This check is rarely done
				if (extensiveCheckServiceClass(clazzes[i], serviceObject
						.getClass()))
					return clazzes[i];
			}
		}
		return null;
	}

	private static boolean extensiveCheckServiceClass(String clazz,
			Class serviceClazz) {
		if (clazz.equals(serviceClazz.getName()))
			return false;
		Class[] interfaces = serviceClazz.getInterfaces();
		for (int i = 0; i < interfaces.length; i++)
			if (!extensiveCheckServiceClass(clazz, interfaces[i]))
				return false;
		Class superClazz = serviceClazz.getSuperclass();
		if (superClazz != null)
			if (!extensiveCheckServiceClass(clazz, superClazz))
				return false;
		return true;
	}

	/**
	 * Message send and handlers
	 */
	private static final String FIRE_REQUEST = "handleFireRequest";

	private static final String FIRE_REQUEST_ERROR_MESSAGE = "exception sending fire request message";

	private static final int FIRE_REQUEST_ERROR_CODE = 202;

	private static final String CALL_REQUEST = "handleCallRequest";

	private static final String CALL_REQUEST_ERROR_MESSAGE = "exception sending call request message";

	private static final int CALL_REQUEST_ERROR_CODE = 203;

	private static final String CALL_REQUEST_TIMEOUT_ERROR_MESSAGE = "timeout for call request";

	private static final int CALL_REQUEST_TIMEOUT_ERROR_CODE = 204;

	private static final String UNREGISTER = "handleUnregister";

	private static final String UNREGISTER_ERROR_MESSAGE = "exception sending service unregister message";

	private static final int UNREGISTER_ERROR_CODE = 206;

	private static final String MSG_INVOKE_ERROR_MESSAGE = "Exception in shared object message invoke";

	private static final int MSG_INVOKE_ERROR_CODE = 207;

	private static final String SERVICE_INVOKE_ERROR_MESSAGE = "Exception invoking service";

	private static final int SERVICE_INVOKE_ERROR_CODE = 208;

	private static final String HANDLE_REQUEST_ERROR_MESSAGE = "Exception locally invoking remote call";

	private static final int HANDLE_REQUEST_ERROR_CODE = 209;

	private static final String CALL_RESPONSE = "handleCallResponse";

	private static final String CALL_RESPONSE_ERROR_MESSAGE = "Exception sending response";

	private static final int CALL_RESPONSE_ERROR_CODE = 210;

	private static final String REQUEST_NOT_FOUND_ERROR_MESSAGE = "request not found for received response";

	private static final int REQUEST_NOT_FOUND_ERROR_CODE = 211;

	private static final long RESPONSE_WAIT_INTERVAL = 5000;

	private static final String ADD_REGISTRATION = "handleAddRegistration";

	private static final String ADD_REGISTRATION_ERROR_MESSAGE = "exception sending add service registration message";

	private static final int ADD_REGISTRATION_ERROR_CODE = 212;

	protected void sendAddRegistration(RemoteServiceRegistrationImpl reg) {
		trace("sendAddRegistration(" + null + "," + reg + ")");
		try {
			sendSharedObjectMsgTo(null, SharedObjectMsg.createMsg(null,
					ADD_REGISTRATION, getLocalContainerID(), reg));
		} catch (IOException e) {
			logSendError(ADD_REGISTRATION_ERROR_CODE,
					ADD_REGISTRATION_ERROR_MESSAGE, e);
		}
	}

	protected void handleAddRegistration(ID remoteContainerID,
			final RemoteServiceRegistrationImpl registration) {
		if (remoteContainerID == null)
			throw new NullPointerException(
					"registry received with null client ID, discarding");
		if (getLocalContainerID().equals(remoteContainerID))
			return;
		trace(remoteContainerID + " to " + getLocalContainerID() + " added "
				+ registration);
		synchronized (remoteRegistrys) {
			// Find registry for remoteContainer
			RemoteServiceRegistryImpl registry = getRemoteRegistry(remoteContainerID);
			// If there's not one already then lazily make one and add it
			if (registry == null) {
				registry = new RemoteServiceRegistryImpl(remoteContainerID);
				addRemoteRegistry(registry);
			}
			// publish service in this registry. At this point it's ready to go
			registry.publishService(registration);
			// notify IRemoteServiceListeners synchronously
			fireRemoteServiceListeners(new IRemoteServiceRegisteredEvent() {

				public String[] getClazzes() {
					return registration.getClasses();
				}

				public ID getContainerID() {
					return registration.getContainerID();
				}

				public IRemoteServiceReference getReference() {
					return registration.getReference();
				}

				public String toString() {
					StringBuffer buf = new StringBuffer(
							"RemoteServiceRegisteredEvent[");
					buf.append("containerID=").append(
							registration.getContainerID());
					buf.append(";clazzes=").append(
							Arrays.asList(registration.getClasses()));
					buf.append(";reference=").append(
							registration.getReference()).append("]");
					return buf.toString();
				}
			});
		}
	}

	protected long sendCallRequest(
			RemoteServiceRegistrationImpl remoteRegistration,
			final IRemoteCall call) throws IOException {
		Request request = createRequest(remoteRegistration, call, null);
		addRequest(request);
		try {
			sendSharedObjectMsgTo(remoteRegistration.getContainerID(),
					SharedObjectMsg.createMsg(CALL_REQUEST, request));
		} catch (IOException e) {
			removeRequest(request);
			throw e;
		}
		return request.getRequestId();
	}

	protected void handleCallRequest(Request request) {
		ID responseTarget = request.getRequestContainerID();
		trace("handleCallRequest(" + request + ") from " + responseTarget);
		RemoteServiceRegistrationImpl localRegistration = getLocalRegistrationForRequest(request);
		// Else we've got a local service and we invoke it
		RemoteCallImpl call = request.getCall();
		Response response = null;
		Object result = null;
		try {
			result = localRegistration.callService(call);
			response = new Response(request.getRequestId(), result);
		} catch (Exception e) {
			response = new Response(request.getRequestId(), e);
			logHandleError(SERVICE_INVOKE_ERROR_CODE,
					SERVICE_INVOKE_ERROR_MESSAGE, e);
		}
		// Now send response back to responseTarget (original requestor)
		sendCallResponse(responseTarget, response);
	}

	protected void sendCallRequestWithListener(
			RemoteServiceRegistrationImpl remoteRegistration, IRemoteCall call,
			IRemoteCallListener listener) {
		Request request = createRequest(remoteRegistration, call, listener);
		fireCallStartEvent(listener, request.getRequestId(), remoteRegistration
				.getReference(), call);
		try {
			addRequest(request);
			sendSharedObjectMsgTo(remoteRegistration.getContainerID(),
					SharedObjectMsg.createMsg(CALL_REQUEST, request));
		} catch (IOException e) {
			logSendError(CALL_REQUEST_ERROR_CODE, CALL_REQUEST_ERROR_MESSAGE, e);
			removeRequest(request);
			fireCallCompleteEvent(listener, request.getRequestId(), null, true,
					e);
		}
	}

	private void sendCallResponse(ID responseTarget, Response response) {
		try {
			trace("sending response " + response + " to " + responseTarget);
			sendSharedObjectMsgTo(responseTarget, SharedObjectMsg.createMsg(
					CALL_RESPONSE, response));
		} catch (IOException e) {
			logSendError(CALL_RESPONSE_ERROR_CODE, CALL_RESPONSE_ERROR_MESSAGE,
					e);
		}
	}

	protected void handleCallResponse(Response response) {
		trace("handleCallResponse(" + response + ")");
		Request request = findRequestForId(response.getRequestId());
		if (request == null) {
			logHandleError(REQUEST_NOT_FOUND_ERROR_CODE,
					REQUEST_NOT_FOUND_ERROR_MESSAGE, new NullPointerException());
			return;
		} else {
			IRemoteCallListener listener = request.getListener();
			if (listener != null) {
				fireCallCompleteEvent(listener, request.getRequestId(),
						response.getResponse(), response.hadException(),
						response.getException());
				return;
			} else {
				synchronized (request) {
					request.setResponse(response);
					request.setDone(true);
					request.notify();
				}
			}
		}
	}

	protected Request sendFireRequest(
			RemoteServiceRegistrationImpl remoteRegistration, IRemoteCall call)
			throws ECFException {
		try {
			Request request = createRequest(remoteRegistration, call, null);
			sendSharedObjectMsgTo(remoteRegistration.getContainerID(),
					SharedObjectMsg.createMsg(FIRE_REQUEST, request));
			return request;
		} catch (IOException e) {
			logSendError(FIRE_REQUEST_ERROR_CODE, FIRE_REQUEST_ERROR_MESSAGE, e);
			throw new ECFException("IOException sending remote request", e);
		}
	}

	protected void handleFireRequest(Request request) {
		trace("handleFireRequest(" + request + ") from "
				+ request.getRequestContainerID());
		RemoteServiceRegistrationImpl localRegistration = getLocalRegistrationForRequest(request);
		// Else we've got a local service and we invoke it
		RemoteCallImpl call = request.getCall();
		try {
			localRegistration.callService(call);
		} catch (Exception e) {
			logSendError(HANDLE_REQUEST_ERROR_CODE,
					HANDLE_REQUEST_ERROR_MESSAGE, e);
		}
	}

	protected void sendUnregister(
			RemoteServiceRegistrationImpl serviceRegistration) {
		synchronized (localRegistry) {
			localRegistry.unpublishService(serviceRegistration);
			ID containerID = serviceRegistration.getContainerID();
			Long serviceId = new Long(serviceRegistration.getServiceId());
			trace("sendUnregisterToAll(containerID=" + containerID
					+ ",serviceId=" + serviceId + ")");
			try {
				this.sendSharedObjectMsgTo(null, SharedObjectMsg.createMsg(
						UNREGISTER, new Object[] { containerID, serviceId }));
			} catch (IOException e) {
				logSendError(UNREGISTER_ERROR_CODE, UNREGISTER_ERROR_MESSAGE, e);
			}
		}
	}

	protected void handleUnregister(ID containerID, Long serviceId) {
		trace("handleUnregister(containerID=" + containerID + ",serviceId="
				+ serviceId + ")");
		synchronized (remoteRegistrys) {
			// get registry for given containerID
			RemoteServiceRegistryImpl serviceRegistry = (RemoteServiceRegistryImpl) remoteRegistrys
					.get(containerID);
			if (serviceRegistry != null) {
				final RemoteServiceRegistrationImpl registration = serviceRegistry
						.findRegistrationForServiceId(serviceId.longValue());
				if (registration != null) {
					trace("handleUnregister.unregistering serviceid="
							+ serviceId);
					serviceRegistry.unpublishService(registration);
					fireRemoteServiceListeners(createUnregisteredEvent(registration));
				}
			}
		}
	}

	protected IRemoteServiceUnregisteredEvent createUnregisteredEvent(
			final RemoteServiceRegistrationImpl registration) {
		return new IRemoteServiceUnregisteredEvent() {

			public String[] getClazzes() {
				return registration.getClasses();
			}

			public ID getContainerID() {
				return registration.getContainerID();
			}

			public IRemoteServiceReference getReference() {
				return registration.getReference();
			}

			public String toString() {
				StringBuffer buf = new StringBuffer(
						"RemoteServiceUnregisteredEvent[");
				buf.append("containerID=")
						.append(registration.getContainerID());
				buf.append(";clazzes=").append(
						Arrays.asList(registration.getClasses()));
				buf.append(";reference=").append(registration.getReference())
						.append("]");
				return buf.toString();
			}
		};
	}

	/**
	 * End message send/handlers
	 */

	protected RemoteServiceRegistryImpl addRemoteRegistry(
			RemoteServiceRegistryImpl registry) {
		return (RemoteServiceRegistryImpl) remoteRegistrys.put(registry
				.getContainerID(), registry);
	}

	protected RemoteServiceRegistryImpl getRemoteRegistry(ID containerID) {
		return (RemoteServiceRegistryImpl) remoteRegistrys.get(containerID);
	}

	protected RemoteServiceRegistryImpl removeRemoteRegistry(ID containerID) {
		return (RemoteServiceRegistryImpl) remoteRegistrys.remove(containerID);
	}

	private RemoteServiceRegistrationImpl getLocalRegistrationForRequest(
			Request request) {
		synchronized (localRegistry) {
			return localRegistry.findRegistrationForServiceId(request
					.getServiceId());
		}
	}

	protected void trace(String msg) {
		Trace.trace(Activator.getDefault(),
				IRemoteServiceProviderDebugOptions.DEBUG, msg);
	}

	protected void traceException(String methodName, Throwable t) {
		Trace.catching(Activator.getDefault(),
				IRemoteServiceProviderDebugOptions.EXCEPTIONS_CATCHING,
				RegistrySharedObject.class, methodName, t);
	}

	protected List requests = Collections.synchronizedList(new ArrayList());

	protected boolean addRequest(Request request) {
		return requests.add(request);
	}

	protected Request findRequestForId(long requestId) {
		synchronized (requests) {
			for (Iterator i = requests.iterator(); i.hasNext();) {
				Request req = (Request) i.next();
				long reqId = req.getRequestId();
				if (reqId == requestId)
					return req;
			}
		}
		return null;
	}

	protected boolean removeRequest(Request request) {
		return requests.remove(request);
	}

	protected boolean handleSharedObjectMsg(SharedObjectMsg msg) {
		try {
			msg.invoke(this);
		} catch (Exception e) {
			logHandleError(MSG_INVOKE_ERROR_CODE, MSG_INVOKE_ERROR_MESSAGE, e);
		}
		return false;
	}

	private void logSendError(int code, String message, Throwable exception) {
		traceException(message, exception);
		Activator.getDefault().getLog().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, message,
						exception));
	}

	private void logHandleError(int code, String message, Throwable exception) {
		traceException(message, exception);
		Activator.getDefault().getLog().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, message,
						exception));
	}

}
