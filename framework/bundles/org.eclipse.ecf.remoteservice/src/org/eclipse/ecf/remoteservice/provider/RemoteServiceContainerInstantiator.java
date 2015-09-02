package org.eclipse.ecf.remoteservice.provider;

import java.util.*;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.provider.BaseContainerInstantiator;
import org.eclipse.ecf.core.provider.IRemoteServiceContainerInstantiator;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;

/**
 * @since 8.7
 */
public abstract class RemoteServiceContainerInstantiator extends BaseContainerInstantiator implements IRemoteServiceContainerInstantiator {

	protected static final String[] defaultSupportedAdapterTypes = new String[] {IRemoteServiceContainerAdapter.class.getName()};
	protected static final Class[][] defaultSupportedParameterTypes = new Class[][] {{Map.class}};

	protected static final String[] defaultSupportedIntents = new String[] {"passByValue", "exactlyOnce", "ordered"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		return defaultSupportedAdapterTypes;
	}

	public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {
		return defaultSupportedParameterTypes;
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		return defaultSupportedIntents;
	}

	protected List<String> exporterConfigs;
	protected Map<String, List<String>> exporterConfigToImporterConfigs;

	protected RemoteServiceContainerInstantiator(List<String> exporterConfigs, Map<String, List<String>> exporterConfigToImporterConfig) {
		this.exporterConfigs = (exporterConfigs == null) ? new ArrayList<String>() : exporterConfigs;
		this.exporterConfigToImporterConfigs = (exporterConfigToImporterConfig == null) ? new HashMap<String, List<String>>() : exporterConfigToImporterConfig;
	}

	protected RemoteServiceContainerInstantiator() {
		this.exporterConfigs = new ArrayList<String>();
		this.exporterConfigToImporterConfigs = new HashMap<String, List<String>>();
	}

	public String[] getSupportedConfigs(ContainerTypeDescription description) {
		List<String> results = new ArrayList<String>();
		String descriptionName = description.getName();
		if (this.exporterConfigs.contains(descriptionName))
			results.add(descriptionName);
		return results.toArray(new String[results.size()]);
	}

	public String[] getImportedConfigs(ContainerTypeDescription description, String[] exporterSupportedConfigs) {
		if (exporterSupportedConfigs == null)
			return null;
		List<String> results = new ArrayList<String>();
		for (String exporterConfig : exporterSupportedConfigs) {
			List<String> importerConfigs = exporterConfigToImporterConfigs.get(exporterConfig);
			if (importerConfigs != null)
				for (String importerConfig : importerConfigs)
					if (description.getName().equals(importerConfig))
						results.add(importerConfig);
		}
		return results.toArray(new String[results.size()]);
	}

	public Dictionary getPropertiesForImportedConfigs(ContainerTypeDescription description, String[] importedConfigs, Dictionary exportedProperties) {
		return null;
	}

	public abstract IContainer createInstance(ContainerTypeDescription description, Object[] parameters) throws ContainerCreateException;
}
