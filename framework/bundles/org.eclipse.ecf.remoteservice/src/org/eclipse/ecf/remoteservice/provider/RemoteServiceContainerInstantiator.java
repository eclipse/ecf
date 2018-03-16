/*******************************************************************************
 * Copyright (c) 2016 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservice.provider;

import java.net.URI;
import java.util.*;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.provider.*;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.util.EndpointDescriptionPropertiesUtil;

/**
 * @since 8.7
 */
public abstract class RemoteServiceContainerInstantiator extends BaseContainerInstantiator implements IRemoteServiceContainerInstantiator {

	protected static final String[] defaultSupportedAdapterTypes = new String[] {IContainer.class.getName(), IRemoteServiceContainerAdapter.class.getName()};
	protected static final Class[][] defaultSupportedParameterTypes = new Class[][] {{Map.class}};

	protected static final String[] defaultSupportedIntents = new String[] {Constants.OSGI_BASIC_INTENT, "passByValue", "exactlyOnce", "ordered"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public String[] getSupportedAdapterTypes(ContainerTypeDescription description) {
		return defaultSupportedAdapterTypes;
	}

	public Class[][] getSupportedParameterTypes(ContainerTypeDescription description) {
		return defaultSupportedParameterTypes;
	}

	protected List<String> exporterConfigs;
	protected Map<String, List<String>> exporterConfigToImporterConfigs;

	/**
	 * @param exportingProvider exporting provider (e.g. server or service host)
	 * @param importingProvider importing provider (e.g. client or service client)
	 * @since 8.9
	 */
	protected RemoteServiceContainerInstantiator(String exportingProvider, String importingProvider) {
		this();
		this.exporterConfigs.add(exportingProvider);
		this.exporterConfigToImporterConfigs.put(exportingProvider, Arrays.asList(new String[] {importingProvider}));
	}

	protected RemoteServiceContainerInstantiator(List<String> exporterConfigs, Map<String, List<String>> exporterConfigToImporterConfig) {
		this();
		this.exporterConfigs.addAll(exporterConfigs);
		this.exporterConfigToImporterConfigs.putAll(exporterConfigToImporterConfig);
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

	public abstract IContainer createInstance(ContainerTypeDescription description, Map<String, ?> parameters) throws ContainerCreateException;

	public IContainer createInstance(ContainerTypeDescription description, Object[] parameters) throws ContainerCreateException {
		return createInstance(description, getMap(parameters));
	}

	protected IContainer throwCreateException(String message, Throwable cause) throws ContainerCreateException {
		ContainerCreateException cce = new ContainerCreateException(message, cause);
		cce.setStackTrace(cause.getStackTrace());
		throw cce;
	}

	/**
	 * @since 8.13
	 */
	protected boolean supportsOSGIConfidentialIntent(ContainerTypeDescription description) {
		return false;
	}

	/**
	 * @since 8.13
	 */
	protected boolean supportsOSGIPrivateIntent(ContainerTypeDescription description) {
		return false;
	}

	/**
	 * @since 8.13
	 */
	protected boolean supportsOSGIAsyncIntent(ContainerTypeDescription description) {
		return false;
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		String[] s = defaultSupportedIntents;
		if (supportsOSGIAsyncIntent(description))
			s = addSupportedIntent(Constants.OSGI_ASYNC_INTENT, s);
		if (supportsOSGIPrivateIntent(description))
			s = addSupportedIntent(Constants.OSGI_PRIVATE_INTENT, s);
		if (supportsOSGIConfidentialIntent(description))
			s = addSupportedIntent(Constants.OSGI_CONFIDENTIAL_INTENT, s);
		return s;
	}

	/**
	 * @since 8.13
	 */
	protected static String[] addSupportedIntent(String intent, String[] currentSupportedIntents) {
		if (intent == null)
			return currentSupportedIntents;
		List<String> results = (currentSupportedIntents == null) ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(currentSupportedIntents));
		results.add(intent);
		return results.toArray(new String[results.size()]);
	}

	/**
	 * @since 8.13
	 */
	protected static String[] removeSupportedIntent(String intent, String[] currentSupportedIntents) {
		if (intent == null)
			return currentSupportedIntents;
		List<String> results = (currentSupportedIntents == null) ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(currentSupportedIntents));
		results.remove(intent);
		return results.toArray(new String[results.size()]);
	}

	/**
	 * @since 8.13
	 */
	protected void checkPrivate(ContainerTypeDescription description, String hostname) throws ContainerIntentException {
		ContainerInstantiatorUtils.checkPrivate(hostname);
	}

	/**
	 * @since 8.13
	 */
	protected List<String> getServiceIntents(Map<String, ?> properties) {
		return EndpointDescriptionPropertiesUtil.getStringPlusProperty(properties, Constants.OSGI_SERVICE_INTENTS);
	}

	/**
	 * @since 8.13
	 */
	protected boolean checkIntentSupported(ContainerTypeDescription description, String intent) {
		String[] supportedIntents = getSupportedIntents(description);
		if (supportedIntents != null)
			return Arrays.asList(supportedIntents).contains(intent);
		return false;
	}

	/**
	 * @since 8.13
	 */
	protected boolean checkAsyncIntent(ContainerTypeDescription description, Map<String, ?> properties) throws ContainerIntentException {
		List<String> serviceIntents = getServiceIntents(properties);
		if (serviceIntents.contains(Constants.OSGI_ASYNC_INTENT)) {
			if (!checkIntentSupported(description, Constants.OSGI_ASYNC_INTENT))
				throw new ContainerIntentException(Constants.OSGI_ASYNC_INTENT, "Intent not supported by distribution provider=" + description.getName()); //$NON-NLS-1$
			return true;
		}
		return false;
	}

	/**
	 * @since 8.13
	 */
	protected boolean checkPrivateIntent(ContainerTypeDescription description, String hostname, Map<String, ?> properties) throws ContainerIntentException {
		List<String> serviceIntents = getServiceIntents(properties);
		if (serviceIntents.contains(Constants.OSGI_PRIVATE_INTENT)) {
			if (!checkIntentSupported(description, Constants.OSGI_PRIVATE_INTENT))
				throw new ContainerIntentException(Constants.OSGI_PRIVATE_INTENT, "Not supported by distribution provider=" + description.getName()); //$NON-NLS-1$
			checkPrivate(description, hostname);
		}
		return false;
	}

	/**
	 * @since 8.13
	 */
	protected boolean checkConfidentialIntent(ContainerTypeDescription description, String uri, Map<String, ?> properties) throws ContainerIntentException {
		List<String> serviceIntents = getServiceIntents(properties);
		if (serviceIntents.contains(Constants.OSGI_CONFIDENTIAL_INTENT)) {
			if (!checkIntentSupported(description, Constants.OSGI_CONFIDENTIAL_INTENT))
				throw new ContainerIntentException(Constants.OSGI_CONFIDENTIAL_INTENT, "Intent not supported by distribution provider=" + description.getName()); //$NON-NLS-1$
			checkConfidential(description, uri);
		}
		return false;
	}

	/**
	 * @since 8.13
	 */
	protected void checkConfidential(ContainerTypeDescription description, String uri) throws ContainerIntentException {
		if (uri != null && uri.startsWith("https")) //$NON-NLS-1$
			return;
		throw new ContainerIntentException(Constants.OSGI_CONFIDENTIAL_INTENT, "provider=" + description.getName() + " failed confientiality check for uri=" + uri); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @since 8.13
	 */
	protected void checkOSGIIntents(ContainerTypeDescription description, URI uri, Map<String, ?> properties) throws ContainerIntentException {
		checkAsyncIntent(description, properties);
		checkPrivateIntent(description, uri.getHost(), properties);
		checkConfidentialIntent(description, uri.toString(), properties);
	}
}
