package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.AbstractSharedObject;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.remoteservice.Activator;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteCallListener;
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.events.IRemoteCallCompleteEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteCallStartEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceEvent;
import org.eclipse.ecf.remoteservice.events.IRemoteServiceRegisterEvent;

public class RegistrySharedObject extends AbstractSharedObject {

	private static final String HANDLE_FIRE_REQUEST = "handleFireRequest";

	private static final String HANDLE_CALL_REQUEST = "handleCallRequest";

	private static final String SEND_REGISTRY_UPDATE = "handleRegistryUpdate";
	
	private static final String SEND_UNREGISTER = "handleUnregister";

	private static final int SEND_REGISTRY_ERROR_CODE = 201;

	private static final String SEND_REGISTRY_UPDATE_ERROR_MESSAGE = "exception sending local registry message";

	private static final String SEND_UNREGISTER_ERROR_MESSAGE = "exception sending service unregister message";

	private static final int MSG_INVOKE_ERROR_CODE = 202;

	private static final String MSG_INVOKE_ERROR_MESSAGE = "Exception in ";

	private static final int HANDLE_REQUEST_ERROR_CODE = 203;
	
	private static final int SEND_UNREGISTER_ERROR_CODE = 204;

	private static final String HANDLE_REQUEST_ERROR_MESSAGE = "Exception locally invoking remote call";

	private static final String HANDLE_RESPONSE = "handleResponse";

	private static final long DEFAULT_WAIT_INTERVAL = 5000;

	protected RemoteServiceRegistryImpl localRegistry;

	protected Map<ID, RemoteServiceRegistryImpl> remoteRegistrys = Collections
			.synchronizedMap(new HashMap<ID, RemoteServiceRegistryImpl>());

	public RegistrySharedObject() {
	}

	protected RemoteServiceRegistryImpl addRemoteRegistry(
			RemoteServiceRegistryImpl registry) {
		notifyListenersAddRemoteRegistry(registry);
		return remoteRegistrys.put(registry.getContainerID(), registry);
	}

	private void notifyListenersAddRemoteRegistry(RemoteServiceRegistryImpl registry) {
		RemoteServiceRegistrationImpl [] registrations = registry.getRegistrations();
		for(int i=0; i < registrations.length; i++) fireRemoteServiceListeners(createAddServiceEvent(registrations[i]));
	}

	private IRemoteServiceEvent createAddServiceEvent(final RemoteServiceRegistrationImpl registration) {
		return new IRemoteServiceRegisterEvent() {

			public String[] getClazzes() {
				return registration.getClasses();
			}
			public ID getContainerID() {
				return registration.getContainerID();
			}
			public String toString() {
				StringBuffer buf = new StringBuffer("RemoteServiceRegisterEvent[");
				buf.append("clazzes=").append(
						Arrays.asList(registration.getClasses())).append(
						";containerID=").append(registration.getContainerID())
						.append("]");
				return buf.toString();
			}
		};
	}

	protected RemoteServiceRegistryImpl getRemoteRegistry(ID containerID) {
		return remoteRegistrys.get(containerID);
	}

	protected RemoteServiceRegistryImpl removeRemoteRegistry(ID containerID) {
		return remoteRegistrys.remove(containerID);
	}

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
		localRegistry = new RemoteServiceRegistryImpl(getHomeContainerID());
	}

	protected void handleContainerDisconnectedEvent(
			IContainerDisconnectedEvent event) {
		removeRemoteRegistry(event.getTargetID());
	}

	protected void handleContainerConnectedEvent(IContainerConnectedEvent event) {
		sendRegistryUpdateToAll();
	}

	private void sendRegistryUpdateToAll() {
		sendRegistryUpdate(null);
	}

	private void sendRegistryUpdate(ID targetRemote) {
		synchronized (localRegistry) {
			trace(getLocalContainerID() + " sending " + localRegistry);
			try {
				sendSharedObjectMsgTo(targetRemote, SharedObjectMsg.createMsg(
						SEND_REGISTRY_UPDATE, localRegistry));
			} catch (IOException e) {
				messageError(SEND_REGISTRY_ERROR_CODE,
						SEND_REGISTRY_UPDATE_ERROR_MESSAGE, e);
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

	protected Request sendFireRequest(
			RemoteServiceRegistrationImpl remoteRegistration, IRemoteCall call)
			throws IOException {
		Request request = createRequest(remoteRegistration, call, null);
		sendSharedObjectMsgTo(remoteRegistration.getContainerID(),
				SharedObjectMsg.createMsg(HANDLE_FIRE_REQUEST, request));
		return request;
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
					SharedObjectMsg.createMsg(HANDLE_CALL_REQUEST, request));
		} catch (IOException e) {
			// XXX LOG
			removeRequest(request);
			fireCallCompleteEvent(listener, request.getRequestId(), null, true,
					e);
		}
	}

	protected long sendCallRequest(
			RemoteServiceRegistrationImpl remoteRegistration,
			final IRemoteCall call) throws IOException {
		Request request = createRequest(remoteRegistration, call, null);
		addRequest(request);
		try {
			sendSharedObjectMsgTo(remoteRegistration.getContainerID(),
					SharedObjectMsg.createMsg(HANDLE_CALL_REQUEST, request));
		} catch (IOException e) {
			removeRequest(request);
			throw e;
		}
		return request.getRequestId();
	}

	private RemoteServiceRegistrationImpl getLocalRegistrationForRequest(
			Request request) {
		synchronized (localRegistry) {
			return localRegistry.findRegistrationForServiceId(request
					.getServiceId());
		}
	}

	protected void handleFireRequest(Request request) {
		trace("handleFireRequest(" + request + ") from "
				+ request.getRequestContainerID());
		RemoteServiceRegistrationImpl localRegistration = getLocalRegistrationForRequest(request);
		if (localRegistration == null) {
			handleNoLocalService();
		}
		// Else we've got a local service and we invoke it
		RemoteCallImpl call = request.getCall();
		try {
			localRegistration.callService(call);
		} catch (Exception e) {
			messageError(HANDLE_REQUEST_ERROR_CODE,
					HANDLE_REQUEST_ERROR_MESSAGE, e);
		}
	}

	protected void handleCallRequest(Request request) {
		ID responseTarget = request.getRequestContainerID();
		trace("handleCallRequest(" + request + ") from " + responseTarget);
		RemoteServiceRegistrationImpl localRegistration = getLocalRegistrationForRequest(request);
		if (localRegistration == null) {
			handleNoLocalService();
		}
		// Else we've got a local service and we invoke it
		RemoteCallImpl call = request.getCall();
		Response response = null;
		Object result = null;
		try {
			result = localRegistration.callService(call);
			response = new Response(request.getRequestId(), result);
		} catch (Exception e) {
			response = new Response(request.getRequestId(), e);
			// XXX log
			traceException(
					"got local exception, sending response back to caller", e);
		}
		try {
			trace("sending response " + response + " to " + responseTarget);
			sendSharedObjectMsgTo(responseTarget, SharedObjectMsg.createMsg(
					HANDLE_RESPONSE, response));
		} catch (IOException e) {
			// XXX log
			traceException("Exception sending response ", e);
		}
	}

	protected void trace(String msg) {
		System.out.println(msg);
	}

	protected void traceException(String msg, Throwable t) {
		System.err.println(msg);
		t.printStackTrace(System.err);
	}

	protected void handleResponse(Response response) {
		trace("handleResponse(" + response + ")");
		Request request = findRequestForId(response.getRequestId());
		if (request == null) {
			// XXX Log and return
			trace("request not found for response " + response);
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

	protected List<Request> requests = Collections
			.synchronizedList(new ArrayList<Request>());

	protected boolean addRequest(Request request) {
		return requests.add(request);
	}

	protected Request findRequestForId(long requestId) {
		synchronized (requests) {
			for (Request i : requests) {
				long reqId = i.getRequestId();
				if (reqId == requestId)
					return i;
			}
		}
		return null;
	}

	protected boolean removeRequest(Request request) {
		return requests.remove(request);
	}

	private void handleNoLocalService() {
		// TODO Auto-generated method stub

	}

	protected void handleRegistryUpdate(RemoteServiceRegistryImpl registry) {
		ID remoteClientID = registry.getContainerID();
		if (remoteClientID == null)
			throw new NullPointerException(
					"registry received with null client ID, discarding");
		if (getLocalContainerID().equals(remoteClientID))
			return;
		trace(remoteClientID + " to " + getLocalContainerID() + " received "
				+ registry);
		addRemoteRegistry(registry);

	}

	protected boolean handleSharedObjectMsg(SharedObjectMsg msg) {
		try {
			msg.invoke(this);
		} catch (Exception e) {
			messageError(MSG_INVOKE_ERROR_CODE, MSG_INVOKE_ERROR_MESSAGE, e);
		}
		return false;
	}

	private void messageError(int code, String message, Throwable exception) {
		Activator.getDefault().getLog().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, message,
						exception));
	}

	protected List<IRemoteServiceListener> serviceListeners = new ArrayList<IRemoteServiceListener>();
	
	public void addRemoteServiceListener(IRemoteServiceListener listener) {
		synchronized (serviceListeners) {
			serviceListeners.add(listener);
		}
	}

	public void removeRemoteServiceListener(IRemoteServiceListener listener) {
		synchronized (serviceListeners) {
			serviceListeners.remove(listener);
		}
	}

	protected void fireRemoteServiceListeners(IRemoteServiceEvent event) {
		synchronized (serviceListeners) {
			for (IRemoteServiceListener l : serviceListeners) {
				l.handleServiceEvent(event);
			}
		}
	}

	public IRemoteService getRemoteService(IRemoteServiceReference ref) {
		return new RemoteServiceImpl(this,
				getRemoteServiceRegistrationImpl(ref));
	}

	private RemoteServiceRegistrationImpl getRemoteServiceRegistrationImpl(
			IRemoteServiceReference reference) {
		RemoteServiceReferenceImpl refImpl = (RemoteServiceReferenceImpl) reference;
		return refImpl.getRegistration();
	}

	IRemoteServiceReference[] getRemoteServiceReferencesForRegistry(
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

	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter,
			String clazz, String filter) {
		IRemoteFilter remoteFilter = createRemoteFilterFromString(filter);
		List<IRemoteServiceReference> references = new ArrayList<IRemoteServiceReference>();
		if (idFilter == null) {
			for (RemoteServiceRegistryImpl r : new ArrayList<RemoteServiceRegistryImpl>(
					remoteRegistrys.values())) {
				getReferencesListFromRegistry(clazz, remoteFilter, references,
						r);
			}
		} else {
			for (int i = 0; i < idFilter.length; i++) {
				RemoteServiceRegistryImpl r = remoteRegistrys.get(idFilter[i]);
				if (r != null)
					getReferencesListFromRegistry(clazz, remoteFilter,
							references, r);
			}
		}
		return (IRemoteServiceReference[]) references
				.toArray(new IRemoteServiceReference[references.size()]);
	}

	private void getReferencesListFromRegistry(String clazz,
			IRemoteFilter remoteFilter,
			List<IRemoteServiceReference> references,
			RemoteServiceRegistryImpl r) {
		IRemoteServiceReference[] rs = r.lookupServiceReferences(clazz,
				remoteFilter);
		if (rs != null)
			for (int j = 0; j < rs.length; j++)
				references.add(rs[j]);
	}

	public IRemoteServiceRegistration registerRemoteService(String[] clazzes,
			Object service, Dictionary properties) {
		if (service == null)
			throw new NullPointerException("service cannot be null");
		int size = clazzes.length;

		if (size == 0) throw new IllegalArgumentException("service classes list is empty");

		String[] copy = new String[clazzes.length];
		for (int i = 0; i < clazzes.length; i++) {
			copy[i] = new String(clazzes[i].getBytes());
		}
		clazzes = copy;

		String invalidService = checkServiceClass(clazzes, service);
		if (invalidService != null) throw new IllegalArgumentException("Service is not valid: "+ invalidService); 

		RemoteServiceRegistrationImpl reg = new RemoteServiceRegistrationImpl();
		reg.publish(this, localRegistry, service, clazzes, properties);

		notifyRemotesOfRegistryChange();
		return reg;
	}

	protected void notifyRemotesOfRegistryChange() {
		synchronized (localRegistry) {
			sendRegistryUpdateToAll();
		}
	}
	
	protected void unregister(RemoteServiceRegistrationImpl serviceRegistration) {
		synchronized (localRegistry) {
			localRegistry.unpublishService(serviceRegistration);
			sendUnregisterToAll(new Long(serviceRegistration.getServiceId()));
			notifyLocalOfUnregister(serviceRegistration.getContainerID(), serviceRegistration);
		}
	}
	
	private void notifyLocalOfUnregister(ID registryContainer, RemoteServiceRegistrationImpl serviceRegistration) {
		trace("notifyLocalOfUnregister("+registryContainer+","+serviceRegistration+")");
		// TODO Auto-generated method stub
	}

	private void sendUnregisterToAll(Long serviceId) {
		trace("sendUnregisterToAll("+serviceId+")");
		try {
			this.sendSharedObjectMsgTo(null, SharedObjectMsg.createMsg(SEND_UNREGISTER, new Object[] { serviceId }));
		} catch (IOException e) {
			messageError(SEND_UNREGISTER_ERROR_CODE,
					SEND_UNREGISTER_ERROR_MESSAGE, e);
		}
	}

	protected void handleUnregister(Long serviceId) {
		trace("handleUnregister("+serviceId+")");
		synchronized (remoteRegistrys) {
			for (ID i : remoteRegistrys.keySet()) {
				RemoteServiceRegistryImpl serviceRegistry = remoteRegistrys.get(i);
				RemoteServiceRegistrationImpl registration = serviceRegistry.findRegistrationForServiceId(serviceId.longValue());
				if (registration != null) {
					trace("handleUnregister...FOUND IT...UNPUBLISHING "+serviceId);
					serviceRegistry.unpublishService(registration);
					notifyLocalOfUnregister(registration.getContainerID(), registration);
				}
			}
		}
	}
	public boolean ungetRemoteService(IRemoteServiceReference ref) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object fireCallAndWait(RemoteServiceRegistrationImpl registration,
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
						trace("Waiting " + DEFAULT_WAIT_INTERVAL
								+ " for response to request: " + request);
						request.wait(DEFAULT_WAIT_INTERVAL);
					}
				}
			}
			if (!doneWaiting)
				throw new ECFException("Request timed out after "
						+ call.getTimeout() + " ms");
		} catch (IOException e) {
			// XXX log
			throw new ECFException("Exception sending request", e);
		} catch (InterruptedException e) {
			// XXX log
			throw new ECFException("Wait for response interrupted", e);
		} catch (ECFException e) {
			// XXX log
			throw e;
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

}
