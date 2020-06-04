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
 * This listener monitors problems that arise such as tracker errors and
 * downloaded pieces which fail hash checks.
 */
public interface ITorrentErrorListener {

	/**
	 * This method is called when the tracker returns an error.
	 * 
	 * @param message
	 *            the failure reason provided by the tracker
	 */
	public void trackerError(String message);

	/**
	 * This method is called when a piece has failed the integrity hash check
	 * and has to be downloaded again.
	 * 
	 * @param piece
	 *            the number of the piece that failed the hash check
	 * @param pieceLength
	 *            the length of the piece indicating the amount of data that has
	 *            been discarded
	 */
	public void pieceDiscarded(int piece, int pieceLength);

}
