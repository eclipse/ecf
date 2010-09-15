/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.internal.server.generic;

import java.util.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.server.generic.*;

public class GenericServerContainerGroupFactory implements IGenericServerContainerGroupFactory {

	class SCGData {
		private String hostname;
		private int port;

		SCGData(String hostname, int port) {
			Assert.isNotNull(hostname);
			Assert.isTrue(port > 0);
			this.hostname = hostname;
			this.port = port;
		}

		public String getHostname() {
			return hostname;
		}

		public int getPort() {
			return port;
		}

		public boolean equals(Object other) {
			if (!(other instanceof SCGData))
				return false;
			SCGData o = (SCGData) other;
			if (this.hostname.equals(o.hostname) && this.port == o.port)
				return true;
			return false;
		}

		public int hashCode() {
			return this.hostname.hashCode() ^ this.port;
		}
	}

	private Hashtable serverContainerGroups = new Hashtable();

	public IGenericServerContainerGroup createContainerGroup(String hostname, int port, Map defaultContainerProperties) throws GenericServerContainerGroupCreateException {
		synchronized (serverContainerGroups) {
			SCGData scgdata = new SCGData(hostname, port);
			if (serverContainerGroups.contains(scgdata))
				throw new GenericServerContainerGroupCreateException("Cannot container group hostname=" + hostname + " port=" + port + " already exists"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			IGenericServerContainerGroup scg = createGenericServerContainerGroup(scgdata, defaultContainerProperties);
			serverContainerGroups.put(scgdata, scg);
			return scg;
		}
	}

	/**
	 * @throws GenericServerContainerGroupCreateException  
	 */
	protected IGenericServerContainerGroup createGenericServerContainerGroup(SCGData scgdata, Map defaultContainerProperties) throws GenericServerContainerGroupCreateException {
		return new GenericServerContainerGroup(scgdata.getHostname(), scgdata.getPort(), defaultContainerProperties);
	}

	public IGenericServerContainerGroup createContainerGroup(String hostname, int port) throws GenericServerContainerGroupCreateException {
		return createContainerGroup(hostname, port, null);
	}

	public IGenericServerContainerGroup createContainerGroup(String hostname) throws GenericServerContainerGroupCreateException {
		return createContainerGroup(hostname, DEFAULT_PORT);
	}

	public void close() {
		synchronized (serverContainerGroups) {
			for (Iterator i = serverContainerGroups.keySet().iterator(); i.hasNext();) {
				SCGData scgdata = (SCGData) i.next();
				IGenericServerContainerGroup scg = (IGenericServerContainerGroup) serverContainerGroups.get(scgdata);
				// call close
				scg.close();
			}
		}
		serverContainerGroups.clear();
	}
}
