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

package org.eclipse.ecf.provider.datashare;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.StringID;
import org.eclipse.ecf.core.sharedobject.BaseSharedObject;
import org.eclipse.ecf.core.sharedobject.ISharedObject;
import org.eclipse.ecf.core.sharedobject.ISharedObjectTransactionConfig;
import org.eclipse.ecf.core.sharedobject.SharedObjectCreateException;
import org.eclipse.ecf.core.sharedobject.SharedObjectDescription;
import org.eclipse.ecf.core.sharedobject.SharedObjectFactory;
import org.eclipse.ecf.core.sharedobject.SharedObjectInitException;
import org.eclipse.ecf.core.sharedobject.SharedObjectTypeDescription;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectDeactivatedEvent;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.ecf.datashare.IChannelConfig;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.datashare.IChannelContainerListener;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.datashare.events.IChannelContainerChannelActivatedEvent;
import org.eclipse.ecf.datashare.events.IChannelContainerChannelDeactivatedEvent;
import org.eclipse.ecf.datashare.events.IChannelContainerEvent;

public class SharedObjectDatashareContainerAdapter extends BaseSharedObject implements IChannelContainerAdapter {
	
	protected static final int DEFAULT_TRANSACTION_WAIT = 30000;
	
	protected List channelContainerListeners = Collections.synchronizedList(new ArrayList());
	
	protected void initialize() throws SharedObjectInitException {
		super.initialize();
		addEventProcessor(new IEventProcessor() {
			public boolean processEvent(Event event) {
				if (event instanceof ISharedObjectActivatedEvent) {
					final ISharedObjectActivatedEvent soae = (ISharedObjectActivatedEvent) event;
					if (!soae.getActivatedID().equals(getID())) 
						fireChannelContainerListeners(new IChannelContainerChannelActivatedEvent() {
							public ID getChannelID() {
								return soae.getActivatedID();
							}
							public ID getChannelContainerID() {
								return soae.getLocalContainerID();
							}
							public String toString() {
								StringBuffer buf = new StringBuffer("ChannelActivatedEvent[");
								buf.append("channelid=").append(soae.getActivatedID()).append(";");
								buf.append("containerid=").append(soae.getLocalContainerID()).append("]");
								return buf.toString();
							}});
				} else if (event instanceof ISharedObjectDeactivatedEvent) {
					final ISharedObjectDeactivatedEvent sode = (ISharedObjectDeactivatedEvent) event;
					if (!sode.getDeactivatedID().equals(getID())) 
						fireChannelContainerListeners(new IChannelContainerChannelDeactivatedEvent() {
							public ID getChannelID() {
								return sode.getDeactivatedID();
							}
							public ID getChannelContainerID() {
								return sode.getLocalContainerID();
							}
							public String toString() {
								StringBuffer buf = new StringBuffer("ChannelDeactivatedEvent[");
								buf.append("channelid=").append(sode.getDeactivatedID()).append(";");
								buf.append("containerid=").append(sode.getLocalContainerID()).append("]");
								return buf.toString();
							}});
				}
				return false;
			}});
	}

	protected void fireChannelContainerListeners(IChannelContainerEvent event) {
		synchronized (channelContainerListeners) {
			for(Iterator i=channelContainerListeners.iterator(); i.hasNext(); ) {
				IChannelContainerListener l = (IChannelContainerListener) i.next();
				if (l != null) l.handleChannelContainerEvent(event);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IChannelContainerAdapter#createChannel(org.eclipse.ecf.datashare.IChannelConfig)
	 */
	public IChannel createChannel(final ID newID,
			final IChannelListener listener, final Map properties)
			throws ECFException {
		return createChannel(new IChannelConfig() {
			public IChannelListener getListener() {
				return listener;
			}
			public ID getID() {
				return newID;
			}
			public Object getAdapter(Class adapter) {
				return null;
			}
			public Map getProperties() {
				return properties;
			}
		});
	}
	protected SharedObjectDescription createChannelSharedObjectDescription(final IChannelConfig channelConfig) {
		return new SharedObjectDescription(BaseChannel.class, channelConfig.getID(), channelConfig.getProperties());
	}
	protected ISharedObjectTransactionConfig createChannelSharedObjectTransactionConfig() {
		return null;
	}
	protected ISharedObject createSharedObject(
			SharedObjectTypeDescription typeDescription,
			ISharedObjectTransactionConfig transactionConfig,
			IChannelListener listener) throws SharedObjectCreateException {
		Class clazz;
		try {
			clazz = Class.forName(typeDescription.getClassName());
		} catch (ClassNotFoundException e) {
			throw new SharedObjectCreateException(
					"No constructor for shared object of class "
							+ typeDescription.getClassName(), e);
		}
		Constructor cons = null;
		try {
			cons = clazz.getDeclaredConstructor(new Class[] {
					ISharedObjectTransactionConfig.class,
					IChannelListener.class });
		} catch (NoSuchMethodException e) {
			throw new SharedObjectCreateException(
					"No constructor for shared object of class "
							+ typeDescription.getClassName(), e);
		}
		ISharedObject so = null;
		try {
			so = (ISharedObject) cons.newInstance(new Object[] {
					transactionConfig, listener });
		} catch (Exception e) {
			throw new SharedObjectCreateException(
					"Cannot create instance of class "
							+ typeDescription.getClassName(), e);
		}
		return so;
	}
	public IChannel createChannel(IChannelConfig newChannelConfig)
			throws ECFException {
		SharedObjectDescription sodesc = createChannelSharedObjectDescription(newChannelConfig);
		SharedObjectTypeDescription sotypedesc = sodesc.getTypeDescription();
		IChannelListener listener = newChannelConfig.getListener();
		ISharedObjectTransactionConfig transactionConfig = createChannelSharedObjectTransactionConfig();
		ISharedObject so = null;
		if (sotypedesc.getName() != null) {
			so = SharedObjectFactory
					.getDefault()
					.createSharedObject(
							sotypedesc,
							new Object[] { transactionConfig, listener });
		} else {
			so = createSharedObject(sotypedesc, transactionConfig, listener);
		}
		IChannel channel = (IChannel) so.getAdapter(IChannel.class);
		if (channel == null)
			throw new SharedObjectCreateException("Cannot adapt object "
					+ channel + " to be of type IChannel");
		ID newID = sodesc.getID();
		if (newID == null)
			newID = IDFactory.getDefault().createGUID();
		// Now add channel to container...this will block
		getContext().getSharedObjectManager().addSharedObject(newID, so, sodesc.getProperties());
		return channel;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IChannelContainerAdapter#getChannel(org.eclipse.ecf.core.identity.ID)
	 */
	public IChannel getChannel(ID channelID) {
		if (channelID == null || channelID.equals(getID())) return null;
		else return (IChannel) getContext().getSharedObjectManager().getSharedObject(channelID);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IChannelContainerAdapter#disposeChannel(org.eclipse.ecf.core.identity.ID)
	 */
	public boolean removeChannel(ID channelID) {
		if (channelID == null || channelID.equals(getID())) return false;
		ISharedObject o = getContext().getSharedObjectManager()
				.removeSharedObject(channelID);
		return (o != null);
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ecf.datashare.IChannelContainerAdapter#getChannelNamespace()
	 */
	public Namespace getChannelNamespace() {
		return IDFactory.getDefault().getNamespaceByName(StringID.class.getName());
	}
	public void addListener(IChannelContainerListener listener) {
		channelContainerListeners.add(listener);
	}
	public void removeListener(IChannelContainerListener listener) {
		channelContainerListeners.add(listener);
	}

}
