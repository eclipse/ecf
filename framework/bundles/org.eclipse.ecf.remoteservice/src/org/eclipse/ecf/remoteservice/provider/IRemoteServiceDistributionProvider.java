package org.eclipse.ecf.remoteservice.provider;

import java.util.Dictionary;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.identity.Namespace;

/**
 * @since 8.7
 * A service interface for distribution providers.   When instances of this interface are registered, they result in the
 * two methods below being called by the org.eclipse.ecf.remoteservice bundle, with the BundleContext from
 * the org.eclipse.ecf.remoteservice bundle.  Intended to be implemented by remote service distribution provider
 * implementations.
 */
public interface IRemoteServiceDistributionProvider {

	ContainerTypeDescription createContainerTypeDescription();

	Dictionary<String, ?> getContainerTypeDescriptionProperties();

	Namespace createNamespace();

	Dictionary<String, ?> getNamespaceProperties();

}
