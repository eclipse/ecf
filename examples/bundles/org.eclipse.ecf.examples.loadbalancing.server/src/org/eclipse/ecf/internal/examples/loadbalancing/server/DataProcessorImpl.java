/****************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.examples.loadbalancing.server;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.examples.loadbalancing.IDataProcessor;

public class DataProcessorImpl implements IDataProcessor {

	private ID containerID;

	public DataProcessorImpl(ID containerID) {
		this.containerID = containerID;
	}

	/**
	 * Entry point for IDataProcessor service implementation
	 */
	public String processData(String data) {
		System.out.println("DataProcessorImpl(" + containerID.getName()
				+ ").processData data=" + data);
		if (data == null) return null;
		return reverseString(data);
	}

	private String reverseString(String data) {
		StringBuffer buf = new StringBuffer(data);
		buf.reverse();
		return buf.toString();
	}

	public void stop() {
	}

}
