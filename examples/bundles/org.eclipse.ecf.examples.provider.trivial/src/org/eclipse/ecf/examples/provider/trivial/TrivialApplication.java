package org.eclipse.ecf.examples.provider.trivial;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class TrivialApplication implements IApplication {

	private boolean done = false;

	public Object start(IApplicationContext context) throws Exception {
		try {
			// Create instance of trivial container
			IContainer container = ContainerFactory.getDefault()
					.createContainer("ecf.container.trivial");

			// Get appropriate container adapter...e.g. IChannelContainerAdapter
			// IChannelContainerAdapter containerAdapter =
			// (IChannelContainerAdapter)
			// container.getAdapter(IChannelContainerAdapter.class);

			// Connect
			ID targetID = IDFactory.getDefault().createID(
					container.getConnectNamespace(), "myid");
			container.connect(targetID, null);

			synchronized (this) {
				while (!done) {
					wait();
				}
			}

		} catch (ECFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void stop() {
		synchronized (this) {
			done = true;
			notify();
		}
	}

}
