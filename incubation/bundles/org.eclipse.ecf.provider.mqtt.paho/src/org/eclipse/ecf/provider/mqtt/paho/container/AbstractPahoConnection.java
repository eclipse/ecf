package org.eclipse.ecf.provider.mqtt.paho.container;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.provider.comm.AsynchEvent;
import org.eclipse.ecf.provider.comm.ConnectionEvent;
import org.eclipse.ecf.provider.comm.DisconnectEvent;
import org.eclipse.ecf.provider.comm.IConnectionListener;
import org.eclipse.ecf.provider.comm.ISynchAsynchConnection;
import org.eclipse.ecf.provider.comm.ISynchAsynchEventHandler;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoID;
import org.eclipse.ecf.provider.mqtt.paho.identity.PahoNamespace;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public abstract class AbstractPahoConnection  implements
ISynchAsynchConnection {

	protected PahoID clientID;
	protected MqttClient client;
	protected String mqttTopic;
	protected boolean connected;
	protected boolean started;
	protected List<IConnectionListener> connectionListeners = new ArrayList<IConnectionListener>();
	protected ISynchAsynchEventHandler handler;

	protected void fireListenersConnect(ConnectionEvent event) {
		List<IConnectionListener> toNotify = null;
		synchronized (connectionListeners) {
			toNotify = new ArrayList<IConnectionListener>(connectionListeners);
		}
		for (Iterator<IConnectionListener> i = toNotify.iterator(); i.hasNext();) {
			IConnectionListener l = (IConnectionListener) i.next();
			l.handleConnectEvent(event);
		}
	}

	protected void fireListenersDisconnect(DisconnectEvent event) {
		List<IConnectionListener> toNotify = null;
		synchronized (connectionListeners) {
			toNotify = new ArrayList<IConnectionListener>(connectionListeners);
		}
		for (Iterator<IConnectionListener> i = toNotify.iterator(); i.hasNext();) {
			IConnectionListener l = (IConnectionListener) i.next();
			l.handleDisconnectEvent(event);
		}
	}

	public void start() {
		this.started = true;
	}

	public void stop() {
		this.started = false;
	}

	public boolean isStarted() {
		return started;
	}

	protected boolean isActive() {
		return isConnected() && isStarted();
	}

	protected String getMqttTopic() {
		return this.mqttTopic;
	}

	public AbstractPahoConnection(PahoID clientID,
			ISynchAsynchEventHandler handler) {
		if (clientID == null)
			throw new IllegalArgumentException("clientID cannot be null");
		this.clientID = clientID;
		if (handler == null)
			throw new IllegalArgumentException("handler cannot be null");
		this.handler = handler;
	}

	protected MqttCallback callback = new MqttCallback() {
		public void connectionLost(Throwable cause) {
			handleConnectionLost(cause);
		}

		public void messageArrived(String topic, MqttMessage message)
				throws Exception {
			handleMessageArrived(topic, message);
		}

		public void deliveryComplete(IMqttDeliveryToken token) {
			handleDeliveryComplete(token);
		}
	};

	protected void handleConnectionLost(Throwable cause) {
		// xxx todo
	}

	protected void handleDeliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
	}

	protected synchronized void connectAndSubscribe(PahoID targetID,
			MqttConnectOptions opts) throws ECFException {
		// Create client
		try {
			this.client = new MqttClient(targetID.getServerURI(),
					targetID.getClientId());
			// Set callback
			this.client.setCallback(callback);
			// Connect to broker with connectOpts
			if (opts == null)
				this.client.connect();
			else
				this.client.connect(opts);
			// Subscribe to topic
			this.client.subscribe(targetID.getClientId());
			this.connected = true;
		} catch (MqttException e) {
			throw new ECFException("Could not connect to targetID"
					+ targetID.getName());
		}
	}

	@SuppressWarnings("rawtypes")
	public Map getProperties() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.comm.IConnection#addCommEventListener(org.eclipse
	 * .ecf.core.comm.IConnectionListener)
	 */
	public void addListener(IConnectionListener listener) {
		connectionListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ecf.core.comm.IConnection#removeCommEventListener(org.eclipse
	 * .ecf.core.comm.IConnectionListener)
	 */
	public void removeListener(IConnectionListener listener) {
		connectionListeners.remove(listener);
	}

	public ID getLocalID() {
		return clientID;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	protected MqttConnectOptions createConnectionOptions(ID targetID,
			Object data, int timeout) {
		MqttConnectOptions options = new MqttConnectOptions();
		options.setCleanSession(true);
		options.setConnectionTimeout(timeout);
		options.setKeepAliveInterval(timeout);
		return options;
	}

	public boolean isConnected() {
		return connected;
	}

	protected void publishMessage(PahoMessage message) throws IOException {
		try {
			this.client.publish(getMqttTopic(), message.toMqttMessage());
		} catch (MqttException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	public synchronized void disconnect() {
		if (isConnected()) {
			try {
				this.client.disconnect();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.client = null;
			this.connected = false;
		}
	}

	public synchronized void sendAsynch(ID receiver, byte[] data)
			throws IOException {
		if (!isActive())
			throw new IOException("PahoConnection not connected");
		if (receiver != null
				&& !PahoNamespace.NAME
						.equals(receiver.getNamespace().getName()))
			throw new IOException("receiver not in PahoID namespace");
		publishMessage(new AsyncPahoMessage((PahoID) getLocalID(),
				(PahoID) receiver, data));
	}

	protected Object synch = new Object();
	protected boolean waitDone;
	protected Serializable reply;
	protected int connectWaitDuration = 15000;

	protected PahoID getLocalPahoID() {
		return (PahoID) getLocalID();
	}

	public Object sendSynch(ID receiver, byte[] data) throws IOException {
		if (!isActive())
			throw new IOException("PahoConnection not connected");
		if (receiver == null)
			throw new IOException("receiver id must not be null");
		if (receiver != null
				&& !PahoNamespace.NAME
						.equals(receiver.getNamespace().getName()))
			throw new IOException("receiver not in PahoID namespace");
		return sendAndWait(new SyncRequestMessage(getLocalPahoID(),
				(PahoID) receiver, data), connectWaitDuration);
	}

	private String PLUGIN_ID = "org.eclipse.ecf.provider.mqtt.paho";

	protected void throwIOException(String method, String msg, Throwable t)
			throws IOException {
		Trace.throwing(PLUGIN_ID, PahoDebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), method, t);
		throw new IOException(msg + ": " + t.getMessage()); //$NON-NLS-1$
	}

	protected void traceAndLogExceptionCatch(int code, String method,
			Throwable e) {
		Trace.catching(PLUGIN_ID, PahoDebugOptions.EXCEPTIONS_CATCHING,
				this.getClass(), method, e);
	}

	private Serializable sendAndWait(SyncRequestMessage syncRequestMessage,
			int waitDuration) throws IOException {
		synchronized (synch) {
			try {
				waitDone = false;
				long waittimeout = System.currentTimeMillis() + waitDuration;
				reply = null;
				this.client.publish(getMqttTopic(),
						syncRequestMessage.toMqttMessage());
				while (!waitDone
						&& (waittimeout - System.currentTimeMillis() > 0)) {
					synch.wait(waitDuration / 10);
				}
				waitDone = true;
				if (reply == null)
					throw new IOException("timeout waiting for response"); //$NON-NLS-1$
			} catch (MqttException e) {
				Trace.catching(PLUGIN_ID, PahoDebugOptions.EXCEPTIONS_CATCHING,
						this.getClass(), "sendAndWait", e); //$NON-NLS-1$
				throwIOException("sendAndWait", "MqttException in sendAndWait", //$NON-NLS-1$ //$NON-NLS-2$
						e);
			} catch (InterruptedException e) {
				traceAndLogExceptionCatch(IStatus.ERROR,
						"handleTopicMessage", e); //$NON-NLS-1$
			}
			return reply;
		}
	}

	protected void handleMessageArrived(String topic, MqttMessage message) {
		if (!isActive())
			return;
		if (topic.equals(this.getMqttTopic()))
			return;
		Object response = null;
		try {
			response = PahoMessage.deserialize(message.getPayload());
			if (response instanceof PahoMessage) {
				PahoMessage pahoMessage = (PahoMessage) response;
				PahoID fromID = pahoMessage.getFromID();
				if (fromID == null) {
					Trace.exiting(PLUGIN_ID, PahoDebugOptions.METHODS_ENTERING,
							this.getClass(),
							"fromID=null...ignoring PahoMessage " + pahoMessage); //$NON-NLS-1$
					return;
				}
				if (fromID.equals(getLocalID())) {
					Trace.exiting(
							PLUGIN_ID,
							PahoDebugOptions.METHODS_ENTERING,
							this.getClass(),
							"fromID=localID...ignoring PahoMessage " + pahoMessage); //$NON-NLS-1$
					return;
				}
				PahoID targetID = pahoMessage.getTargetID();
				if (targetID == null) {
					if (pahoMessage instanceof AsyncPahoMessage)
						handleTopicMessage((AsyncPahoMessage) pahoMessage);
					else
						Trace.trace(PLUGIN_ID,
								"onMessage.received invalid message to group"); //$NON-NLS-1$
				} else {
					if (targetID.equals(getLocalID())) {
						if (pahoMessage instanceof AsyncPahoMessage)
							handleTopicMessage((AsyncPahoMessage) pahoMessage);
						else if (pahoMessage instanceof SyncRequestMessage)
							handleSynchRequest((SyncRequestMessage) pahoMessage);
						else if (pahoMessage instanceof SyncResponseMessage)
							handleSynchResponse((SyncResponseMessage) pahoMessage);
						else
							Trace.trace(
									PLUGIN_ID,
									"onMessage.msg invalid message to " + targetID); //$NON-NLS-1$
					} else
						Trace.trace(
								PLUGIN_ID,
								"onMessage.msg " + pahoMessage + " not intended for " + targetID); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else
				// received bogus message...ignore
				Trace.trace(
						PLUGIN_ID,
						"onMessage: received non-ECFMessage...ignoring " + response); //$NON-NLS-1$
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void handleTopicMessage(AsyncPahoMessage message) {
		if (isActive()) {
			try {
				this.handler.handleAsynchEvent(new AsynchEvent(this,message.getData()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected void handleSynchResponse(SyncResponseMessage pahoMessage) {
		synchronized (this.synch) {
			this.reply = pahoMessage.getData();
			this.waitDone = true;
			this.synch.notify();
		}
	}

	protected abstract void handleSynchRequest(SyncRequestMessage pahoMessage);

}
