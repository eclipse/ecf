package org.eclipse.ecf.tutorial.datashare;

import org.eclipse.ecf.core.ISharedObjectTransactionConfig;
import org.eclipse.ecf.core.ReplicaSharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.events.IContainerConnectedEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Event;
import org.eclipse.ecf.core.util.IEventProcessor;
import org.eclipse.ecf.datashare.IChannelListener;
import org.eclipse.ecf.provider.datashare.BaseChannel;

public class MyChannel extends BaseChannel {
	
	public MyChannel() {
		super();
	}

	public MyChannel(ISharedObjectTransactionConfig config, IChannelListener listener) {
		super(config, listener);
	}

	protected ReplicaSharedObjectDescription getReplicaDescription(ID targetContainerID) {
		return null;
	}

	protected void initialize() throws SharedObjectInitException {
		super.initialize();
		// Add event processor that responds to IContainerConnectedEvent messages
		addEventProcessor(new IEventProcessor() {
			public boolean acceptEvent(Event event) {
				if (event instanceof IContainerConnectedEvent) return true;
				return false;
			}
			public Event processEvent(Event event) {
				// If event is IContainerConnectedEvent 
				if (event instanceof IContainerConnectedEvent) {
					IContainerConnectedEvent ccevent = (IContainerConnectedEvent) event;
					// Check to make sure it's a client...not the groupID
					if (!ccevent.getTargetID().equals(getGroupID())) sendHelloMessage();
				}
				return event;
			}});
	}
	
	protected void sendHelloMessage() {
		try {
			// send message
			this.sendMessage(("hello from "+getHomeContainerID().getName()).getBytes());
		} catch (ECFException e) {
			e.printStackTrace();
		}
	}
}
