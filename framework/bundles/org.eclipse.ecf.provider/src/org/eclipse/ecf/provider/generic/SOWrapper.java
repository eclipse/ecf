package org.eclipse.ecf.provider.generic;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.SharedObjectActivatedEvent;
import org.eclipse.ecf.core.events.SharedObjectContainerDepartedEvent;
import org.eclipse.ecf.core.events.SharedObjectContainerJoinedEvent;
import org.eclipse.ecf.core.events.SharedObjectDeactivatedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.AsynchResult;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.SimpleQueueImpl;
import org.eclipse.ecf.provider.Trace;
import org.eclipse.ecf.provider.generic.gmm.Member;

final class SOWrapper {
    static Trace debug = Trace.create(SOWrapper.class.getName());

    protected ISharedObject sharedObject;
    private SOConfig sharedObjectConfig;
    private ID sharedObjectID;
    private ID sharedObjectHomeID;
    private SOContainer container;
    private ID containerID;
    private Thread thread;
    private SimpleQueueImpl queue;

    SOWrapper(SOContainer.LoadingSharedObject obj, SOContainer cont) {
        sharedObjectID = obj.getID();
        sharedObjectHomeID = obj.getHomeID();
        sharedObject = obj;
        container = cont;
        containerID = cont.getID();
        sharedObjectConfig = null;
        thread = null;
        queue = new SimpleQueueImpl();
    }
    SOWrapper(SOConfig aConfig, ISharedObject obj, SOContainer cont) {
        sharedObjectConfig = aConfig;
        sharedObjectID = sharedObjectConfig.getSharedObjectID();
        sharedObjectHomeID = sharedObjectConfig.getHomeContainerID();
        sharedObject = obj;
        container = cont;
        containerID = cont.getID();
        thread = null;
        queue = new SimpleQueueImpl();
    }

    void init() throws SharedObjectInitException {
        sharedObject.init(sharedObjectConfig);
    }
    ID getObjID() {
        return sharedObjectConfig.getSharedObjectID();
    }

    ID getHomeID() {
        return sharedObjectConfig.getHomeContainerID();
    }

    void activated(ID[] ids) {
        // First, make space reference accessible to use by RepObject
        sharedObjectConfig.makeActive(new QueueEnqueueImpl(queue));
        thread = (Thread) AccessController.doPrivileged(new PrivilegedAction() {
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
        send(new SharedObjectDeactivatedEvent(containerID, sharedObjectID));
        container.notifySharedObjectDeactivated(sharedObjectID);
        destroyed();
    }
    private void destroyed() {
        if (!queue.isStopped()) {
            sharedObjectConfig.makeInactive();
            // Enqueue destroy message on our RepObject's queue
            if (thread != null)
                queue.enqueue(new DisposeEvent());
            // Close queue...RepObject will receive no more messages from this
            // point on.
            queue.close();
        }

    }
    void otherChanged(ID otherID, boolean activated) {
        if (activated && thread != null) {
            send(new SharedObjectActivatedEvent(containerID, otherID, null));
        } else {
            send(new SharedObjectDeactivatedEvent(containerID, otherID));
        }
    }
    void memberChanged(Member m, boolean add) {
        if (thread != null) {
            if (add) {
                send(new SharedObjectContainerJoinedEvent(containerID, m
                        .getID()));
            } else {
                send(new SharedObjectContainerDepartedEvent(containerID, m
                        .getID()));
            }
        }
    }
    Thread getThread() {
        // Get new thread instance from space.
        return container.getNewSharedObjectThread(sharedObjectID,
                new Runnable() {
                    public void run() {
                        if (Trace.ON && debug != null) {
                            debug.msg("Starting runner for " + sharedObjectID);
                        }
                        // The debug class will associate this thread with
                        // container
                        Trace.setThreadDebugGroup(container.getID());
                        // Then process messages on queue until interrupted or
                        // queue closed
                        //Msg aMsg = null;
                        Event evt = null;
                        for (;;) {
                            // make sure the thread hasn't been interrupted and
                            // get Msg from SimpleQueueImpl
                            if (Thread.currentThread().isInterrupted())
                                break;

                            evt = (Event) queue.dequeue();
                            if (Thread.currentThread().isInterrupted()
                                    || evt == null)
                                break;

                            try {
                                if (evt instanceof ProcEvent) {
                                    SOWrapper.this.svc(((ProcEvent) evt)
                                            .getEvent());
                                } else if (evt instanceof DisposeEvent) {
                                    SOWrapper.this.doDestroy();
                                }
                            } catch (Throwable t) {
                                if (Trace.ON && debug != null) {
                                    debug.dumpStack(t,
                                            "Exception executing event " + evt
                                                    + " on meta " + this);
                                }
                                handleRuntimeException(t);
                            }
                        }
                        // If the thread was interrupted, then show appropriate
                        // spam
                        if (Thread.currentThread().isInterrupted()) {
                            if (Trace.ON && debug != null) {
                                debug
                                        .msg("Runner for "
                                                + sharedObjectID
                                                + " terminating after being interrupted");
                            }
                        } else {
                            if (Trace.ON && debug != null) {
                                debug.msg("Runner for " + sharedObjectID
                                        + " terminating normally");
                            }
                        }
                    }
                });
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

    //void createMsgResp(ID fromID, ContainerMessage.CreateResponse resp) {
    /*
     * if (sharedObjectConfig.getMsgMask().get(MsgMask.CREATERESPONSE) && thread !=
     * null) { send( Msg.makeMsg( null, CREATE_RESP_RCVD, fromID, resp.myExcept,
     * new Long(resp.mySeq))); }
     */
    //}
    void deliverObjectFromRemote(ID fromID, Serializable data) {
        // If we have a container, forward message onto container
        /*
         * if (myContainerID != null) { forwardToContainer( Msg.makeMsg(null,
         * REMOTE_REPOBJ_MSG, fromID, data)); // otherwise, send to our object
         * (assuming it has thread and that it wants to receive message) } else
         * if ( sharedObjectConfig.getMsgMask().get(MsgMask.REMOTEDATA) &&
         * thread != null) { send(Msg.makeMsg(null, REMOTE_REPOBJ_MSG, fromID,
         * data)); }
         */
    }

    void forwardToContainer(Event msg) {
        /*
         * try { container.deliverForwardToRepObject(sharedObjectID,
         * myContainerID, msg); } catch (Exception e) {
         * handleRuntimeException(e); }
         */
    }
    void deliverEventFromSharedObject(ID fromID, Event evt) {
        /*
         * if (myContainerID != null) { forwardToContainer(Msg.makeMsg(null,
         * REPOBJ_MSG, fromID, msg)); // otherwise, send to our object (assuming
         * it has thread and that it wants to receive message) } else if (
         * sharedObjectConfig.getMsgMask().get(MsgMask.REPOBJMSG) && thread !=
         * null) { send(Msg.makeMsg(null, REPOBJ_MSG, fromID, msg)); }
         */
    }
    void deliverRequestFromRepObject(ID fromID, Event evt, AsynchResult future) {
        /*
         * if (myContainerID != null) { forwardToContainer( Msg.makeMsg(null,
         * REPOBJ_REQ, fromID, msg, future)); } else if (
         * sharedObjectConfig.getMsgMask().get(MsgMask.REPOBJMSG) && thread !=
         * null) { // Check to see that messages may be received...determined by
         * the REPOBJMSG // bit in msg mask send(Msg.makeMsg(null, REPOBJ_REQ,
         * fromID, msg, future)); }
         */
    }
    void deliverForwardedMsg(ID fromID, Event evt) {
        /*
         * if (myContainerID != null) { forwardToContainer(Msg.makeMsg(null,
         * REPOBJ_FOR, fromID, msg)); } else if (
         * sharedObjectConfig.getMsgMask().get(MsgMask.REPOBJMSG) && thread !=
         * null) { send(Msg.makeMsg(null, REPOBJ_FOR, fromID, msg)); }
         */
    }
    void deliverRemoteMessageFailed(ID toID, Serializable object, Throwable e) {
        /*
         * if (sharedObjectConfig.getMsgMask().get(MsgMask.REPOBJMSG) && thread !=
         * null) { send(Msg.makeMsg(null, REMOTE_REPOBJ_MSG_FAILED, toID,
         * object, e)); }
         */
    }

    void destroySelf() {
        /*
         * if (thread != null) { send(Msg.makeMsg(null, REPOBJ_DESTROY_SELF)); }
         */
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SharedObjectWrapper[").append(getObjID()).append("]");
        return sb.toString();
    }
    void handleRuntimeException(Throwable except) {
        if (Trace.ON && debug != null) {
            debug.dumpStack(except, "handleRuntimeException called for "
                    + sharedObjectID);
        }
        try {
            Trace.errDumpStack(except, "handleRuntimeException called for "
                    + sharedObjectID);
        } catch (Throwable e) {
        }
    }
    /**
     * @return
     */
    protected ISharedObject getSharedObject() {
        return sharedObject;
    }
    /**
     * @return
     */
    public SimpleQueueImpl getQueue() {
        return queue;
    }

}