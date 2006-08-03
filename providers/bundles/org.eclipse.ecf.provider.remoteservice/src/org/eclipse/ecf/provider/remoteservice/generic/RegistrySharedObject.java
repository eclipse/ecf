package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.IOException;
import java.util.ArrayList;
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
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;

public class RegistrySharedObject extends AbstractSharedObject {

	private static final String HANDLE_FIRE_REQUEST = "handleFireRequest";
	
	private static final String HANDLE_CALL_REQUEST = "handleCallRequest";

	private static final String SEND_REGISTRY_UPDATE = "handleRegistryUpdate";

	private static final int SEND_REGISTRY_ERROR_CODE = 201;

	private static final String SEND_REGISTRY_UPDATE_ERROR_MESSAGE = "exception sending local registry message";

	private static final int MSG_INVOKE_ERROR_CODE = 202;

	private static final String MSG_INVOKE_ERROR_MESSAGE = "Exception in ";

	private static final int HANDLE_REQUEST_ERROR_CODE = 203;

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
		return remoteRegistrys.put(registry.getContainerID(), registry);
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
			trace(getLocalContainerID() + " sending "
					+ localRegistry);
			try {
				sendSharedObjectMsgTo(targetRemote, SharedObjectMsg.createMsg(
						SEND_REGISTRY_UPDATE, localRegistry));
			} catch (IOException e) {
				messageError(SEND_REGISTRY_ERROR_CODE,
						SEND_REGISTRY_UPDATE_ERROR_MESSAGE, e);
			}
		}
	}

	private Request createRequest(RemoteServiceRegistrationImpl remoteRegistration, IRemoteCall call) {
		RemoteServiceReferenceImpl refImpl = (RemoteServiceReferenceImpl) remoteRegistration.getReference();
		RemoteCallImpl remoteCall = RemoteCallImpl.createRemoteCall(refImpl.getRemoteClass(), call.getMethod(),call.getParameters(),call.getTimeout());
		return new Request(this.getLocalContainerID(),
				remoteRegistration.getServiceId(), remoteCall);
	}
	protected Request sendFireRequest(RemoteServiceRegistrationImpl remoteRegistration,
			IRemoteCall call) throws IOException {
		Request request = createRequest(remoteRegistration,call);
		sendSharedObjectMsgTo(remoteRegistration.getContainerID(), SharedObjectMsg.createMsg(
				HANDLE_FIRE_REQUEST, request));
		return request;
	}
	protected Request sendCallRequest(RemoteServiceRegistrationImpl remoteRegistration,
			IRemoteCall call) throws IOException {
		Request request = createRequest(remoteRegistration,call);
		synchronized (request) {
			sendSharedObjectMsgTo(remoteRegistration.getContainerID(), SharedObjectMsg.createMsg(
				HANDLE_CALL_REQUEST, request));
			addRequest(request);
		}
		return request;
	}

	private RemoteServiceRegistrationImpl getLocalRegistrationForRequest(Request request) {
		synchronized (localRegistry) {
			return localRegistry.findRegistrationForServiceId(request
					.getServiceId());
		}
	}
	protected void handleFireRequest(Request request) {
		trace("handleFireRequest(" + request + ") from "+request.getRequestContainerID());
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
		trace("handleCallRequest(" + request + ") from "+responseTarget);
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
			response = new Response(request.getRequestId(),e);
			// XXX log
			traceException("got local exception, sending response back to caller",e);
		}
		try {
			trace("sending response "+response+" to "+responseTarget);
			sendSharedObjectMsgTo(responseTarget,SharedObjectMsg.createMsg(HANDLE_RESPONSE,response));
		} catch (IOException e) {
			// XXX log
			traceException("Exception sending response ",e);
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
		trace("handleResponse("+response+")");
		Request request = findRequestForId(response.getRequestId());
		if (request == null) {
			// XXX Log and return
			trace("request not found for response "+response);
			return;
		} else {
			trace("request "+request+" found for response "+response);
			synchronized (request) {
				request.setResponse(response);
				request.setDone(true);
				request.notify();
			}
		}
	}
	
	protected List<Request> requests = Collections.synchronizedList(new ArrayList<Request>());
	
	protected boolean addRequest(Request request) {
		return requests.add(request);
	}
	protected Request findRequestForId(long requestId) {
		synchronized (requests) {
			for (Request i : requests) {
				long reqId = i.getRequestId();
				if (reqId==requestId) return i;
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
		trace(remoteClientID + " to " + getLocalContainerID()
				+ " received " + registry);
		addRemoteRegistry(registry);

	}

	protected boolean handleSharedObjectMsg(SharedObjectMsg msg) {
		try {
			msg.invoke(this);
		} catch (Exception e) {
			messageError(MSG_INVOKE_ERROR_CODE,
					MSG_INVOKE_ERROR_MESSAGE, e);
		}
		return false;
	}

	private void messageError(int code, String message, Throwable exception) {
		Activator.getDefault().getLog().log(
				new Status(IStatus.ERROR, Activator.PLUGIN_ID, code, message,
						exception));
	}

	public void addRemoteServiceListener(IRemoteServiceListener listener) {
		// TODO Auto-generated method stub

	}

	public IRemoteService getRemoteService(IRemoteServiceReference ref) {
		return new RemoteServiceImpl(this,getRemoteServiceRegistrationImpl(ref));
	}

	private RemoteServiceRegistrationImpl getRemoteServiceRegistrationImpl(IRemoteServiceReference reference) {
		RemoteServiceReferenceImpl refImpl = (RemoteServiceReferenceImpl) reference;
		return refImpl.getRegistration();
	}

	IRemoteServiceReference[] getRemoteServiceReferencesForRegistry(
			RemoteServiceRegistryImpl registry, String clazz, String filter) {
		return registry.lookupServiceReferences(clazz, createRemoteFilterFromString(filter));
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
			for (RemoteServiceRegistryImpl r : new ArrayList<RemoteServiceRegistryImpl>(remoteRegistrys.values())) {
				getReferencesListFromRegistry(clazz, remoteFilter, references, r);
			}
		} else {
			for(int i=0; i < idFilter.length; i++) {
				RemoteServiceRegistryImpl r = remoteRegistrys.get(idFilter[i]);
				if (r != null) getReferencesListFromRegistry(clazz, remoteFilter, references, r);
			}
		}
		return (IRemoteServiceReference []) references.toArray(new IRemoteServiceReference[references.size()]);
	}

	private void getReferencesListFromRegistry(String clazz, IRemoteFilter remoteFilter, List<IRemoteServiceReference> references, RemoteServiceRegistryImpl r) {
		IRemoteServiceReference[] rs = r.lookupServiceReferences(clazz, remoteFilter);
		if (rs != null) for(int j=0; j < rs.length; j++) references.add(rs[j]);
	}

	public IRemoteServiceRegistration registerRemoteService(String[] clazzes,
			Object service, Dictionary properties) {
		if (service == null)
			throw new NullPointerException("service cannot be null");
		int size = clazzes.length;

		if (size == 0) {
			throw new IllegalArgumentException("service classes list is empty");
		}

		String[] copy = new String[clazzes.length];
		for (int i = 0; i < clazzes.length; i++) {
			copy[i] = new String(clazzes[i].getBytes());
		}
		clazzes = copy;

		RemoteServiceRegistrationImpl reg = new RemoteServiceRegistrationImpl();
		reg.publish(localRegistry, service, clazzes, properties);

		notifyRemotesOfRegistryChange();
		return reg;
	}

	protected void notifyRemotesOfRegistryChange() {
		synchronized (localRegistry) {
			sendRegistryUpdateToAll();
		}
	}

	public void remoteRemoteServiceListener(IRemoteServiceListener listener) {
		// TODO Auto-generated method stub

	}

	public boolean ungetRemoteService(IRemoteServiceReference ref) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object fireCallAndWait(RemoteServiceRegistrationImpl registration, IRemoteCall call) throws ECFException {
		Request request = null;
		try {
			request = sendCallRequest(registration, call);
		} catch (IOException e) {
			return new ECFException("Exception sending request",e);
		}
		try {
			long timeout = call.getTimeout() + System.currentTimeMillis();
			while ((timeout-System.currentTimeMillis()) > 0) {
				Request r = findRequestForId(request.getRequestId());
				if (r == null)
					throw new ECFException("no request found for id "+request.getRequestId());
				trace("checking request "+request+" for done");
				synchronized (r) {
					if (r.isDone()) {
						removeRequest(request);
						trace("request "+request+" done");
						Response resp = r.getResponse();
						trace("request "+request+" returning");
						if (resp.hadException()) {
							trace("request "+request+" throwing exception ");
							throw new ECFException("Exception in response ",resp.getException());
						}
						else {
							Object result = resp.getResponse();
							trace("request "+request+" returning "+result);
							return result;
						}
					} else r.wait(DEFAULT_WAIT_INTERVAL);
				}
			}
			throw new ECFException("Request timed out after "+call.getTimeout()+" ms");
		} catch (InterruptedException e) {
			throw new ECFException("Wait for response interrupted",e);
		}		
	}
}
