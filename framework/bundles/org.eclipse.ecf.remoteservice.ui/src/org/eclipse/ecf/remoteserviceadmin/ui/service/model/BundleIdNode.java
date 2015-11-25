package org.eclipse.ecf.remoteserviceadmin.ui.service.model;

public class BundleIdNode extends AbstractServicesNode {

	private final long bundleId;

	public BundleIdNode(long bundleId) {
		this.bundleId = bundleId;
	}

	public long getBundleId() {
		return this.bundleId;
	}
}
