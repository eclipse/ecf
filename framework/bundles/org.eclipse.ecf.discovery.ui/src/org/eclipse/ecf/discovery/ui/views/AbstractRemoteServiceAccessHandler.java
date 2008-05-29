package org.eclipse.ecf.discovery.ui.views;

import java.util.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.AdapterContainerFilter;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.internal.discovery.ui.Activator;
import org.eclipse.ecf.internal.discovery.ui.Messages;
import org.eclipse.ecf.remoteservice.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.InvalidSyntaxException;

public abstract class AbstractRemoteServiceAccessHandler implements IServiceAccessHandler {

	protected static final IContributionItem[] EMPTY_CONTRIBUTION = {};

	protected static IContributionItem[] NOT_AVAILABLE_CONTRIBUTION;

	protected final AdapterContainerFilter containerFilter;

	protected IServiceInfo serviceInfo;

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

	protected IServiceInfo getServiceInfo() {
		return serviceInfo;
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

	protected boolean matchServiceType(String service) {
		return Arrays.asList(this.serviceInfo.getServiceID().getServiceTypeID().getServices()).contains(service);
	}

	protected List getRemoteServiceContainerAdapters() {
		final List remoteServicesContainerAdapters = new ArrayList();
		final IContainerManager containerManager = getContainerManager();
		if (containerManager == null)
			return remoteServicesContainerAdapters;
		final IContainer[] containers = containerManager.getAllContainers();
		for (int i = 0; i < containers.length; i++) {
			if (containerFilter.match(containers[i]) && matchTargetNamespace(containers[i], getConnectNamespace())) {
				remoteServicesContainerAdapters.add(containerFilter.getMatchResult());
			}
		}
		return remoteServicesContainerAdapters;
	}

	protected String getContainerFactory() {
		return this.serviceInfo.getServiceProperties().getPropertyString(Constants.DISCOVERY_CONTAINER_FACTORY_PROPERTY);
	}

	protected String getRemoteServiceClass() {
		return this.serviceInfo.getServiceProperties().getPropertyString(Constants.DISCOVERY_OBJECTCLASS_PROPERTY);
	}

	protected String getConnectNamespace() {
		return this.serviceInfo.getServiceProperties().getPropertyString(Constants.DISCOVERY_CONNECT_ID_NAMESPACE_PROPERTY);
	}

	protected String getConnectID() {
		return this.serviceInfo.getServiceProperties().getPropertyString(Constants.DISCOVERY_CONNECT_ID_PROPERTY);
	}

	protected String getServiceNamespace() {
		return this.serviceInfo.getServiceProperties().getPropertyString(Constants.DISCOVERY_SERVICE_ID_NAMESPACE_PROPERTY);
	}

	protected String getServiceID() {
		return this.serviceInfo.getServiceProperties().getPropertyString(Constants.DISCOVERY_SERVICE_ID_PROPERTY);
	}

	protected String getFilter() {
		return this.serviceInfo.getServiceProperties().getPropertyString(Constants.DISCOVERY_FILTER_PROPERTY);
	}

	protected ID createID(String namespace, String value) throws IDCreateException {
		return IDFactory.getDefault().createID(namespace, value);
	}

	protected ID createConnectID() throws IDCreateException {
		return createID(getConnectNamespace(), getConnectID());
	}

	protected abstract IContributionItem[] getContributionsForMatchingService();

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.ui.views.IServiceAccessHandler#getContributionsForService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public IContributionItem[] getContributionsForService(IServiceInfo svcInfo) {
		if (svcInfo == null)
			return EMPTY_CONTRIBUTION;
		this.serviceInfo = svcInfo;
		if (matchServiceType(Constants.DISCOVERY_SERVICE_TYPE))
			return getContributionsForMatchingService();
		return EMPTY_CONTRIBUTION;
	}

	protected IRemoteServiceReference[] getRemoteServiceReferencesForRemoteServiceAdapter(IRemoteServiceContainerAdapter adapter) throws InvalidSyntaxException, IDCreateException {
		ID serviceID = null;
		String serviceNamespace = getServiceNamespace();
		String serviceid = getServiceID();
		if (serviceNamespace != null && serviceid != null) {
			serviceID = createID(serviceNamespace, serviceid);
		}
		ID[] targets = (serviceID == null) ? null : new ID[] {serviceID};
		return adapter.getRemoteServiceReferences(targets, getRemoteServiceClass(), getFilter());
	}

	protected IContainer createContainer() throws ContainerCreateException {
		return ContainerFactory.getDefault().createContainer(getContainerFactory());
	}

	protected void connectContainer(IContainer container, ID connectTargetID, IConnectContext connectContext) throws ContainerConnectException {
		Assert.isNotNull(container);
		Assert.isNotNull(connectTargetID);
		container.connect(connectTargetID, connectContext);
	}

	protected abstract IContributionItem[] getContributionItemsForRemoteServiceAdapter(final IRemoteServiceContainerAdapter adapter);

	protected IContributionItem[] getContributionItemsForConnectedContainer(final IContainer container, final IRemoteServiceContainerAdapter adapter) {
		// Add disconnect and separator
		final List results = new ArrayList();
		final IContributionItem[] serviceItem = getContributionItemsForRemoteServiceAdapter(adapter);
		if (serviceItem != null) {
			for (int i = 0; i < serviceItem.length; i++)
				results.add(serviceItem[i]);
		} else
			return EMPTY_CONTRIBUTION;
		results.add(new Separator());
		final IAction disconnectAction = new Action() {
			public void run() {
				container.disconnect();
			}
		};
		disconnectAction.setText(Messages.AbstractRemoteServiceAccessHandler_DISCONNECT_MENU_TEXT);
		results.add(new ActionContributionItem(disconnectAction));
		return (IContributionItem[]) results.toArray(new IContributionItem[] {});
	}

	protected void showResult(final String serviceInterface, final IRemoteCall remoteCall, final Object result) {
		final Object display = (result != null && result.getClass().isArray()) ? Arrays.asList((Object[]) result) : result;
		final Object[] bindings = new Object[] {serviceInterface, remoteCall.getMethod(), Arrays.asList(remoteCall.getParameters()), display};
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(null, Messages.AbstractRemoteServiceAccessHandler_MSG_BOX_RECEIVED_RESP_TITLE, NLS.bind(Messages.AbstractRemoteServiceAccessHandler_MSG_BOX_RECEIVED_RESP_TEXT, bindings));
			}
		});
	}

	protected void showException(final Throwable t) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(null, Messages.AbstractRemoteServiceAccessHandler_MSG_BOX_RECEIVED_EXCEPTION_TITLE, NLS.bind(Messages.AbstractRemoteServiceAccessHandler_MSG_BOX_RECEIVED_EXCEPTION_TEXT, t.getLocalizedMessage()));
			}
		});
	}

	protected void showInformation(final String title, final String message) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(null, title, message);
			}
		});
	}

}
