package org.eclipse.ecf.ds.impl;

import java.io.Serializable;

import org.eclipse.ecf.core.events.ISharedObjectContainerConnectedEvent;
import org.eclipse.ecf.core.events.ISharedObjectContainerDisconnectedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.AbstractSharedObject;
import org.eclipse.ecf.core.sharedobject.SharedObjectMsgEvent;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.ds.IChannel;
import org.eclipse.ecf.ds.IChannelListener;

public class ChannelImpl extends AbstractSharedObject implements IChannel {

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
	
	public ChannelImpl(IChannelListener listener) {
		this.listener = listener;
	}
	
	protected void initialize() {
		super.initialize();
		addEventProcessor(new IEventProcessor() {
			public boolean acceptEvent(Event event) {
				if (event instanceof ISharedObjectContainerConnectedEvent) {
					return true;
				} else if (event instanceof ISharedObjectContainerDisconnectedEvent) {
					return true;
				}
				return false;
			}
			public Event processEvent(Event event) {
				if (event instanceof ISharedObjectContainerConnectedEvent) {
					ChannelImpl.this.listener.handleMemberJoined(getID(), ((ISharedObjectContainerConnectedEvent)event).getTargetID());
				} else if (event instanceof ISharedObjectContainerDisconnectedEvent) {
					ChannelImpl.this.listener.handleMemberDeparted(getID(), ((ISharedObjectContainerDisconnectedEvent)event).getTargetID());
				}
				return event;
			}
		});
	}
	
    protected Event handleSharedObjectMsgEvent(SharedObjectMsgEvent event) {
    	Object data = event.getData();
    	ChannelMsg channelData = null;
    	if (data instanceof ChannelMsg) {
    		channelData = (ChannelMsg) data;
    	}
    	if (channelData != null) {
    		listener.handleMessage(getID(),channelData.getData());
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

	public ID[] getCurrentMembership() {
		try {
			return getContext().getGroupMemberIDs();
		} catch (Exception e) {
			return new ID[0];
		}
	}

}
