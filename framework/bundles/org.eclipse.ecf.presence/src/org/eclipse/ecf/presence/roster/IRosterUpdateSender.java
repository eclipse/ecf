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
package org.eclipse.ecf.presence.roster;

import org.eclipse.ecf.core.util.ECFException;

/**
 * Message sender interface for sending roster update requests
 */
public interface IRosterUpdateSender {

	/**
	 * Send roster refresh request.
	 * 
	 * @throws ECFException if message cannot be sent
	 */
	public void sendRosterRefresh() throws ECFException;
	
}
