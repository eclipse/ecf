/****************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.datashare.nio;

final class ChannelData {

	private final byte[] data;
	private final boolean open;

	ChannelData(byte[] data, boolean open) {
		this.data = data;
		this.open = open;
	}

	public byte[] getData() {
		return data;
	}

	public boolean isOpen() {
		return open;
	}

}