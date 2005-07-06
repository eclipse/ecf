/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.provider.generic.sobject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.ecf.core.IIdentifiable;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.ISharedObjectContainerTransaction;
import org.eclipse.ecf.core.ISharedObjectContext;
import org.eclipse.ecf.core.ISharedObjectManager;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.provider.Trace;

/**
 * @author slewis
 *
 */
public class BaseSharedObject implements ISharedObject, IIdentifiable {

	protected static final String TRANSACTIONAL_SUFFIX = ".transactional";
	private static long identifier = 0L;
	Trace trace = Trace.create("basesharedobject");
	
	ISharedObjectConfig config = null;
	List eventProcessors = new Vector();
	Boolean transactional = null;
	
	protected static long getIdentifier() {
		return identifier++;
	}
	private void trace(String msg) {
		if (Trace.ON && trace != null) {
			trace.msg(getID()+":"+msg);
		}
	}
	private void traceStack(String msg, Throwable t) {
		if (Trace.ON && trace != null) {
			trace.dumpStack(t,getID()+":"+msg);
		}
	}
	protected void addEventProcessor(IEventProcessor proc) {
		eventProcessors.add(proc);
	}
	protected boolean removeEventProcessor(IEventProcessor proc) {
		return eventProcessors.remove(proc);
	}
	protected void fireEventProcessors(Event event) {
		if (event == null) return;
		Event evt = event;
		trace("fireEventProcessors("+event+")");
		if (eventProcessors.size()==0) {
			handleUnhandledEvent(event);
			return;
		}
		for(Iterator i=eventProcessors.iterator(); i.hasNext(); ) {
			IEventProcessor ep = (IEventProcessor) i.next();
			if (ep != null) {
				if (evt != null) {
					if (ep.acceptEvent(evt)) {
						trace("eventProcessor="+ep+":event="+evt);
						evt = ep.processEvent(evt);
					}
				}
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObject#init(org.eclipse.ecf.core.ISharedObjectConfig)
	 */
	public void init(ISharedObjectConfig initData)
			throws SharedObjectInitException {
		this.config = initData;
		trace("init("+initData+")");
		Map props = config.getProperties();
		Object o = props.get(this.getClass().getName()+TRANSACTIONAL_SUFFIX);
		if (o instanceof Boolean) {
			Boolean b = (Boolean) o;
			if (b != null && b.booleanValue()) {
				// transactional...
				new TwoPhaseCommit(this);
			}
		}
		
	}
	protected ISharedObjectConfig getConfig() {
		return config;
	}
	protected ISharedObjectContext getContext() {
		ISharedObjectConfig c = getConfig();
		if (c == null) {
			return null;
		} else return config.getContext();
	}
	protected ID getHomeID() {
		ISharedObjectConfig conf = getConfig();
		if (conf == null) return null;
		else return conf.getHomeContainerID();
	}
	protected ID getLocalID() {
		ISharedObjectContext context = getContext();
		if (context == null) {
			return null;
		} else return context.getLocalContainerID();
	}
	protected ID getGroupID() {
		ISharedObjectContext context = getContext();
		if (context == null) {
			return null;
		} else return context.getGroupID();
	}
	protected boolean isPrimary() {
		ID local = getLocalID();
		ID home = getHomeID();
		if (local == null || home == null) {
			return false;
		} else return (local.equals(home));
	}
	protected Map getProperties() {
		ISharedObjectConfig config = getConfig();
		if (config == null) {
			return null;
		} else return config.getProperties();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvent(org.eclipse.ecf.core.util.Event)
	 */
	public void handleEvent(Event event) {
		fireEventProcessors(event);
	}
	protected void handleUnhandledEvent(Event event) {
		trace("handleUnhandledEvent("+event+")");
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObject#handleEvents(org.eclipse.ecf.core.util.Event[])
	 */
	public void handleEvents(Event[] events) {
		if (events == null) return;
		for(int i=0; i < events.length; i++) {
			handleEvent(events[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObject#dispose(org.eclipse.ecf.core.identity.ID)
	 */
	public void dispose(ID containerID) {
		config = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class clazz) {
		if (clazz.equals(ISharedObjectContainerTransaction.class)) {
			transactional = new Boolean(true);
			return new TwoPhaseCommit(this);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IIdentifiable#getID()
	 */
	public ID getID() {
		ISharedObjectConfig conf = getConfig();
		if (conf == null) {
			return null;
		} else return conf.getSharedObjectID();
	}
    public void destroySelf() {
        if (isPrimary()) {
            try {
                // Send destroy message to all known remotes
                destroyRemote(null);
            } catch (IOException e) {
                traceStack("Exception sending destroy message to remotes", e);
            }
        }
        destroySelfLocal();
    }

    public void destroySelfLocal() {
        try {
            ISharedObjectConfig soconfig = getConfig();
            if (soconfig != null) {
                ID myID = soconfig.getSharedObjectID();
                ISharedObjectContext context = getContext();
                if (context != null) {
                    ISharedObjectManager manager = context.getSharedObjectManager();
                    if (manager != null) {
                        manager.removeSharedObject(myID);
                    }
                }
            }
        } catch (Exception e) {
            traceStack("Exception in destroySelfLocal()",e);
        }
    }
    public void destroyRemote(ID remoteID) throws IOException {
        ISharedObjectContext context = getContext();
        if (context != null) {
            context.sendDispose(remoteID);
        }
    }

    protected void replicateToRemote(ID remote) {
        trace("replicateToRemote(" + remote + ")");
        try {
            // Get current group membership
            ISharedObjectContext context = getContext();
            if (context == null) return;
            ID[] group = context.getGroupMemberIDs();
            if (group == null || group.length < 1) {
                // we're done
                return;
            }
            SharedObjectDescription createInfo = getReplicaDescription(remote);
            if (createInfo != null) {
                context.sendCreate(remote, createInfo);
            } else {
                return;
            }
        } catch (IOException e) {
            traceStack("Exception in replicate("+remote+")", e);
            return;
        }
    }
    protected SharedObjectDescription getReplicaDescription(ID receiver) {
    	Map props = getConfig().getProperties();
    	props.put(this.getClass().getName()+TRANSACTIONAL_SUFFIX,transactional);
        return new SharedObjectDescription(getID(), getClass().getName(),
            		props, getIdentifier());
    }



}
