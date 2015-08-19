package org.eclipse.ecf.remoteservice.provider;

import java.util.Dictionary;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.identity.Namespace;

/**
 * Basic implementation of IRemoteServiceDistributionProvider.  Intended to be subclassed by distribution
 * provider implementations.
 * 
 * @since 8.7
 */
public abstract class RemoteServiceDistributionProvider implements IRemoteServiceDistributionProvider {

	public abstract ContainerTypeDescription createContainerTypeDescription();

	public Dictionary<String, ?> getContainerTypeDescriptionProperties() {
		return null;
	}

	public Namespace createNamespace() {
		return null;
	}

	public Dictionary<String, ?> getNamespaceProperties() {
		return null;
	}
}
