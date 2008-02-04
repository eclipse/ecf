package org.eclipse.ecf.discovery.ui.handlers;

import java.util.*;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.AdapterContainerFilter;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.IServiceProperties;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;
import org.eclipse.ecf.discovery.ui.views.IServiceAccessHandler;
import org.eclipse.ecf.internal.discovery.ui.Activator;
import org.eclipse.ecf.internal.discovery.ui.Messages;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.jface.action.*;

public abstract class AbstractRemoteServiceAccessHandler implements IServiceAccessHandler {

	protected static final IContributionItem[] EMPTY_CONTRIBUTION = {};

	protected static IContributionItem[] NOT_AVAILABLE_CONTRIBUTION;

	protected final AdapterContainerFilter containerFilter;

	public AbstractRemoteServiceAccessHandler() {
		containerFilter = new AdapterContainerFilter(IRemoteServiceContainerAdapter.class);
		final IAction containerNotAvailableAction = new Action() {
			public void run() {
				// Do nothing
			}
		};
		containerNotAvailableAction.setText(Messages.AbstractRemoteServiceAccessHandler_NOT_AVAILABLE_MENU_TEXT);
		containerNotAvailableAction.setEnabled(false);
		NOT_AVAILABLE_CONTRIBUTION = new IContributionItem[] {new ActionContributionItem(containerNotAvailableAction)};
	}

	private IContainerManager getContainerManager() {
		return Activator.getDefault().getContainerManager();
	}

	protected boolean isConnected(IContainer container) {
		if (container == null)
			return false;
		return (container.getConnectedID() == null);
	}

	protected boolean matchTargetNamespace(IContainer container, String targetNamespace) {
		final Namespace containerNamespace = container.getConnectNamespace();
		if (containerNamespace == null && targetNamespace == null)
			return true;
		return (containerNamespace.getName().equals(targetNamespace));
	}

	protected boolean matchServiceType(IServiceTypeID serviceTypeID, String service) {
		final List serviceTypes = Arrays.asList(serviceTypeID.getServices());
		return serviceTypes.contains(service);
	}

	protected List getRemoteServiceContainerAdapters(IServiceInfo serviceInfo, String targetNamespace) {
		final List remoteServicesContainerAdapters = new ArrayList();
		final IContainerManager containerManager = getContainerManager();
		if (containerManager == null)
			return remoteServicesContainerAdapters;
		final IContainer[] containers = containerManager.getAllContainers();
		for (int i = 0; i < containers.length; i++) {
			if (containerFilter.match(containers[i]) && isConnected(containers[i]) && matchTargetNamespace(containers[i], targetNamespace)) {
				remoteServicesContainerAdapters.add(containerFilter.getMatchResult());
			}
		}
		return remoteServicesContainerAdapters;
	}

	public IContributionItem[] getContributionsForService(IServiceInfo serviceInfo) {
		if (matchServiceType(serviceInfo.getServiceID().getServiceTypeID(), Constants.DISCOVERY_SERVICE_TYPE)) {
			// First get container manager...if we don't have one, then we're outta here
			final IServiceProperties serviceProperties = serviceInfo.getServiceProperties();
			final String targetNamespace = serviceProperties.getPropertyString(Constants.DISCOVERY_TARGET_ID_NAMESPACE_PROPERTY);
			final String clazz = serviceProperties.getPropertyString(Constants.DISCOVERY_OBJECTCLASS_PROPERTY);
			if (targetNamespace == null || clazz == null)
				return EMPTY_CONTRIBUTION;
			final List remoteServicesContainerAdapters = getRemoteServiceContainerAdapters(serviceInfo, targetNamespace);
			// If we've got none, then we return 
			if (remoteServicesContainerAdapters.size() == 0)
				return NOT_AVAILABLE_CONTRIBUTION;
			final String filter = serviceProperties.getPropertyString(Constants.DISCOVERY_FILTER_PROPERTY);
			final String target = serviceProperties.getPropertyString(Constants.DISCOVERY_TARGET_ID_PROPERTY);
			// If we've got one, then we do our thing
			final List contributions = new ArrayList();
			for (final Iterator i = remoteServicesContainerAdapters.iterator(); i.hasNext();) {
				contributions.add(getContributionsForService((IRemoteServiceContainerAdapter) i.next(), targetNamespace, target, clazz, filter));
			}
			return (IContributionItem[]) contributions.toArray(new IContributionItem[] {});
		}
		return EMPTY_CONTRIBUTION;
	}

	/**
	 * @param adapter the IRemoteServiceContainerAdapter to use to lookup the {@link IRemoteServiceReference}.  Will not be <code>null</code>.
	 * @param targetNamespace the target namespace for the idFilter parameter for the call to {@link IRemoteServiceContainerAdapter#getRemoteServiceReferences(org.eclipse.ecf.core.identity.ID[], String, String)}.  Will not be <code>null</code>.
	 * @param target the target id for the idFilter parameter for the call to {@link IRemoteServiceContainerAdapter#getRemoteServiceReferences(org.eclipse.ecf.core.identity.ID[], String, String)}.  May be <code>null</code>.
	 * @param clazz the clazz parameter for the call to {@link IRemoteServiceContainerAdapter#getRemoteServiceReferences(org.eclipse.ecf.core.identity.ID[], String, String)}.  Will not be <code>null</code>.
	 * @param filter the filter parameter for the call to {@link IRemoteServiceContainerAdapter#getRemoteServiceReferences(org.eclipse.ecf.core.identity.ID[], String, String)}.  May be <code>null</code>.
	 * @return IContributionItem the menu contribution item to be added to the menu.  May be <code>null</code>.  If <code>null</code> then no item is added to the
	 * menu.
	 */
	protected abstract IContributionItem getContributionsForService(final IRemoteServiceContainerAdapter adapter, final String targetNamespace, final String target, final String clazz, final String filter);

}
