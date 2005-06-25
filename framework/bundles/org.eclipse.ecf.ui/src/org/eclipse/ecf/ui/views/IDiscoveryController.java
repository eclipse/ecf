package org.eclipse.ecf.ui.views;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.discovery.IDiscoveryContainer;
import org.eclipse.ecf.discovery.IServiceInfo;

public interface IDiscoveryController {
	public IDiscoveryContainer getDiscoveryContainer();
	public ISharedObjectContainer getSharedObjectContainer();
	public String [] getServiceTypes();
	public void connectToService(IServiceInfo service);
	public void setupDiscoveryContainer(DiscoveryView view);
	public void disposeDiscoveryContainer(DiscoveryView view);
}
