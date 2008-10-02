package org.eclipse.ecf.internal.sync;

import java.util.Dictionary;
import java.util.Properties;

import org.eclipse.ecf.internal.sync.doc.cola.ColaSynchronizationStrategyFactory;
import org.eclipse.ecf.internal.sync.doc.identity.IdentitySynchronizationStrategyFactory;
import org.eclipse.ecf.sync.IServiceConstants;
import org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategyFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "org.eclipse.ecf.sync";

	private static Activator bundle;
	private ServiceRegistration colaServiceRegistration;
	private ServiceRegistration identityServiceRegistration;
	private BundleContext context;

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
		// Register identity synchronizer service
		final Dictionary identityServiceProps = new Properties();
		identityServiceProps.put(IServiceConstants.SYNCSTRATEGY_TYPE_PROPERTY, IdentitySynchronizationStrategyFactory.SYNCHSTRATEGY_TYPE);
		identityServiceProps.put(IServiceConstants.SYNCSTRATEGY_PROVIDER_PROPETY, IdentitySynchronizationStrategyFactory.SYNCHSTRATEGY_PROVIDER);
		identityServiceRegistration = this.context.registerService(IDocumentSynchronizationStrategyFactory.class.getName(), new IdentitySynchronizationStrategyFactory(), identityServiceProps);
		// Register cola synchronizer service
		final Dictionary colaServiceProps = new Properties();
		colaServiceProps.put(IServiceConstants.SYNCSTRATEGY_TYPE_PROPERTY, ColaSynchronizationStrategyFactory.SYNCHSTRATEGY_TYPE);
		colaServiceProps.put(IServiceConstants.SYNCSTRATEGY_PROVIDER_PROPETY, ColaSynchronizationStrategyFactory.SYNCHSTRATEGY_PROVIDER);
		colaServiceRegistration = this.context.registerService(IDocumentSynchronizationStrategyFactory.class.getName(), new ColaSynchronizationStrategyFactory(), colaServiceProps);
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
		this.context = null;
		bundle = null;
	}

}
