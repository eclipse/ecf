/****************************************************************************
 * Copyright (c)2010 REMAIN B.V. The Netherlands. (http://www.remainsoftware.com).
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 *  Contributors:
 *    Wim Jongman - initial API and implementation 
 *    Ahmed Aadel - initial API and implementation     
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.zookeeper.core.internal;

import org.eclipse.ecf.provider.zookeeper.node.internal.WatchManager;

public class Advertiser {

	private static Advertiser singleton;
	private WatchManager watcher;

	private Advertiser(WatchManager watcher) {
		this.watcher = watcher;
		singleton = this;
	}

	public static Advertiser getSingleton(WatchManager watcher) {
		if (singleton == null)
			new Advertiser(watcher);
		return singleton;
	}

	public WatchManager getWather() {
		return this.watcher;
	}
}
