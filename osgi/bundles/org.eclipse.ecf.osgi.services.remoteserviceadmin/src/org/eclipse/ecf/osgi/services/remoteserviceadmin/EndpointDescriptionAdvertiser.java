/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.jobs.JobsExecutor;
import org.eclipse.ecf.discovery.IDiscoveryAdvertiser;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.Activator;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;
import org.eclipse.equinox.concurrent.future.IExecutor;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.ImmediateExecutor;
import org.eclipse.equinox.concurrent.future.ThreadsExecutor;

/**
 * Default implementation of {@link IEndpointDescriptionAdvertiser}.
 * 
 */
public class EndpointDescriptionAdvertiser implements
		IEndpointDescriptionAdvertiser {

	private static final String advertiserFilter = System
			.getProperty("org.eclipse.ecf.osgi.services.remoteserviceadmin.endpointDescriptionDiscoveryAdvertiserFilter"); //$NON-NLS-1$

	private static final String executorType = System
			.getProperty(
					"org.eclipse.ecf.osgi.services.remoteserviceadmin.endpointDescriptionDiscoveryAdvertiserExecutorType", "immediate"); //$NON-NLS-1$ //$NON-NLS-2$

	private EndpointDescriptionLocator endpointDescriptionLocator;
	private List<String> filteredAdvertisers = new ArrayList<String>();

	public EndpointDescriptionAdvertiser(
			EndpointDescriptionLocator endpointDescriptionLocator) {
		this.endpointDescriptionLocator = endpointDescriptionLocator;
		if (advertiserFilter != null) {
			try {
				String[] fas = advertiserFilter.split(","); //$NON-NLS-1$
				if (fas != null)
					for (int i = 0; i < fas.length; i++)
						filteredAdvertisers.add(fas[i]);
			} catch (PatternSyntaxException e) {
				// should never happen
			}
		}
		for (String filteredAdvertiser : filteredAdvertisers)
			trace("EndpointDescriptionAdvertiser<init>", //$NON-NLS-1$
					"filtering advertiser=" + filteredAdvertiser); //$NON-NLS-1$
	}

	private IExecutor createExecutor() {
		if (executorType.equals("jobs")) { //$NON-NLS-1$
			return new JobsExecutor(this.getClass().getName());
		} else if (executorType.equals("threads")) { //$NON-NLS-1$
			return new ThreadsExecutor();
		} else
			return new ImmediateExecutor();
	}

	private IExecutor executor;

	private IExecutor getExecutor() {
		synchronized (this) {
			if (executor == null)
				executor = createExecutor();
			return executor;
		}
	}

	/**
	 * @since 3.0
	 */
	public IStatus advertise(EndpointDescription endpointDescription) {
		return doDiscovery(endpointDescription, true);
	}

	/**
	 * @since 3.0
	 */
	protected void trace(String methodName, String message) {
		LogUtility.trace(methodName,
				DebugOptions.ENDPOINT_DESCRIPTION_ADVERTISER, this.getClass(),
				message);
	}

	/**
	 * @since 3.0
	 */
	protected IStatus doDiscovery(
			final IDiscoveryAdvertiser discoveryAdvertiser,
			final IServiceInfo serviceInfo, final boolean advertise) {
		IProgressRunnable<IStatus> runnable = new IProgressRunnable<IStatus>() {
			public IStatus run(IProgressMonitor monitor) throws Exception {
				try {
					if (advertise) {
						trace("doDiscovery", "discoveryAdvertiser=" + discoveryAdvertiser + " serviceInfo=" + serviceInfo); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						synchronized (discoveryAdvertiser) {
							discoveryAdvertiser.registerService(serviceInfo);
						}
					} else {
						trace("doUndiscovery", "discoveryAdvertiser=" + discoveryAdvertiser + " serviceInfo=" + serviceInfo); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						synchronized (discoveryAdvertiser) {
							discoveryAdvertiser.unregisterService(serviceInfo);
						}
					}
				} catch (Exception e) {
					LogUtility
							.logError(
									"doDiscovery", DebugOptions.ENDPOINT_DESCRIPTION_ADVERTISER, this.getClass(), createErrorStatus("Unexplained exception in discoveryAdvertiserID=" + discoveryAdvertiser + " for serviceInfo=" + serviceInfo + " advertise=" + true)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				return null;
			}
		};
		// Start/do it
		getExecutor().execute(runnable, null);
		return Status.OK_STATUS;
	}

	protected IServiceInfoFactory getServiceInfoFactory() {
		return endpointDescriptionLocator.getServiceInfoFactory();
	}

	protected IDiscoveryAdvertiser[] getDiscoveryAdvertisers() {
		IDiscoveryAdvertiser[] unfilteredAdvertisers = endpointDescriptionLocator
				.getDiscoveryAdvertisers();
		List<IDiscoveryAdvertiser> results = new ArrayList<IDiscoveryAdvertiser>();
		if (unfilteredAdvertisers != null)
			for (int i = 0; i < unfilteredAdvertisers.length; i++) {
				String className = unfilteredAdvertisers[i].getClass()
						.getName();
				if (!filteredAdvertisers.contains(className))
					results.add(unfilteredAdvertisers[i]);
				else
					LogUtility
							.logWarning(
									"getDiscoveryAdvertisers", DebugOptions.ENDPOINT_DESCRIPTION_ADVERTISER, this.getClass(), "Filtering out discoveryAdvertiser=" + className); //$NON-NLS-1$ //$NON-NLS-2$
			}
		return results.toArray(new IDiscoveryAdvertiser[results.size()]);
	}

	protected IStatus createErrorStatus(String message) {
		return createErrorStatus(message, null);
	}

	protected IStatus createErrorStatus(String message, Throwable e) {
		return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, e);
	}

	/**
	 * @since 3.0
	 */
	protected IStatus doDiscovery(EndpointDescription endpointDescription,
			boolean advertise) {
		Assert.isNotNull(endpointDescription);
		String messagePrefix = advertise ? "Advertise" : "Unadvertise"; //$NON-NLS-1$ //$NON-NLS-2$
		List<IStatus> statuses = new ArrayList<IStatus>();
		// First get serviceInfoFactory
		IServiceInfoFactory serviceInfoFactory = getServiceInfoFactory();
		if (serviceInfoFactory == null)
			return createErrorStatus(messagePrefix
					+ " endpointDescription=" //$NON-NLS-1$
					+ endpointDescription
					+ ".  No IServiceInfoFactory is available.  Cannot unpublish endpointDescription=" //$NON-NLS-1$
					+ endpointDescription);
		IDiscoveryAdvertiser[] discoveryAdvertisers = getDiscoveryAdvertisers();
		if (discoveryAdvertisers == null || discoveryAdvertisers.length == 0)
			return createErrorStatus(messagePrefix
					+ " endpointDescription=" //$NON-NLS-1$
					+ endpointDescription
					+ ".  No endpointDescriptionLocator advertisers available.  Cannot " //$NON-NLS-1$
					+ (advertise ? "publish" : "unpublish") //$NON-NLS-1$ //$NON-NLS-2$
					+ " endpointDescription=" //$NON-NLS-1$ 
					+ endpointDescription);
		for (int i = 0; i < discoveryAdvertisers.length; i++) {
			IServiceInfo serviceInfo = (advertise ? serviceInfoFactory
					.createServiceInfo(discoveryAdvertisers[i],
							endpointDescription) : serviceInfoFactory
					.removeServiceInfo(discoveryAdvertisers[i],
							endpointDescription));
			if (serviceInfo == null) {
				statuses.add(createErrorStatus(messagePrefix
						+ " endpointDescription=" //$NON-NLS-1$
						+ endpointDescription
						+ ".  Service Info is null.  Cannot publish endpointDescription=" //$NON-NLS-1$
						+ endpointDescription));
				continue;
			}
			// Now actually unregister with advertiser
			statuses.add(doDiscovery(discoveryAdvertisers[i], serviceInfo,
					advertise));
		}
		return createResultStatus(statuses, messagePrefix
				+ " endpointDesription=" + endpointDescription //$NON-NLS-1$
				+ ".  Problem in unadvertise"); //$NON-NLS-1$
	}

	/**
	 * @since 3.0
	 */
	public IStatus unadvertise(EndpointDescription endpointDescription) {
		return doDiscovery(endpointDescription, false);
	}

	private IStatus createResultStatus(List<IStatus> statuses,
			String errorMessage) {
		List<IStatus> errorStatuses = new ArrayList<IStatus>();
		for (IStatus status : statuses)
			if (!status.isOK())
				errorStatuses.add(status);
		if (errorStatuses.size() > 0)
			return new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR,
					(IStatus[]) statuses.toArray(new IStatus[statuses.size()]),
					errorMessage, null);
		else
			return Status.OK_STATUS;
	}

	public void close() {
		this.filteredAdvertisers.clear();
		this.endpointDescriptionLocator = null;
		executor = null;
	}

	public IStatus advertise(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		return doDiscovery(endpointDescription, true);
	}

	protected IStatus doDiscovery(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription,
			boolean advertise) {
		Assert.isNotNull(endpointDescription);
		String messagePrefix = advertise ? "Advertise" : "Unadvertise"; //$NON-NLS-1$ //$NON-NLS-2$
		List<IStatus> statuses = new ArrayList<IStatus>();
		// First get serviceInfoFactory
		IServiceInfoFactory serviceInfoFactory = getServiceInfoFactory();
		if (serviceInfoFactory == null)
			return createErrorStatus(messagePrefix
					+ " endpointDescription=" //$NON-NLS-1$
					+ endpointDescription
					+ ".  No IServiceInfoFactory is available.  Cannot unpublish endpointDescription=" //$NON-NLS-1$
					+ endpointDescription);
		IDiscoveryAdvertiser[] discoveryAdvertisers = getDiscoveryAdvertisers();
		if (discoveryAdvertisers == null || discoveryAdvertisers.length == 0)
			return createErrorStatus(messagePrefix
					+ " endpointDescription=" //$NON-NLS-1$
					+ endpointDescription
					+ ".  No endpointDescriptionLocator advertisers available.  Cannot " //$NON-NLS-1$
					+ (advertise ? "publish" : "unpublish") //$NON-NLS-1$ //$NON-NLS-2$
					+ " endpointDescription=" //$NON-NLS-1$ 
					+ endpointDescription);
		for (int i = 0; i < discoveryAdvertisers.length; i++) {
			IServiceInfo serviceInfo = (advertise ? serviceInfoFactory
					.createServiceInfo(discoveryAdvertisers[i],
							endpointDescription) : serviceInfoFactory
					.removeServiceInfo(discoveryAdvertisers[i],
							endpointDescription));
			if (serviceInfo == null) {
				statuses.add(createErrorStatus(messagePrefix
						+ " endpointDescription=" //$NON-NLS-1$
						+ endpointDescription
						+ ".  Service Info is null.  Cannot publish endpointDescription=" //$NON-NLS-1$
						+ endpointDescription));
				continue;
			}
			// Now actually unregister with advertiser
			statuses.add(doDiscovery(discoveryAdvertisers[i], serviceInfo,
					advertise));
		}
		return createResultStatus(statuses, messagePrefix
				+ " endpointDesription=" + endpointDescription //$NON-NLS-1$
				+ ".  Problem in unadvertise"); //$NON-NLS-1$
	}

	public IStatus unadvertise(
			org.osgi.service.remoteserviceadmin.EndpointDescription endpointDescription) {
		return doDiscovery(endpointDescription, false);
	}

}
