package org.eclipse.ecf.internal.sync;

import java.util.Dictionary;
import java.util.Properties;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.util.LogHelper;
import org.eclipse.ecf.core.util.PlatformHelper;
import org.eclipse.ecf.internal.sync.doc.cola.ColaSynchronizationStrategyFactory;
import org.eclipse.ecf.internal.sync.doc.identity.IdentitySynchronizationStrategyFactory;
import org.eclipse.ecf.sync.IServiceConstants;
import org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategyFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.sync";

	private static Activator bundle;
	private ServiceRegistration colaServiceRegistration;
	private ServiceRegistration identityServiceRegistration;
	private BundleContext context;

	IDocumentSynchronizationStrategyFactory identity;
	IDocumentSynchronizationStrategyFactory cola;
	
	private ServiceTracker adapterManagerTracker = null;
	private ServiceTracker logServiceTracker = null;

	public IAdapterManager getAdapterManager() {
		// First, try to get the adapter manager via
		if (adapterManagerTracker == null) {
			adapterManagerTracker = new ServiceTracker(this.context, IAdapterManager.class.getName(), null);
			adapterManagerTracker.open();
		}
		IAdapterManager adapterManager = (IAdapterManager) adapterManagerTracker.getService();
		// Then, if the service isn't there, try to get from Platform class via
		// PlatformHelper class
		if (adapterManager == null)
			adapterManager = PlatformHelper.getPlatformAdapterManager();
		if (adapterManager == null)
			getDefault().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, "Cannot get adapter manager", null)); //$NON-NLS-1$
		return adapterManager;
	}

	protected LogService getLogService() {
		if (logServiceTracker == null) {
			logServiceTracker = new ServiceTracker(this.context, LogService.class.getName(), null);
			logServiceTracker.open();
		}
		return (LogService) logServiceTracker.getService();
	}

	public void log(IStatus status) {
		LogService logService = getLogService();
		if (logService != null) {
			logService.log(LogHelper.getLogCode(status), LogHelper.getLogMessage(status), status.getException());
		}
	}

	public static Activator getDefault() {
		return bundle;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext ctxt) throws Exception {
		bundle = this;
		this.context = ctxt;
		this.identity = new IdentitySynchronizationStrategyFactory();
		this.cola = new ColaSynchronizationStrategyFactory();
		// Register identity synchronizer service
		final Dictionary identityServiceProps = new Properties();
		identityServiceProps.put(IServiceConstants.SYNCSTRATEGY_TYPE_PROPERTY, IdentitySynchronizationStrategyFactory.SYNCHSTRATEGY_TYPE);
		identityServiceProps.put(IServiceConstants.SYNCSTRATEGY_PROVIDER_PROPETY, IdentitySynchronizationStrategyFactory.SYNCHSTRATEGY_PROVIDER);
		identityServiceRegistration = this.context.registerService(IDocumentSynchronizationStrategyFactory.class.getName(), this.identity, identityServiceProps);
		// Register cola synchronizer service
		final Dictionary colaServiceProps = new Properties();
		colaServiceProps.put(IServiceConstants.SYNCSTRATEGY_TYPE_PROPERTY, ColaSynchronizationStrategyFactory.SYNCHSTRATEGY_TYPE);
		colaServiceProps.put(IServiceConstants.SYNCSTRATEGY_PROVIDER_PROPETY, ColaSynchronizationStrategyFactory.SYNCHSTRATEGY_PROVIDER);
		colaServiceRegistration = this.context.registerService(IDocumentSynchronizationStrategyFactory.class.getName(), this.cola, colaServiceProps);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (colaServiceRegistration != null) {
			colaServiceRegistration.unregister();
			colaServiceRegistration = null;
		}
		if (identityServiceRegistration != null) {
			identityServiceRegistration.unregister();
			identityServiceRegistration = null;
		}
		if (this.identity != null) {
			this.identity.dispose();
			this.identity = null;
		}
		if (this.cola != null) {
			this.cola.dispose();
			this.cola = null;
		}
		if (logServiceTracker != null) {
			logServiceTracker.close();
			logServiceTracker = null;
		}
		if (adapterManagerTracker != null) {
			adapterManagerTracker.close();
			adapterManagerTracker = null;
		}
		this.context = null;
		bundle = null;
	}

}
