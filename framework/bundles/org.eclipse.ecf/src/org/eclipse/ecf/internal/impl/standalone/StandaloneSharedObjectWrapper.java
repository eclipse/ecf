package org.eclipse.ecf.internal.impl.standalone;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.SharedObjectActivatedEvent;
import org.eclipse.ecf.core.events.SharedObjectContainerDepartedEvent;
import org.eclipse.ecf.core.events.SharedObjectContainerJoinEvent;
import org.eclipse.ecf.core.events.SharedObjectDeactivatedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.internal.impl.standalone.gmm.Item;
import org.eclipse.ecf.internal.util.queue.SimpleQueueImpl;

final class StandaloneSharedObjectWrapper {
	static Debug debug = Debug.create(StandaloneSharedObjectWrapper.class.getName());

	protected ISharedObject sharedObject;
	private StandaloneSharedObjectConfig sharedObjectConfig;
	private ID sharedObjectID;
	private ID sharedObjectHomeID;
	private StandaloneContainer container;
	private ID containerID;
	private Thread thread;
	private SimpleQueueImpl queue;

	StandaloneSharedObjectWrapper(
	        StandaloneSharedObjectConfig aConfig,
		ISharedObject obj,
		StandaloneContainer space) {
		sharedObjectConfig = aConfig;
		sharedObjectID = sharedObjectConfig.getSharedObjectID();
		sharedObjectHomeID = sharedObjectConfig.getHomeContainerID();
		sharedObject = obj;
		container = space;
		containerID = space.getID();
		thread = null;
		queue = new SimpleQueueImpl();
	}
	
	void init() throws SharedObjectInitException {
	    sharedObject.init(sharedObjectConfig);
	}
	ID getObjID() {
		return sharedObjectID;
	}

	ID getHomeID() {
		return sharedObjectHomeID;
	}

	void activated(ID[] ids) {
		// First, make space reference accessible to use by RepObject
		sharedObjectConfig.makeActive(new QueueEnqueueImpl(queue));
		thread =
				(Thread) AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
						// Get thread instance
				    	Thread aThread = getThread();
					return aThread;
				}
			});
			thread.start();
			send(new SharedObjectActivatedEvent(containerID, sharedObjectID, ids));
		container.notifySharedObjectActivated(sharedObjectID);
		
	}
	void deactivated() {
		send(new SharedObjectDeactivatedEvent(containerID,sharedObjectID));
		container.notifySharedObjectDeactivated(sharedObjectID);
		destroyed();
		
	}
	private void destroyed() {
		if (!queue.isStopped()) {
		    
		    sharedObjectConfig.makeInactive();
			// Enqueue destroy message on our RepObject's queue
			if (thread != null)
				queue.enqueue(new DisposeEvent());
			// Close queue...RepObject will receive no more messages from this point on.
			queue.close();
		}
		
	}
	void otherChanged(ID otherID, ID[] all, boolean activated) {
			if (activated && thread != null) {
				send(new SharedObjectActivatedEvent(containerID,otherID,all));
			} else {
				send(new SharedObjectDeactivatedEvent(containerID,otherID));
			}
	}
	void memberChanged(Item m, boolean add) {
		if (thread != null) {
			if (add) {
				send(new SharedObjectContainerJoinEvent(containerID,m.getID()));
			} else {
				send(new SharedObjectContainerDepartedEvent(containerID,m.getID()));
			}
		}
	}
	Thread getThread() {
		// Get new thread instance from space.
	    
		return container
			.getSharedObjectThread(sharedObjectID, new Runnable() {
			public void run() {
				if (Debug.ON && debug != null) {
					debug.msg("Starting runner for " + sharedObjectID);
				}
				// The debug class will associate this thread with container
				Debug.setThreadDebugGroup(container.getID());
				// Then process messages on queue until interrupted or queue closed
				//Msg aMsg = null;
				Event evt = null;
				for (;;) {
					// make sure the thread hasn't been interrupted and get Msg from SimpleQueueImpl
					if (Thread.currentThread().isInterrupted())
						break;
					
					evt = (Event) queue.dequeue();
					if (Thread.currentThread().isInterrupted() || evt == null)
						break;
						
					try {
					    if (evt instanceof ProcEvent) {
							StandaloneSharedObjectWrapper.this.svc(((ProcEvent)evt).getEvent());
					    } else if (evt instanceof DisposeEvent) {
					        StandaloneSharedObjectWrapper.this.doDestroy();
					    }
					} catch (Throwable t) {
						if (Debug.ON && debug != null) {
							debug.dumpStack(
								t,
								"Exception executing event "
									+ evt
									+ " on meta "
									+ this);
						}
						handleRuntimeException(t);
					}
				}
				// If the thread was interrupted, then show appropriate spam
				if (Thread.currentThread().isInterrupted()) {
					if (Debug.ON && debug != null) {
						debug.msg(
							"Runner for "
								+ sharedObjectID
								+ " terminating after being interrupted");
					}
				} else {
					if (Debug.ON && debug != null) {
						debug.msg(
							"Runner for " + sharedObjectID + " terminating normally");
					}
				}
			}
		}, "SOID:" + sharedObjectID);
	}
	private void send(Event evt) {
		queue.enqueue(new ProcEvent(evt));
	}

	protected static class ProcEvent implements Event {
	    Event theEvent = null;
	    ProcEvent(Event event) {
	        theEvent = event;
	    }
	    Event getEvent() {
	        return theEvent;
	    }
	}
	protected static class DisposeEvent implements Event {
	    DisposeEvent() {
	    }
	}
	void svc(Event evt) {
	    sharedObject.handleEvent(evt);
	}
	void doDestroy() {
	    sharedObject.dispose(containerID);
	}

	void destroySelf() {
	    /*
		if (thread != null) {
			send(Msg.makeMsg(null, REPOBJ_DESTROY_SELF));
		}
		*/
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SharedObjectWrapper[").append(getObjID()).append("]");
		return sb.toString();
	}
	void handleRuntimeException(Throwable except) {
		if (Debug.ON && debug != null) {
			debug.dumpStack(
				except,
				"handleRuntimeException called for " + sharedObjectID);
		}
		try {
			Debug.errDumpStack(
				except,
				"handleRuntimeException called for " + sharedObjectID);
		} catch (Throwable e) {
		}
	}

}
