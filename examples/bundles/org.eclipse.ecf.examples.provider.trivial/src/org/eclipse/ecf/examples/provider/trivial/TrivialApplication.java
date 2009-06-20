package org.eclipse.ecf.examples.provider.trivial;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.util.tracker.ServiceTracker;

public class TrivialApplication implements IApplication {

	private boolean done = false;

	private ServiceTracker containerManagerTracker;
	
	public Object start(IApplicationContext context) throws Exception {
		try {
			IContainerFactory factory = getContainerManager().getContainerFactory();
			// Create instance of trivial container
			IContainer container = factory.createContainer("ecf.container.trivial");

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
		if (containerManagerTracker != null) {
			containerManagerTracker.close();
			containerManagerTracker = null;
		}
		synchronized (this) {
			done = true;
			notify();
		}
	}

	protected IContainerManager getContainerManager() {
		if (containerManagerTracker == null) {
			containerManagerTracker = new ServiceTracker(
					org.eclipse.ecf.internal.examples.provider.trivial.Activator.getDefault().getContext(), IContainerManager.class.getName(),
					null);
			containerManagerTracker.open();
		}
		return (IContainerManager) containerManagerTracker.getService();
	}


}
