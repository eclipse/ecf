/*
 * Created on Dec 16, 2004
 *  
 */
package org.eclipse.ecf.provider.generic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.eclipse.ecf.core.IOSGIService;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.ISharedObjectContainerConfig;
import org.eclipse.ecf.core.ISharedObjectContainerListener;
import org.eclipse.ecf.core.ISharedObjectContainerTransaction;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectAddException;
import org.eclipse.ecf.core.SharedObjectContainerJoinException;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.comm.AsynchConnectionEvent;
import org.eclipse.ecf.core.comm.ConnectionEvent;
import org.eclipse.ecf.core.comm.DisconnectConnectionEvent;
import org.eclipse.ecf.core.comm.IConnection;
import org.eclipse.ecf.core.comm.ISynchAsynchConnectionEventHandler;
import org.eclipse.ecf.core.comm.SynchConnectionEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.provider.Trace;
import org.eclipse.ecf.provider.generic.gmm.Member;

public abstract class SOContainer implements ISharedObjectContainer {

	static Trace debug = Trace.create("container");

	public static final String DEFAULT_OBJECT_ARG_KEY = SOContainer.class
			.getName()
			+ ".sharedobjectargs";

	public static final String DEFAULT_OBJECT_ARGTYPES_KEY = SOContainer.class
			.getName()
			+ ".sharedobjectargs";

	private long sequenceNumber = 0L;

	private Vector listeners = null;

	protected ISharedObjectContainerConfig config = null;

	protected SOContainerGMM groupManager = null;

	protected ThreadGroup loadingThreadGroup = null;

	protected ThreadGroup sharedObjectThreadGroup = null;

	protected SOManager sharedObjectManager = null;

	protected boolean isClosing = false;

	protected MessageReceiver receiver;

	protected void debug(String msg) {
		if (Trace.ON && debug != null) {
			debug.msg(msg + ":" + config.getID());
		}
	}

	protected void dumpStack(String msg, Throwable e) {
		if (Trace.ON && debug != null) {
			debug.dumpStack(e, msg + ":" + config.getID());
		}
	}

	protected byte[] getBytesForObject(Serializable obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		return bos.toByteArray();
	}

	protected ContainerMessage getObjectFromBytes(byte[] bytes)
			throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object obj = null;
		try {
			obj = ois.readObject();
		} catch (ClassNotFoundException e) {
			// what to do?
			dumpStack("class not found for message", e);
			return null;
		}
		if (obj instanceof ContainerMessage) {
			return (ContainerMessage) obj;
		} else {
			// what to do
			return null;
		}
	}

	protected void killConnection(IConnection conn) {
		debug("killconnection");
		try {
			if (conn != null)
				conn.disconnect();
		} catch (IOException e) {
			// XXX log
		}
	}

	protected void memberLeave(ID target, IConnection conn) {
		debug("memberLeave:" + target + ":" + conn);
		if (conn != null)
			killConnection(conn);
	}

	protected void fireContainerEvent(IContainerEvent event) {
		synchronized (listeners) {
			for (Iterator i = listeners.iterator(); i.hasNext();)
				((ISharedObjectContainerListener) i.next()).handleEvent(event);
		}
	}

	protected long getNextSequenceNumber() {
		if (sequenceNumber == Long.MAX_VALUE) {
			sequenceNumber = 0;
			return sequenceNumber;
		} else
			return sequenceNumber++;
	}

	protected Object getGroupMembershipLock() {
		return groupManager;
	}

	public ID[] getOtherMemberIDs() {
		return groupManager.getOtherMemberIDs();
	}

	protected ISharedObject getSharedObject(ID id) {
		SOWrapper wrap = getSharedObjectWrapper(id);
		if (wrap == null)
			return null;
		else
			return wrap.getSharedObject();
	}

	protected ISharedObject addSharedObjectAndWait(SharedObjectDescription sd,
			ISharedObject s, ISharedObjectContainerTransaction t)
			throws Exception {
		if (sd.getID() == null || s == null)
			return null;
		ISharedObject so = addSharedObject0(sd, s);
		// Wait right here until committed
		if (t != null)
			t.waitToCommit();
		return s;
	}

	protected ISharedObject addSharedObject0(SharedObjectDescription sd,
			ISharedObject s) throws Exception {
		addSharedObjectWrapper(makeNewSharedObjectWrapper(sd, s));
		return s;
	}

	protected SOWrapper makeNewSharedObjectWrapper(SharedObjectDescription sd,
			ISharedObject s) {
		SOConfig newConfig = makeNewSharedObjectConfig(sd, this);
		return new SOWrapper(newConfig, s, this);
	}

	protected SOConfig makeNewSharedObjectConfig(SharedObjectDescription sd,
			SOContainer cont) {
		ID homeID = sd.getHomeID();
		if (homeID == null)
			homeID = getID();
		return new SOConfig(sd.getID(), homeID, this, sd.getProperties());
	}

	protected SOWrapper getSharedObjectWrapper(ID id) {
		return groupManager.getFromActive(id);
	}

	protected void addSharedObjectWrapper(SOWrapper wrapper) throws Exception {
		if (wrapper == null)
			return;
		ID id = wrapper.getObjID();
		synchronized (getGroupMembershipLock()) {
			Object obj = groupManager.getFromAny(id);
			if (obj != null) {
				throw new SharedObjectAddException("SharedObject with id "
						+ id.getName() + " already in container");
			}
			// Call initialize. If this throws it halts everything
			wrapper.init();
			// Put in table
			groupManager.addSharedObjectToActive(wrapper);
		}
	}

	protected ISharedObject removeSharedObject(ID id) {
		synchronized (getGroupMembershipLock()) {
			SOWrapper wrap = groupManager.getFromActive(id);
			if (wrap == null)
				return null;
			groupManager.removeSharedObject(id);
			return wrap.getSharedObject();
		}
	}

	protected boolean addNewRemoteMember(ID memberID, Object data) {
		debug("addNewRemoteMember:" + memberID);
		return groupManager.addMember(new Member(memberID, data));
	}

	abstract protected void queueContainerMessage(ContainerMessage mess)
			throws IOException;

	abstract protected void forwardToRemote(ID from, ID to,
			ContainerMessage data) throws IOException;

	abstract protected void forwardExcluding(ID from, ID excluding,
			ContainerMessage data) throws IOException;

	protected final void forward(ID fromID, ID toID, ContainerMessage data)
			throws IOException {
		if (toID == null) {
			forwardExcluding(fromID, fromID, data);
		} else {
			forwardToRemote(fromID, toID, data);
		}
	}

	protected boolean removeRemoteMember(ID remoteMember) {
		return groupManager.removeMember(remoteMember);
	}

	protected void sendMessage(ContainerMessage data) throws IOException {
		debug("sendcontainermessage:" + data);
		synchronized (getGroupMembershipLock()) {
			ID ourID = getID();
			// We don't send to ourselves
			if (!ourID.equals(data.getToContainerID()))
				queueContainerMessage(data);
		}
	}

	protected ID[] sendCreateSharedObjectMessage(ID toContainerID,
			SharedObjectDescription sd) throws IOException {
		ID[] returnIDs = null;
		if (toContainerID == null) {
			synchronized (getGroupMembershipLock()) {
				// Send message to all
				sendMessage(ContainerMessage.makeSharedObjectCreateMessage(
						getID(), toContainerID, getNextSequenceNumber(), sd));
				returnIDs = getOtherMemberIDs();
			}
		} else {
			// If the create msg is directed to this space, no msg will be sent
			if (getID().equals(toContainerID)) {
				returnIDs = new ID[0];
			} else {
				sendMessage(ContainerMessage.makeSharedObjectCreateMessage(
						getID(), toContainerID, getNextSequenceNumber(), sd));
				returnIDs = new ID[1];
				returnIDs[0] = toContainerID;
			}
		}
		return returnIDs;
	}

	protected void sendCreateResponseSharedObjectMessage(ID toContainerID,
			ID fromSharedObject, Throwable t, long ident) throws IOException {
		sendMessage(ContainerMessage.makeSharedObjectCreateResponseMessage(
				getID(), toContainerID, getNextSequenceNumber(),
				fromSharedObject, t, ident));
	}

	protected void sendSharedObjectMessage(ID toContainerID,
			ID fromSharedObject, Serializable data) throws IOException {
		sendMessage(ContainerMessage.makeSharedObjectMessage(getID(),
				toContainerID, getNextSequenceNumber(), fromSharedObject, data));
	}

	protected void sendDisposeSharedObjectMessage(ID toContainerID,
			ID fromSharedObject) throws IOException {
		sendMessage(ContainerMessage.makeSharedObjectDisposeMessage(getID(),
				toContainerID, getNextSequenceNumber(), fromSharedObject));
	}

	public SOContainer(ISharedObjectContainerConfig config) {
		if (config == null)
			throw new InstantiationError("config must not be null");
		this.config = config;
		groupManager = new SOContainerGMM(this, new Member(config.getID()));
		sharedObjectManager = new SOManager(this);
		loadingThreadGroup = getLoadingThreadGroup();
		sharedObjectThreadGroup = getSharedObjectThreadGroup();
		listeners = new Vector();
		receiver = new MessageReceiver();
		debug("<init>");
	}

	protected ISynchAsynchConnectionEventHandler getReceiver() {
		return receiver;
	}

	protected boolean isClosing() {
		return isClosing;
	}

	protected void setIsClosing() {
		isClosing = true;
	}

	protected ThreadGroup getLoadingThreadGroup() {
		return new ThreadGroup(getID() + ":Loading");
	}

	protected ThreadGroup getSharedObjectThreadGroup() {
		return new ThreadGroup(getID() + ":SOs");
	}

	public ID getID() {
		return config.getID();
	}

	protected int getMaxGroupMembers() {
		return groupManager.getMaxMembers();
	}

	protected void setMaxGroupMembers(int max) {
		groupManager.setMaxMembers(max);
	}

	protected void notifySharedObjectActivated(ID sharedObjectID) {
		groupManager.notifyOthersActivated(sharedObjectID);
	}

	protected void notifySharedObjectDeactivated(ID sharedObjectID) {
		groupManager.notifyOthersDeactivated(sharedObjectID);
	}

	protected boolean destroySharedObject(ID sharedObjectID) {
		return groupManager.removeSharedObject(sharedObjectID);
	}

	protected IOSGIService getOSGIServiceInterface() {
		return null;
	}

	protected void sendCreate(ID sharedObjectID, ID toContainerID,
			SharedObjectDescription sd) throws IOException {
		sendCreateSharedObjectMessage(toContainerID, sd);
	}

	protected void sendDispose(ID toContainerID, ID sharedObjectID)
			throws IOException {
		sendDisposeSharedObjectMessage(toContainerID, sharedObjectID);
	}

	protected void sendMessage(ID toContainerID, ID sharedObjectID,
			Object message) throws IOException {
		if (message == null)
			return;
		if (message instanceof Serializable)
			throw new NotSerializableException(message.getClass().getName());
		sendSharedObjectMessage(toContainerID, sharedObjectID,
				(Serializable) message);
	}

	protected void sendCreateResponse(ID homeID, ID sharedObjectID,
			Throwable t, long identifier) throws IOException {
		sendCreateResponseSharedObjectMessage(homeID, sharedObjectID, t,
				identifier);
	}

	protected Thread getNewSharedObjectThread(ID sharedObjectID,
			Runnable runnable) {
		return new Thread(sharedObjectThreadGroup, runnable, getID().getName()
				+ ";" + sharedObjectID.getName());
	}

	protected ISharedObject load(SharedObjectDescription sd) throws Exception {
		return sharedObjectManager.loadSharedObject(sd);
	}

	protected ID[] getSharedObjectIDs() {
		return groupManager.getSharedObjectIDs();
	}

	protected SOConfig makeSharedObjectConfig(SharedObjectDescription sd,
			ISharedObject obj) {
		return new SOConfig(sd.getID(), sd.getHomeID(), this, sd
				.getProperties());
	}

	protected void moveFromLoadingToActive(SOWrapper wrap) {
		groupManager.moveSharedObjectFromLoadingToActive(wrap);
	}

	protected void removeFromLoading(ID id) {
		groupManager.removeSharedObjectFromLoading(id);
	}

	protected void processDisconnect(DisconnectConnectionEvent e) {
		debug("processDisconnect:" + e);
		try {
			ContainerMessage mess = getObjectFromBytes((byte[]) e.getData());
		} catch (IOException except) {

		}
	}
	protected Object checkCreate(ID fromID, ID toID, long seq, SharedObjectDescription desc) {
		// XXX TODO
		return desc;
	}
	protected boolean addToLoading(LoadingSharedObject lso) {
		return groupManager.addLoadingSharedObject(lso);
	}
	protected void handleCreateMessage(ContainerMessage mess) throws IOException {
		debug("handleCreateMessage:"+mess);
		
		SharedObjectDescription desc = (SharedObjectDescription) mess.getData();
		ID fromID = mess.getFromContainerID();
		ID toID = mess.getToContainerID();
		long seq = mess.getSequence();
		Object result = checkCreate(fromID,toID,seq,desc);
		if (result != null && (toID == null || toID.equals(getID()))) {
			LoadingSharedObject lso = new LoadingSharedObject(desc,result);
			synchronized (getGroupMembershipLock()) {
				if (!addToLoading(lso)) {
					ID sharedObjectID = desc.getID();
					try {
						sendCreateResponse(fromID,sharedObjectID,new SharedObjectAddException("shared object "+sharedObjectID),desc.getIdentifier());
					} catch (IOException e) {
						// XXX Log this
					}
				}
				forward(fromID, toID, mess);
				return;
			}
		}
		synchronized (getGroupMembershipLock()) {
			forward(fromID,toID,mess);
		}
	}
	protected void handleCreateResponseMessage(ContainerMessage mess) throws IOException {
		debug("handleCreateResponseMessage:"+mess);
		ID fromID = mess.getFromContainerID();
		ID toID = mess.getToContainerID();
		long seq = mess.getSequence();
		ContainerMessage.CreateResponseMessage resp = (ContainerMessage.CreateResponseMessage) mess.getData();
		if (toID != null && toID.equals(getID())) {
			SOWrapper sow = getSharedObjectWrapper(resp.getSharedObjectID());
			if (sow != null) {
				sow.deliverCreateResponse(fromID,resp);
			} else {
				// XXX log here
			}
		} else {
			forwardToRemote(fromID,toID,mess);
		}
	}
	protected void handleSharedObjectDisposeMessage(ContainerMessage mess) throws IOException {
		debug("handleSharedObjectDisposeMessage:"+mess);
		ID fromID = mess.getFromContainerID();
		ID toID = mess.getToContainerID();
		long seq = mess.getSequence();
		ContainerMessage.SharedObjectDisposeMessage resp = (ContainerMessage.SharedObjectDisposeMessage) mess.getData();
		ID sharedObjectID = resp.getSharedObjectID();
		synchronized (getGroupMembershipLock()) {
			if (groupManager.isLoading(sharedObjectID)) {
				groupManager.removeSharedObjectFromLoading(sharedObjectID);
			} else {
				groupManager.removeSharedObject(sharedObjectID);
			}
			forward(fromID, toID, mess);
		}
	}
	protected void handleSharedObjectMessage(ContainerMessage mess) throws IOException {
		debug("handleSharedObjectMessage:"+mess);
		ID fromID = mess.getFromContainerID();
		ID toID = mess.getToContainerID();
		long seq = mess.getSequence();
		ContainerMessage.SharedObjectMessage resp = (ContainerMessage.SharedObjectMessage) mess.getData();
		synchronized (getGroupMembershipLock()) {
			if (toID == null || toID.equals(getID())) {
					SOWrapper sow = getSharedObjectWrapper(resp.getFromSharedObjectID());
					if (sow != null) {
						sow.deliverSharedObjectMessage(fromID,resp.getData());
					}
			}
			forward(fromID,toID,mess);
		}
	}
	protected abstract void handleViewChangeMessage(ContainerMessage mess) throws IOException;

	protected void handleUnidentifiedMessage(ContainerMessage mess) throws IOException {
		// do nothing
	}
	protected void handleAsynchIOException(IOException except, AsynchConnectionEvent e) {
		// If we get IO Exception, we'll disconnect...if we can
		killConnection(e.getConnection());
	}
	protected void processAsynch(AsynchConnectionEvent e) {
		debug("processAsynch:" + e);
		try {
			ContainerMessage mess = getObjectFromBytes((byte[]) e.getData());
			Serializable submess = mess.getData();
			if (submess != null) {
				if (submess instanceof ContainerMessage.CreateMessage) {
					handleCreateMessage(mess);
				} else if (submess instanceof ContainerMessage.CreateResponseMessage) {
					handleCreateResponseMessage(mess);
				} else if (submess instanceof ContainerMessage.SharedObjectDisposeMessage) {
					handleSharedObjectDisposeMessage(mess);
				} else if (submess instanceof ContainerMessage.SharedObjectMessage) {
					handleSharedObjectMessage(mess);
				} else if (submess instanceof ContainerMessage.ViewChangeMessage) {
					handleViewChangeMessage(mess);
				} else {
					handleUnidentifiedMessage(mess);
				}
			} else {
				handleUnidentifiedMessage(mess);
			}
		} catch (IOException except) {
			handleAsynchIOException(except,e);
		}
	}

	protected Serializable processSynch(SynchConnectionEvent e)
			throws IOException {
		debug("processSynch:" + e);
		ContainerMessage mess = getObjectFromBytes((byte[]) e.getData());
		ID fromID = mess.getFromContainerID();
		notifyGroupLeave(mess);
		synchronized (getGroupMembershipLock()) {
			memberLeave(fromID, e.getConnection());
		}
		return null;
	}

	protected void notifyGroupLeave(ContainerMessage mess) {
		// XXX todo
	}

	class LoadingSharedObject implements ISharedObject {

		SharedObjectDescription description;
		Object credentials;
		
		Thread runner = null;

		LoadingSharedObject(SharedObjectDescription sd, Object credentials) {
			this.description = sd;
			this.credentials = credentials;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
		 */
		public void init(ISharedObjectConfig initData)
				throws SharedObjectInitException {
		}

		ID getID() {
			return description.getID();
		}

		ID getHomeID() {
			return description.getHomeID();
		}

		void start() {
			if (runner == null) {
				runner = (Thread) AccessController
						.doPrivileged(new PrivilegedAction() {
							public Object run() {
								return getThread();
							}
						});
				runner.setDaemon(true);
				runner.start();
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
		 */
		public void handleEvent(Event event) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.core.ISharedObject#handleEvents(org.eclipse.ecf.core.util.Event[])
		 */
		public void handleEvents(Event[] events) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.core.ISharedObject#dispose(org.eclipse.ecf.core.identity.ID)
		 */
		public void dispose(ID containerID) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.core.ISharedObject#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class clazz) {
			return null;
		}

		Thread getThread() {
			return new Thread(loadingThreadGroup, new Runnable() {
				public void run() {
					try {
						if (Thread.currentThread().isInterrupted()
								|| isClosing())
							throw new InterruptedException(
									"Loading interrupted for object "
											+ getID().getName());
						// First load given object
						ISharedObject obj = load(description);
						// Get config info for new object
						SOConfig aConfig = makeSharedObjectConfig(description,
								obj);
						// Call init method on new object.
						obj.init(aConfig);
						// Check to make sure thread has not been
						// interrupted...if it has, throw
						if (Thread.currentThread().isInterrupted()
								|| isClosing())
							throw new InterruptedException(
									"Loading interrupted for object "
											+ getID().getName());

						// Create meta object and move from loading to active
						// list.
						SOContainer.this.moveFromLoadingToActive(new SOWrapper(
								aConfig, obj, SOContainer.this));
					} catch (Exception e) {
						SOContainer.this.removeFromLoading(getID());
						try {
							sendCreateResponse(getHomeID(), getID(), e,
									description.getIdentifier());
						} catch (Exception e1) {
						}
					}
				}
			}, "LRunner" + getID().getName());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#getConfig()
	 */
	public ISharedObjectContainerConfig getConfig() {
		return config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#addListener(org.eclipse.ecf.core.ISharedObjectContainerListener,
	 *      java.lang.String)
	 */
	public void addListener(ISharedObjectContainerListener l, String filter) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#removeListener(org.eclipse.ecf.core.ISharedObjectContainerListener)
	 */
	public void removeListener(ISharedObjectContainerListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#dispose(long)
	 */
	public void dispose(long waittime) {
		debug("dispose:" + waittime);
		isClosing = true;
		// XXX Notify listeners that we're going away
		// Clear group manager
		if (groupManager != null) {
			groupManager.removeAllMembers();
			groupManager = null;
		}
		// Clear shared object manager
		if (sharedObjectManager != null) {
			sharedObjectManager.dispose();
			sharedObjectManager = null;
		}
		if (sharedObjectThreadGroup != null) {
			sharedObjectThreadGroup.interrupt();
			sharedObjectThreadGroup = null;
		}
		if (loadingThreadGroup != null) {
			loadingThreadGroup.interrupt();
			loadingThreadGroup = null;
		}
		if (listeners != null) {
			listeners.clear();
			listeners = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#joinGroup(org.eclipse.ecf.core.identity.ID,
	 *      java.lang.Object)
	 */
	public abstract void joinGroup(ID groupID, Object loginData)
			throws SharedObjectContainerJoinException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#leaveGroup()
	 */
	public abstract void leaveGroup();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#getGroupID()
	 */
	public abstract ID getGroupID();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#getGroupMemberIDs()
	 */
	public ID[] getGroupMemberIDs() {
		return groupManager.getMemberIDs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#isGroupManager()
	 */
	public abstract boolean isGroupManager();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#isGroupServer()
	 */
	public abstract boolean isGroupServer();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#getSharedObjectManager()
	 */
	public ISharedObjectManager getSharedObjectManager() {
		return sharedObjectManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.ISharedObjectContainer#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	protected ClassLoader getClassLoaderForContainer() {
		return this.getClass().getClassLoader();
	}

	/**
	 * @param sd
	 * @return
	 */
	protected ClassLoader getClassLoaderForSharedObject(
			SharedObjectDescription sd) {
		if (sd != null) {
			ClassLoader cl = sd.getClassLoader();
			if (cl != null)
				return cl;
			else
				return getClassLoaderForContainer();
		} else
			return getClassLoaderForContainer();
	}

	/**
	 * @param sd
	 * @return
	 */
	public Object[] getArgsFromProperties(SharedObjectDescription sd) {
		if (sd == null)
			return null;
		Map aMap = sd.getProperties();
		if (aMap == null)
			return null;
		Object obj = aMap.get(DEFAULT_OBJECT_ARG_KEY);
		if (obj == null)
			return null;
		if (obj instanceof Object[]) {
			Object[] ret = (Object[]) obj;
			aMap.remove(DEFAULT_OBJECT_ARG_KEY);
			return ret;
		} else
			return null;
	}

	/**
	 * @param sd
	 * @return
	 */
	public String[] getArgTypesFromProperties(SharedObjectDescription sd) {
		if (sd == null)
			return null;
		Map aMap = sd.getProperties();
		if (aMap == null)
			return null;
		Object obj = aMap.get(DEFAULT_OBJECT_ARGTYPES_KEY);
		if (obj == null)
			return null;
		if (obj instanceof String[]) {
			String[] ret = (String[]) obj;
			aMap.remove(DEFAULT_OBJECT_ARGTYPES_KEY);
			return ret;
		} else
			return null;
	}

	class MessageReceiver implements ISynchAsynchConnectionEventHandler {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.internal.comm.ISynchConnectionEventHandler#handleSynchEvent(org.eclipse.ecf.internal.comm.SynchConnectionEvent)
		 */
		public Object handleSynchEvent(SynchConnectionEvent event)
				throws IOException {
			return processSynch(event);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.internal.comm.IConnectionEventHandler#handleSuspectEvent(org.eclipse.ecf.internal.comm.ConnectionEvent)
		 */
		public boolean handleSuspectEvent(ConnectionEvent event) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.internal.comm.IConnectionEventHandler#handleDisconnectEvent(org.eclipse.ecf.internal.comm.ConnectionEvent)
		 */
		public void handleDisconnectEvent(DisconnectConnectionEvent event) {
			processDisconnect(event);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.internal.comm.IConnectionEventHandler#getAdapter(java.lang.Class)
		 */
		public Object getAdapter(Class clazz) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.internal.comm.IAsynchConnectionEventHandler#handleAsynchEvent(org.eclipse.ecf.internal.comm.AsynchConnectionEvent)
		 */
		public void handleAsynchEvent(AsynchConnectionEvent event)
				throws IOException {
			processAsynch(event);
		}

	}
}