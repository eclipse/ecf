/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.datashare.multicast;

import java.util.TimerTask;

class Timeout extends TimerTask {

	interface Listener {
		void timeout(Version version);
	}

	private final Listener listener;

	final Version version;

	public Timeout(Listener listener, Version version) {
		this.listener = listener;
		this.version = version;
	}

	public Version getVersion() {
		return version;
	}

	public void run() {
		listener.timeout(version);
	}
}