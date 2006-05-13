package org.eclipse.ecf.provider.datashare;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.ISharedObjectTransactionConfig;
import org.eclipse.ecf.core.ISharedObjectTransactionParticipantsFilter;
import org.eclipse.ecf.core.SharedObjectCreateException;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectFactory;
import org.eclipse.ecf.core.SharedObjectTypeDescription;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.events.ISharedObjectActivatedEvent;
import org.eclipse.ecf.core.events.ISharedObjectDeactivatedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDInstantiationException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.StringID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.ecf.datashare.IChannelConfig;
import org.eclipse.ecf.datashare.IChannelContainer;
import org.eclipse.ecf.datashare.IChannelContainerListener;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.datashare.events.IChannelContainerChannelActivatedEvent;
import org.eclipse.ecf.datashare.events.IChannelContainerEvent;
import org.eclipse.ecf.datashare.events.IChannelContainerChannelDeactivatedEvent;
import org.eclipse.ecf.provider.generic.SOContainer;

public class DatashareContainerAdapter implements IChannelContainer {
	
	protected SOContainer container = null;
	protected static final int DEFAULT_TRANSACTION_WAIT = 30000;
	
	protected List channelContainerListener = Collections.synchronizedList(new ArrayList());
	
	protected void fireChannelContainerListeners(IChannelContainerEvent event) {
		synchronized (channelContainerListener) {
			for(Iterator i=channelContainerListener.iterator(); i.hasNext(); ) {
				IChannelContainerListener l = (IChannelContainerListener) i.next();
				if (l != null) l.handleChannelContainerEvent(event);
			}
		}
	}
	public DatashareContainerAdapter(SOContainer container) {
		this.container = container;
		this.container.addListener(new ContainerListener(), null);
	}
	
	protected class ContainerListener implements IContainerListener {
		public void handleEvent(final IContainerEvent evt) {
			if (evt instanceof ISharedObjectActivatedEvent) {
				final ISharedObjectActivatedEvent soae = (ISharedObjectActivatedEvent) evt;
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
			} else if (evt instanceof ISharedObjectDeactivatedEvent) {
				final ISharedObjectDeactivatedEvent sode = (ISharedObjectDeactivatedEvent) evt;
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
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IChannelContainer#createChannel(org.eclipse.ecf.datashare.IChannelConfig)
	 */
	public IChannel createChannel(final ID newID,
			final IChannelListener listener, final Map properties)
			throws ECFException {
		return createChannel(new IChannelConfig() {
			public IChannelListener getListener() {
				return listener;
			}
			public ISharedObjectTransactionConfig getTransactionConfig() {
				return new ISharedObjectTransactionConfig() {
					public int getTimeout() {
						return DEFAULT_TRANSACTION_WAIT;
					}
					public ISharedObjectTransactionParticipantsFilter getParticipantsFilter() {
						return null;
					}};
			}
			public Object getAdapter(Class adapter) {
				return null;
			}
			public SharedObjectDescription getPrimaryDescription() {
				return new SharedObjectDescription(BaseChannel.class, newID,
						properties);
			}
		});
	}
	protected SharedObjectDescription getDefaultChannelDescription()
			throws IDInstantiationException {
		return new SharedObjectDescription(BaseChannel.class, IDFactory
				.getDefault().createGUID(), new HashMap());
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
		SharedObjectDescription sodesc = newChannelConfig.getPrimaryDescription();
		if (sodesc == null)
			sodesc = getDefaultChannelDescription();
		SharedObjectTypeDescription sotypedesc = sodesc.getTypeDescription();
		IChannelListener listener = newChannelConfig.getListener();
		ISharedObjectTransactionConfig transactionConfig = newChannelConfig
				.getTransactionConfig();
		ISharedObject so = null;
		if (sotypedesc.getName() != null) {
			so = SharedObjectFactory
					.getDefault()
					.createSharedObject(
							sotypedesc,
							new String[] {
									ISharedObjectTransactionConfig.class
											.getName(),
									IChannelListener.class.getName() },
							new Object[] { transactionConfig, listener });
		} else {
			so = createSharedObject(sotypedesc, transactionConfig, listener);
		}
		IChannel channel = (IChannel) so.getAdapter(IChannel.class);
		if (channel == null)
			throw new SharedObjectCreateException("Cannot coerce object "
					+ channel + " to be of type IChannel");
		ID newID = sodesc.getID();
		if (newID == null)
			newID = IDFactory.getDefault().createGUID();
		Map properties = sodesc.getProperties();
		if (properties == null)
			properties = new HashMap();
		// Now add channel to container...this will block
		container.getSharedObjectManager().addSharedObject(newID, so, properties);
		return channel;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IChannelContainer#getChannel(org.eclipse.ecf.core.identity.ID)
	 */
	public IChannel getChannel(ID channelID) {
		return (IChannel) container.getSharedObjectManager().getSharedObject(channelID);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.IChannelContainer#disposeChannel(org.eclipse.ecf.core.identity.ID)
	 */
	public boolean removeChannel(ID channelID) {
		ISharedObject o = container.getSharedObjectManager()
				.removeSharedObject(channelID);
		return (o != null);
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ecf.datashare.IChannelContainer#getChannelNamespace()
	 */
	public Namespace getChannelNamespace() {
		return IDFactory.getDefault().getNamespaceByName(StringID.class.getName());
	}
	public void addChannelContainerListener(IChannelContainerListener listener) {
		channelContainerListener.add(listener);
	}
	public void removeChannelContainerListener(IChannelContainerListener listener) {
		channelContainerListener.add(listener);
	}
}
