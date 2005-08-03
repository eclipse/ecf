package org.eclipse.ecf.ui.views;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.discovery.IDiscoveryContainer;
import org.eclipse.ecf.discovery.IServiceInfo;

public interface IDiscoveryController {
	public IDiscoveryContainer getDiscoveryContainer();
	public IContainer getContainer();
	public String [] getServiceTypes();
	public void connectToService(IServiceInfo service);
	public void setupDiscoveryContainer(DiscoveryView view);
	public void disposeDiscoveryContainer(DiscoveryView view);
}
