package org.eclipse.ecf.example.collab;

import java.net.InetAddress;
import java.net.URI;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.ISharedObjectContainer;
import org.eclipse.ecf.core.SharedObjectContainerFactory;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.ServiceID;
import org.eclipse.ecf.discovery.IDiscoveryContainer;
import org.eclipse.ecf.discovery.IServiceEvent;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceListener;
import org.eclipse.ecf.discovery.IServiceTypeListener;
import org.eclipse.ecf.discovery.ServiceInfo;
import org.eclipse.ecf.ui.views.DiscoveryView;
import org.eclipse.ecf.ui.views.IServiceConnectListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class DiscoveryStartup {

	public static final String DISCOVERY_CONTAINER = "org.eclipse.ecf.provider.jmdns.container.JmDNS";
	public static final String TCPSERVER_DISCOVERY_TYPE = "_ecftcp._tcp.local.";
	public static final String PROP_PROTOCOL_NAME = "protocol";
	public static final String PROP_CONTAINER_TYPE_NAME = "containertype";
	public static final String PROP_CONTAINER_TYPE_VALUE = Client.GENERIC_CONTAINER_CLIENT_NAME;
	public static final String PROP_PW_REQ_NAME = "pwrequired";
	public static final String PROP_PW_REQ_VALUE = "false";
	public static final String PROP_DEF_USER_NAME = "defaultuser";
	public static final String PROP_DEF_USER_VALUE = "guest";
	public static final String PROP_PATH_NAME = "path";
	public static final int SVC_DEF_WEIGHT = 0;
	public static final int SVC_DEF_PRIORITY = 0;
	
	static IDiscoveryContainer discovery = null;
    protected DiscoveryView discoveryView = null;
    
    static String serviceTypes[] = new String[] {
            TCPSERVER_DISCOVERY_TYPE,
            "_http._tcp.local."
        };
	
	public static IDiscoveryContainer getDefault() {
		return discovery;
	}
	public void showTypeDetails(boolean val) {
		if (discoveryView != null) {
			discoveryView.setShowTypeDetails(val);
		}
	}
	public DiscoveryStartup() {
		if (ClientPlugin.getDefault().getPreferenceStore().getBoolean(ClientPlugin.PREF_REGISTER_SERVER)) {
			setupDiscovery();
		}
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
				container.joinGroup(null,null);
				registerServiceTypes();
			}
			else {
				ClientPlugin.log("No discovery container available");
			}
		} catch (Exception e) {
			ClientPlugin.log("Exception creating discovery container",e);
		}

	}

	protected void connectToService(IServiceInfo svcInfo) {
		// XXX TODO
	}
	
    protected void setupDiscoveryContainer(final IDiscoveryContainer dc) {
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                try {
                    IWorkbenchWindow ww = PlatformUI.getWorkbench()
                            .getActiveWorkbenchWindow();
                    IWorkbenchPage wp = ww.getActivePage();
                    IViewPart view = wp.showView("org.eclipse.ecf.ui.view.discoveryview");
                    discoveryView = (DiscoveryView) view;
                    discoveryView.setDiscoveryContainer(dc);
                    discoveryView.setShowTypeDetails(false);
                    discoveryView.setServiceConnectListener(new IServiceConnectListener() {
						public void connectToService(IServiceInfo service) {
							connectToService(service);
						}
                    });
                } catch (Exception e) {
                    IStatus status = new Status(IStatus.ERROR,ClientPlugin.PLUGIN_ID,IStatus.OK,"Exception showing presence view",e);
                    ClientPlugin.getDefault().getLog().log(status);
                }
            }
        });
        if (discoveryView != null) {
	        dc.addServiceTypeListener(new IServiceTypeListener() {
				public void serviceTypeAdded(IServiceEvent event) {
					ServiceID svcID = event.getServiceInfo().getServiceID();
					discoveryView.addServiceTypeInfo(svcID.getServiceType());
					dc.addServiceListener(event.getServiceInfo().getServiceID(), new IServiceListener() {
						public void serviceAdded(IServiceEvent evt) {
							discoveryView.addServiceInfo(evt.getServiceInfo().getServiceID());
							dc.requestServiceInfo(evt.getServiceInfo().getServiceID(),3000);
						}
						public void serviceRemoved(IServiceEvent evt) {
							discoveryView.removeServiceInfo(evt.getServiceInfo());
						}
						public void serviceResolved(IServiceEvent evt) {
							discoveryView.addServiceInfo(evt.getServiceInfo());
						}});
					dc.registerServiceType(svcID);
				}});
        }
	}
	public static void unregisterServerType() {
		if (discovery != null) {
			discovery.unregisterAllServices();
		}
	}
	public static void registerServer(ID id) {
		if (discovery != null) {
			String name = id.getName();
			try {
				URI uri = id.toURI();
				String path = uri.getPath();
				Properties props = new Properties();
				String protocol = uri.getScheme();
				props.setProperty(PROP_CONTAINER_TYPE_NAME,PROP_CONTAINER_TYPE_VALUE);
				props.setProperty(PROP_PROTOCOL_NAME,protocol);
				props.setProperty(PROP_PW_REQ_NAME, PROP_PW_REQ_VALUE);
				props.setProperty(PROP_DEF_USER_NAME, PROP_DEF_USER_VALUE);
				props.setProperty(PROP_PATH_NAME, path);
				InetAddress host = InetAddress.getByName(uri.getHost());
				int port = uri.getPort();
				String svcName = System.getProperty("user.name")+"."+protocol;
				discovery.registerService(new ServiceInfo(host, new ServiceID(TCPSERVER_DISCOVERY_TYPE,svcName), port, SVC_DEF_PRIORITY,
						SVC_DEF_WEIGHT, props));
			} catch (Exception e) {
				ClientPlugin.log("Exception registering server " + name, e);
			}
		} 
	}

	public static void unregisterServer(ISharedObjectContainer container) {

	}

	public static void registerServiceTypes() {
		if (discovery != null) {
			for(int i=0; i < serviceTypes.length; i++) {
				discovery.registerServiceType(serviceTypes[i]);
			}
		}
	}


}
