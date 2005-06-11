package org.eclipse.ecf.example.collab;

import java.net.InetAddress;
import java.net.URI;
import java.util.Properties;

import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.SharedObjectContainerFactory;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.ServiceID;
import org.eclipse.ecf.discovery.IDiscoveryContainer;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceTypeListener;
import org.eclipse.ecf.discovery.ServiceInfo;

public class DiscoveryStartup {

	public static final String DISCOVERY_CONTAINER = "org.eclipse.ecf.provider.jmdns.container.JmDNS";
	public static final String TCPSERVER_DISCOVERY_TYPE = "_http._tcp._local.";

	static IDiscoveryContainer discovery = null;

	public static IDiscoveryContainer getDefault() {
		return discovery;
	}
	
	public DiscoveryStartup() {
		setupDiscovery();
	}
	
	public void dispose() {
		unregisterServerType();
	}
	protected void setupDiscovery() {
		try {
			ISharedObjectContainer container = SharedObjectContainerFactory
					.makeSharedObjectContainer(DISCOVERY_CONTAINER);
			discovery = (IDiscoveryContainer) container
					.getAdapter(IDiscoveryContainer.class);
			if (discovery != null) {
				setupDiscoveryContainer(discovery);
				container.joinGroup(new ServiceID(TCPSERVER_DISCOVERY_TYPE, null),null);
				//registerServerType();
			}
			else {
				ClientPlugin.log("No discovery container available");
			}
		} catch (Exception e) {
			ClientPlugin.log("Exception creating discovery container",e);
		}

	}

	protected void setupDiscoveryContainer(final IDiscoveryContainer dc) {
		dc.addServiceTypeListener(new IServiceTypeListener() {
			public void serviceTypeAdded(IServiceEvent event) {
				System.out.println("serviceTypeAdded("+event.getServiceInfo());
				//ServiceID svcID = event.getServiceInfo().getServiceID();
				/*
				dc.addServiceListener(event.getServiceInfo().getServiceID(),
						new IServiceListener() {
							public void serviceAdded(IServiceEvent event) {
								System.out.println("serviceAdded(" + event.getServiceInfo());
								log("serviceAdded(" + event.getServiceInfo());
								dc.requestServiceInfo(event.getServiceInfo()
										.getServiceID(), 3000);
							}

							public void serviceRemoved(IServiceEvent event) {
								System.out.println("serviceRemoved(" + event.getServiceInfo());
								log("serviceRemoved(" + event.getServiceInfo());
							}

							public void serviceResolved(IServiceEvent event) {
								System.out.println("serviceResolved(" + event.getServiceInfo());
								log("serviceResolved(" + event.getServiceInfo());
							}
						});
				dc.registerServiceType(svcID);
				*/
			}
		});

	}

	public static void unregisterServerType() {
		if (discovery != null) {
			discovery.unregisterAllServices();
		}
	}
	public static void registerServer(ID id) {
		if (discovery != null) {
			// Make service info
			String name = id.getName();
			try {
				URI uri = id.toURI();
				String path = uri.getPath();
				Properties props = new Properties();
				props.setProperty("id", name);
				props.setProperty("passwordrequired", "false");
				props.setProperty("defaultuser", "guest");
				props.setProperty("path", path);
				// Get localhost InetAddress
				InetAddress host = InetAddress.getByName(uri.getHost());
				int port = uri.getPort();
				ServiceID svcID = new ServiceID(TCPSERVER_DISCOVERY_TYPE,"foo");
				ServiceInfo svcinfo = new ServiceInfo(host, svcID, port, 0,
						0, props);
				// Then register service
				System.out.println("registering serviceinfo "+svcinfo);
				discovery.registerService(svcinfo);
			} catch (Exception e) {
				ClientPlugin.log("Exception registering server " + name, e);
			}
		}
	}

	public static void unregisterServer(ISharedObjectContainer container) {

	}

	public static void registerServerType() {
		if (discovery != null) {
			discovery.registerServiceType(TCPSERVER_DISCOVERY_TYPE);
		}
	}


}
