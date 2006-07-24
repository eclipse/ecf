package org.eclipse.ecf.provider.remoteservice.generic;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.ISharedObjectMessageEvent;
import org.eclipse.ecf.core.events.RemoteSharedObjectEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.AbstractSharedObject;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsg;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsgEvent;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.provider.remoteservice.generic.registry.RemoteServiceRegistrationImpl;
import org.eclipse.ecf.provider.remoteservice.generic.registry.RemoteServiceRegistryImpl;
import org.eclipse.ecf.remoteservice.Activator;
import org.eclipse.ecf.remoteservice.IRemoteFilter;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceListener;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;

public class RegistrySharedObject extends AbstractSharedObject {

	private static final String HANDLE_REQUEST = "handleRequest";

	private static final String SEND_REGISTRY_UPDATE = "handleRegistryUpdate";

	private static final int SEND_REGISTRY_ERROR_CODE = 201;

	private static final String SEND_REGISTRY_UPDATE_ERROR_MESSAGE = "exception sending local registry message";

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
			public boolean acceptEvent(Event arg0) {
				return true;
			}

			public Event processEvent(Event arg0) {
				if (arg0 instanceof IContainerConnectedEvent)
					handleContainerConnectedEvent((IContainerConnectedEvent) arg0);
				else if (arg0 instanceof IContainerDisconnectedEvent)
					handleContainerDisconnectedEvent((IContainerDisconnectedEvent) arg0);
				return null;
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
			System.out.println(getLocalContainerID() + " sending "
					+ localRegistry);
			SharedObjectMsg msg = SharedObjectMsg.createMsg(
					SEND_REGISTRY_UPDATE, localRegistry);
			try {
				sendSharedObjectMsgTo(targetRemote, msg);
			} catch (Exception e) {
				messageError(SEND_REGISTRY_ERROR_CODE,
						SEND_REGISTRY_UPDATE_ERROR_MESSAGE, e);
			}
		}
	}

	/**
	 * Send a
	 * 
	 * @param targetContainer
	 * @param request
	 * @throws IOException
	 */
	private void sendRequest(ID targetContainer,
			RemoteServiceRegistrationImpl remoteRegistration,
			RemoteCallImpl call) throws IOException {
		Request request = new Request(this.getLocalContainerID(),
				remoteRegistration.getServiceId(), call);
		sendSharedObjectMsgTo(targetContainer, SharedObjectMsg.createMsg(
				HANDLE_REQUEST, request));
	}

	protected void handleRequest(Request request) {
		System.out.println("received remote call request: " + request);
		RemoteServiceRegistrationImpl reg = null;
		synchronized (localRegistry) {
			reg = localRegistry.findRegistrationForServiceId(request
					.getServiceId());
		}
		RemoteServiceRegistrationImpl localRegistration = reg;
		if (localRegistration == null) {
			handleNoLocalService();
		}
		// Else we've got a local service and we invoke it
		RemoteCallImpl call = request.getCall();
		try {
			localRegistration.callService(call);
		} catch (Exception e) {
			// XXX handle properly
			e.printStackTrace();
		}
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
		System.out.println(remoteClientID + " to " + getLocalContainerID()
				+ " received " + registry);
		addRemoteRegistry(registry);

		// XXX TESTING
		/*
		 * RemoteServiceRegistrationImpl reg =
		 * registry.findRegistrationForServiceId(0); if (reg != null) {
		 * System.out.println("testing: sending invoke for registration "+reg);
		 * try { sendCallRequest(reg.getContainerID(), reg, RemoteCallImpl
		 * .createRemoteCall("java.lang.Runnable", "run")); } catch (Exception
		 * e) { e.printStackTrace(); // TODO: handle exception } }
		 */
	}

	protected Event handleSharedObjectMsgEvent(ISharedObjectMessageEvent event) {
		Object data = null;
		if (event instanceof RemoteSharedObjectEvent) {
			RemoteSharedObjectEvent rsoe = (RemoteSharedObjectEvent) event;
			data = rsoe.getData();
			if (data instanceof SharedObjectMsgEvent) {
				SharedObjectMsgEvent some = (SharedObjectMsgEvent) data;
				try {
					some.getSharedObjectMsg().invoke(this);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		return event;
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
		// TODO Auto-generated method stub
		return null;
	}

	IRemoteServiceReference[] getRemoteServiceReferencesForRegistry(
			RemoteServiceRegistryImpl registry, String clazz, String filter) {
		// XXX make remote filter
		IRemoteFilter remoteFilter = null;
		return registry.lookupServiceReferences(clazz, remoteFilter);
	}

	public IRemoteServiceReference[] getRemoteServiceReferences(ID[] idFilter,
			String clazz, String filter) {
		// TODO Auto-generated method stub
		return null;
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
}
