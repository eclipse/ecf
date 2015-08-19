package org.eclipse.ecf.remoteservice.provider;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.identity.Namespace;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * @since 8.7
 * A service interface for distribution providers.   When instances of this interface are registered, they result in the
 * two methods below being called by the org.eclipse.ecf.remoteservice bundle, with the BundleContext from
 * the org.eclipse.ecf.remoteservice bundle.  Intended to be implemented by remote service distribution provider
 * implementations.
 */
public interface IRemoteServiceDistributionProvider {

	/**
	 * Register the appropriate ContainerTypeDescription instance given the bundle context.
	 * @param context the BundleContext that will register the ContainerTypeDescription.  Will not be <code>null</code>.
	 * @return ServiceRegistration for the registered ContainerTypeDescription.
	 */
	ServiceRegistration<ContainerTypeDescription> registerContainerTypeDescription(BundleContext context);

	/**
	 * Register the appropriate Namespace instance given the bundle context.
	 * @param context the BundleContext that will register the Namespace.  Will not be <code>null</code>.
	 * @return ServiceRegistration for the registered Namespace.
	 */
	ServiceRegistration<Namespace> registerNamespace(BundleContext context);

}
