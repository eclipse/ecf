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

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.sync.IModelSynchronizationStrategy;
import org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategyFactory;

/**
 *
 */
public class IdentitySynchronizationStrategyFactory implements IDocumentSynchronizationStrategyFactory {

	public static final String SYNCHSTRATEGY_PROVIDER = "org.eclipse.ecf.sync.doc.identity";

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategyFactory#disposeSynchronizationStragety(org.eclipse.ecf.core.identity.ID)
	 */
	public void disposeSynchronizationStrategy(ID uniqueID) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategyFactory#getSyncronizationStrategy(org.eclipse.ecf.core.identity.ID, boolean)
	 */
	public IModelSynchronizationStrategy createDocumentSynchronizationStrategy(ID uniqueID, boolean isInitiator) {
		return new IdentitySynchronizationStrategy();
	}

	public void dispose() {
		
	}
}
