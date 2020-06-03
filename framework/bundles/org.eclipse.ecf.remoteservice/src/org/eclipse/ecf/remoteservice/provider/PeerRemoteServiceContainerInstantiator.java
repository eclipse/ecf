/****************************************************************************
 * Copyright (c) 2016 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
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
