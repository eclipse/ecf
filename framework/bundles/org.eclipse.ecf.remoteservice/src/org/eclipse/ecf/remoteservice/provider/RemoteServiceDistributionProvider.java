package org.eclipse.ecf.remoteservice.provider;

import java.util.Dictionary;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.identity.Namespace;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Basic implementation of IRemoteServiceDistributionProvider.  Intended to be subclassed by distribution
 * provider implementations.
 * 
 * @since 8.7
 */
public abstract class RemoteServiceDistributionProvider implements IRemoteServiceDistributionProvider {

	public ServiceRegistration<ContainerTypeDescription> registerContainerTypeDescription(BundleContext context) {
		return context.registerService(ContainerTypeDescription.class, getContainerTypeDescription(), getContainerTypeDescriptionProperties());
	}

	protected abstract ContainerTypeDescription getContainerTypeDescription();

	protected Dictionary<String, ?> getContainerTypeDescriptionProperties() {
		return null;
	}

	public ServiceRegistration<Namespace> registerNamespace(BundleContext context) {
		Namespace ns = getNamespace();
		return (ns == null) ? null : context.registerService(Namespace.class, ns, getNamespaceProperties());
	}

	protected Namespace getNamespace() {
		return null;
	}

	protected Dictionary<String, ?> getNamespaceProperties() {
		return null;
	}
}
