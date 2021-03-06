/****************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.provider.filetransfer.retrieve;

import org.eclipse.ecf.core.AbstractContainerAdapterFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.filetransfer.IRetrieveFileTransferContainerAdapter;

/**
 * 
 */
public class MultiProtocolRetrieveAdapterFactory extends AbstractContainerAdapterFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.sharedobject.AbstractSharedObjectContainerAdapterFactory#getAdapterList()
	 */
	public Class<?>[] getAdapterList() {
		return new Class[] {IRetrieveFileTransferContainerAdapter.class};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.AbstractContainerAdapterFactory#getContainerAdapter(org.eclipse.ecf.core.IContainer,
	 *      java.lang.Class)
	 */
	protected Object getContainerAdapter(IContainer container, Class adapterType) {
		if (adapterType.equals(IRetrieveFileTransferContainerAdapter.class)) {
			return new MultiProtocolRetrieveAdapter();
		}
		return null;
	}

}
