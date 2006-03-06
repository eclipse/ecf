package org.eclipse.ecf.example.clients;

import java.util.Map;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.ecf.datashare.IChannelContainer;
import org.eclipse.ecf.datashare.IChannelListener;

public class DatashareClient {

	protected IChannel createChannel(IContainer container, String channelName,
			IChannelListener channelListener, Map channelProps)
			throws ECFException {
		IChannelContainer channelContainer = (IChannelContainer) container
				.getAdapter(IChannelContainer.class);
		if (channelContainer == null)
			throw new NullPointerException(
					"cannot get channel container adapter");
		return channelContainer.createChannel(IDFactory.getDefault().createID(
				channelContainer.getChannelNamespace(), channelName),
				channelListener, channelProps);
	}
	
	protected IContainer createContainer(String containerType) throws ECFException {
		return ContainerFactory.getDefault().createContainer(containerType);
	}

}
