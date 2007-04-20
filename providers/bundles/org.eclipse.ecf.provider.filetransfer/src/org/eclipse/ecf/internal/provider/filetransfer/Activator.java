package org.eclipse.ecf.internal.provider.filetransfer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;
import org.eclipse.ecf.provider.filetransfer.retrieve.MultiProtocolRetrieveAdapter;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

	private static final String CLASS_ATTR = "class"; //$NON-NLS-1$
	private static final String PROTOCOL_ATTR = "protocol"; //$NON-NLS-1$

	private static final String[] jvmSchemes = new String[] {
		Messages.FileTransferNamespace_Http_Protocol,
		Messages.FileTransferNamespace_Ftp_Protocol,
		Messages.FileTransferNamespace_File_Protocol,
		Messages.FileTransferNamespace_Jar_Protocol,
		Messages.FileTransferNamespace_Https_Protocol,
		Messages.FileTransferNamespace_Mailto_Protocol,
		Messages.FileTransferNamespace_Gopher_Protocol};


	private static final String URL_HANDLER_PROTOCOL_NAME = "url.handler.protocol"; //$NON-NLS-1$

	private static final String URLSTREAM_HANDLER_SERVICE_NAME = "org.osgi.service.url.URLStreamHandlerService"; //$NON-NLS-1$

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.ecf.provider.filetransfer"; //$NON-NLS-1$

	private static final String FILETRANSFER_PROTOCOL_FACTORY_EPOINT = PLUGIN_ID + "." //$NON-NLS-1$
	+ "fileTransferProtocolFactory"; //$NON-NLS-1$
	
	// The shared instance
	private static Activator plugin;

	private BundleContext context = null;

	private ServiceRegistration fileTransferServiceRegistration;

	private ServiceTracker logServiceTracker = null;
	private ServiceTracker extensionRegistryTracker = null;
	
	private Map fileTransferProtocolMap = null;


	/**
	 * The constructor
	 */
	public Activator() {
	}
	
	protected LogService getLogService() {
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context,
					LogService.class.getName(), null);
			logServiceTracker.open();
		}
		return (LogService) logServiceTracker.getService();
	}

	public void log(IStatus status) {
		LogService logService = getLogService();
		if (logService != null) {
			logService.log(LogHelper.getLogCode(status), LogHelper
					.getLogMessage(status), status.getException());
		}
	}

	public Bundle getBundle() {
		if (context == null) return null;
		return context.getBundle();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		this.context = context;
		fileTransferServiceRegistration = context.registerService(
				IRetrieveFileTransferFactory.class.getName(),
				new IRetrieveFileTransferFactory() {
					public IRetrieveFileTransfer newInstance() {
						return new MultiProtocolRetrieveAdapter();
					}
				}, null);
		// Can't be lazy about this, as schemes need to be registered with platform
		loadProtocolHandlers();
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
		this.context = null;
		this.fileTransferProtocolMap = null;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public synchronized static Activator getDefault() {
		if (plugin == null) {
			plugin = new Activator();
		}
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
		for (int i = 0; i < jvmSchemes.length; i++)
			protocols.add(jvmSchemes[i]);
		return (String[]) protocols.toArray(new String[] {});
	}
	
	public IExtensionRegistry getExtensionRegistry() {
		if (extensionRegistryTracker == null) {
			this.extensionRegistryTracker = new ServiceTracker(context,
					IExtensionRegistry.class.getName(), null);
			this.extensionRegistryTracker.open();
		}
		return (IExtensionRegistry) extensionRegistryTracker.getService();
	}
	
	// TODO we need to be dynamic here
	private void loadProtocolHandlers() {
		this.fileTransferProtocolMap = new HashMap(3);
		IExtensionRegistry reg = getExtensionRegistry();
		if (reg != null) {
			IExtensionPoint extensionPoint = reg
					.getExtensionPoint(FILETRANSFER_PROTOCOL_FACTORY_EPOINT);
			if (extensionPoint == null) {
				return;
			}
			IConfigurationElement[] configurationElements = extensionPoint
					.getConfigurationElements();

			String [] existingSchemes = getPlatformSupportedSchemes();
			
			for (int i = 0; i < configurationElements.length; i++) {
				try {
					String protocol = configurationElements[i].getAttribute(PROTOCOL_ATTR);
					// If the protocol is not already registered as a scheme with platform
					// then register
					if (!isSchemeRegistered(protocol,existingSchemes)) registerScheme(protocol);
					IRetrieveFileTransferFactory clazz = (IRetrieveFileTransferFactory) configurationElements[i]
							.createExecutableExtension(CLASS_ATTR);
					fileTransferProtocolMap.put(protocol, clazz);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean isSchemeRegistered(String protocol, String [] schemes) {
		for(int i=0; i < schemes.length; i++) {
			if (protocol.equals(schemes[i])) return true;
		}
		return false;
	}
	
	class DummyURLStreamHandlerService extends AbstractURLStreamHandlerService {

		/* (non-Javadoc)
		 * @see org.osgi.service.url.AbstractURLStreamHandlerService#openConnection(java.net.URL)
		 */
		public URLConnection openConnection(URL u) throws IOException {
			throw new IOException(NLS.bind(Messages.Activator_EXCEPTION_URLConnection_CANNOT_BE_CREATED,u.toExternalForm()));
		}
		
	}
	
	private DummyURLStreamHandlerService dummyService = new DummyURLStreamHandlerService();
	
	private void registerScheme(String protocol) {
		Hashtable properties = new Hashtable();
		properties.put(URLConstants.URL_HANDLER_PROTOCOL,
				new String[] { protocol });
		context.registerService(URLStreamHandlerService.class
				.getName(), dummyService, properties);
	}
	
	// TODO we can be more lazy here
	public IRetrieveFileTransfer getFileTransfer(String protocol) {
		IRetrieveFileTransferFactory factory = (IRetrieveFileTransferFactory) fileTransferProtocolMap.get(protocol);
		if(factory != null)
			return factory.newInstance();
		return null;
	}
	
}
