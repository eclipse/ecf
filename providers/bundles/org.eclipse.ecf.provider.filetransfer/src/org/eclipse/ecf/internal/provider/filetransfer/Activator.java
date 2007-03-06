package org.eclipse.ecf.internal.provider.filetransfer;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;
import org.eclipse.ecf.provider.filetransfer.retrieve.MultiProtocolRetrieveAdapter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	private static final String URL_HANDLER_PROTOCOL_NAME = "url.handler.protocol"; //$NON-NLS-1$

	private static final String URLSTREAM_HANDLER_SERVICE_NAME = "org.osgi.service.url.URLStreamHandlerService"; //$NON-NLS-1$

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.filetransfer"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private BundleContext context;

	private ServiceRegistration fileTransferServiceRegistration;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		this.context = context;
		fileTransferServiceRegistration = context.registerService(
				IRetrieveFileTransferFactory.class.getName(),
				new IRetrieveFileTransferFactory() {
					public IRetrieveFileTransfer newInstance() {
						return new MultiProtocolRetrieveAdapter();
					}
				}, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.context = null;
		if (fileTransferServiceRegistration != null) {
			fileTransferServiceRegistration.unregister();
			fileTransferServiceRegistration = null;
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public String[] getPlatformSupportedSchemes() {
		ServiceTracker handlers = new ServiceTracker(context,
				URLSTREAM_HANDLER_SERVICE_NAME, null);
		handlers.open();
		ServiceReference[] refs = handlers.getServiceReferences();
		Set protocols = new HashSet();
		if (refs != null)
			for (int i = 0; i < refs.length; i++) {
				Object protocol = refs[i]
						.getProperty(URL_HANDLER_PROTOCOL_NAME);
				if (protocol instanceof String)
					protocols.add(protocol);
				else if (protocol instanceof String[]) {
					String[] ps = (String[]) protocol;
					for (int j = 0; j < ps.length; j++)
						protocols.add(ps[j]);
				}
			}
		handlers.close();
		return (String[]) protocols.toArray(new String[] {});
	}
}
