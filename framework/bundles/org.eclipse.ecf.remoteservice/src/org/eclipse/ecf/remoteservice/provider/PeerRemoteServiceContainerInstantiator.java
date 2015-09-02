package org.eclipse.ecf.remoteservice.provider;

import java.util.*;

/**
 * @since 8.7
 */
public abstract class PeerRemoteServiceContainerInstantiator extends RemoteServiceContainerInstantiator {

	public PeerRemoteServiceContainerInstantiator(String peerA, String peerB) {
		this.exporterConfigs = new ArrayList<String>();
		this.exporterConfigs.add(peerA);
		this.exporterConfigs.add(peerB);
		this.exporterConfigToImporterConfigs = new HashMap<String, List<String>>();
		this.exporterConfigToImporterConfigs.put(peerA, Arrays.asList(new String[] {peerA, peerB}));
		this.exporterConfigToImporterConfigs.put(peerB, Arrays.asList(new String[] {peerA, peerB}));
	}

}
