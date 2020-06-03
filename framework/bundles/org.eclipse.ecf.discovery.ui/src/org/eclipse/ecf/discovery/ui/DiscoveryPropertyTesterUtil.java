/****************************************************************************
 * Copyright (c) 2008 Versant Corp.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Markus Kuppe (mkuppe <at> versant <dot> com) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.discovery.ui;

import org.eclipse.ecf.discovery.IServiceInfo;

/**
 * @since 3.0
 */
public class DiscoveryPropertyTesterUtil {

	public static IServiceInfo getIServiceInfoReceiver(Object receiver) {
		if(receiver instanceof org.eclipse.ecf.discovery.ui.model.IServiceInfo) {
			org.eclipse.ecf.discovery.ui.model.IServiceInfo isi = (org.eclipse.ecf.discovery.ui.model.IServiceInfo) receiver;
			return isi.getEcfServiceInfo();
		}
		return null;
	}

}
