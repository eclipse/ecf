/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/
package org.eclipse.ecf.provider.generic.sobject;

import org.eclipse.ecf.core.ISharedObjectContainerTransaction;
import org.eclipse.ecf.core.SharedObjectAddAbortException;

/**
 * @author slewis
 *
 */
public class ReplicateSharedObjectTransaction implements
		ISharedObjectContainerTransaction {

	BaseSharedObject sharedObject = null;
	
	public ReplicateSharedObjectTransaction(BaseSharedObject so) {
		sharedObject = so;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerTransaction#waitToCommit()
	 */
	public void waitToCommit() throws SharedObjectAddAbortException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.ISharedObjectContainerTransaction#getTransactionState()
	 */
	public byte getTransactionState() {
		// TODO Auto-generated method stub
		return 0;
	}

}
