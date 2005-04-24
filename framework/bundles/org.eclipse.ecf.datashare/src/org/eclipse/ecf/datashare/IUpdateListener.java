/*******************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.datashare;

import org.eclipse.ecf.core.identity.ID;

/**
 * Interface used by service implementations to notify the application that a
 * change has been made to the data graph by a remote group member.
 * 
 * @author pnehrer
 */
public interface IUpdateListener {

	/**
	 * Gives implementor the opportunity to consume the remote update. The
	 * implementor is expected to leave behind an empty Change Summary (with
	 * logging turned on).
	 * 
	 * @param graph
	 *            shared data graph whose remote changes to consume
	 * @param containerID
	 *            id of the remote container that made the change
	 * @return <code>true</code> if the update has been consumed,
	 *         <code>false</code> otherwise (the update will be rejected as a
	 *         result)
	 */
	boolean consumeUpdate(ISharedData graph, ID containerID);

	/**
	 * Notifies the implementor that a remote update has been received, but was
	 * not successfully applied (i.e., cannot be consumed).
	 * 
	 * @param graph
	 *            shared data graph whose update failed
	 * @param containerID
	 *            id of the container that sent the update
	 * @param cause
	 *            optional exception that caused the failure
	 */
	void updateFailed(ISharedData graph, ID containerID, Throwable cause);
}
