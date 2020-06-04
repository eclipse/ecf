/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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

package org.eclipse.ecf.example.collab.share;

import org.eclipse.ecf.core.identity.ID;

public interface SharedObjectEventListener {

	public void memberRemoved(ID member);

	public void memberAdded(ID member);

	public void otherActivated(ID other);

	public void otherDeactivated(ID other);

	public void windowClosing();

}
