/****************************************************************************
 * Copyright (c) 2006, 2008 Remy Suen, Composent Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.protocol.bittorrent;

/**
 * This listener reports on the overall progress of the current download.
 */
public interface ITorrentProgressListener {

	/**
	 * This method is called when a piece has been identified as being completed
	 * after a hash check verification has completed.
	 * 
	 * @param completed
	 *            the number of pieces completed thus far
	 */
	public void pieceCompleted(int completed);

}
