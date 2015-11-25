package org.eclipse.ecf.remoteserviceadmin.ui.service.model;

public class UsingBundleIdsNode extends AbstractServicesNode {

	private String usingBundlesName;

	public UsingBundleIdsNode(String name, long[] usingBundleIds) {
		this.usingBundlesName = name;
		for (int i = 0; i < usingBundleIds.length; i++)
			addChild(new BundleIdNode(usingBundleIds[i]));
	}

	public String getUsingBundleIdsName() {
		return this.usingBundlesName;
	}

}
