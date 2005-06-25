package org.eclipse.ecf.ui.views;

import org.eclipse.ecf.discovery.IServiceInfo;

public interface IDiscoveryControlListener {
	public void connectToService(IServiceInfo service);
	public void setupDiscoveryContainer(DiscoveryView view);
	public void disposeDiscoveryContainer(DiscoveryView view);
}
