package org.eclipse.ecf.ds.impl;

import java.io.Serializable;

import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ITransactionConfiguration;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsgEvent;
import org.eclipse.ecf.core.sharedobject.TransactionSharedObject;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.ds.IChannel;
import org.eclipse.ecf.ds.IChannelChangeEvent;
import org.eclipse.ecf.ds.IChannelListener;
import org.eclipse.ecf.ds.IChannelMessageEvent;

public class ChannelImpl extends TransactionSharedObject implements IChannel {

	class ChannelMsg implements Serializable {
		private static final long serialVersionUID = -6752722047308362941L;
		byte [] channelData = null;
		
		ChannelMsg() {}
		ChannelMsg(byte [] data) {
			this.channelData = data;
		}
		byte [] getData() {
			return channelData;
		}
	}

	IChannelListener listener;
	
	public ChannelImpl() {
		this(null);
	}
	public ChannelImpl(ITransactionConfiguration config) {
		super(config);
	}
	public void addChannelListener(IChannelListener listener) {
		this.listener = listener;
	}
	protected void initialize() {
		super.initialize();
		addEventProcessor(new IEventProcessor() {
			public boolean acceptEvent(Event event) {
				if (event instanceof IContainerConnectedEvent) {
					return true;
				} else if (event instanceof IContainerDisconnectedEvent) {
					return true;
				}
				return false;
			}
			public Event processEvent(Event event) {
				if (event instanceof IContainerConnectedEvent) {
					ChannelImpl.this.listener.handleChannelEvent(createChannelChangeEvent(true,((IContainerConnectedEvent)event).getTargetID()));
				} else if (event instanceof IContainerDisconnectedEvent) {
					ChannelImpl.this.listener.handleChannelEvent(createChannelChangeEvent(true,((IContainerDisconnectedEvent)event).getTargetID()));
				}
				return event;
			}
		});
	}
	
	protected IChannelChangeEvent createChannelChangeEvent(final boolean hasJoined,final ID targetID) {
		return new IChannelChangeEvent() {
			private static final long serialVersionUID = -1085237280463725283L;
			public boolean hasJoined() {
				return hasJoined;
			}
			public ID getTargetID() {
				return targetID;
			}
			public ID getChannelID() {
				return getID();
			}
		};
	}
    protected Event handleSharedObjectMsgEvent(final SharedObjectMsgEvent event) {
    	Object data = event.getData();
    	ChannelMsg channelData = null;
    	if (data instanceof ChannelMsg) {
    		channelData = (ChannelMsg) data;
    	}
    	if (channelData != null) {
    		listener.handleChannelEvent(new IChannelMessageEvent() {
				private static final long serialVersionUID = -2270885918818160970L;
				public ID getFromID() {
					return event.getSenderSharedObjectID();
				}
				public byte[] getData() {
					return (byte []) event.getData();
				}
				public ID getChannelID() {
					return getID();
				}});
    		// Discontinue processing of this event...we are it
    		return null;
    	}
    	return event;
    }
	
	public void sendMessage(byte[] message) throws ECFException {
		sendMessage(null,message);
	}

	public void sendMessage(ID receiver, byte[] message) throws ECFException {
		try {
			getContext().sendMessage(receiver, new ChannelMsg(message));
		} catch (Exception e) {
			throw new ECFException("send message exception",e);
		}
	}

}
