/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
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

package org.eclipse.ecf.internal.sync.doc.identity;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.ecf.internal.sync.Activator;
import org.eclipse.ecf.sync.IModelChange;
import org.eclipse.ecf.sync.IModelChangeMessage;
import org.eclipse.ecf.sync.IModelSynchronizationStrategy;
import org.eclipse.ecf.sync.SerializationException;
import org.eclipse.ecf.sync.doc.DocumentChangeMessage;
import org.eclipse.ecf.sync.doc.IDocumentChange;

/**
 *
 */
public class IdentitySynchronizationStrategy implements IModelSynchronizationStrategy {

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategy#deserializeToDocumentChange(byte[])
	 */
	public IModelChange deserializeRemoteChange(byte[] bytes) throws SerializationException {
		return DocumentChangeMessage.deserialize(bytes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategy#registerLocalChange(org.eclipse.ecf.sync.doc.IDocumentChange)
	 */
	public IModelChangeMessage[] registerLocalChange(IModelChange localChange) {
		if (localChange instanceof IDocumentChange) {
			IDocumentChange docChange = (IDocumentChange) localChange;
			return new IModelChangeMessage[] {new DocumentChangeMessage(docChange.getOffset(), docChange.getLengthOfReplacedText(), docChange.getText())};
		} else return new IModelChangeMessage[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategy#transformRemoteChange(org.eclipse.ecf.sync.IModelChange)
	 */
	public IModelChange[] transformRemoteChange(IModelChange remoteChange) {
		return new IModelChange[] {remoteChange};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == null) return null;
		IAdapterManager manager = Activator.getDefault().getAdapterManager();
		if (manager == null) return null;
		return manager.loadAdapter(this, adapter.getName());
	}

}
