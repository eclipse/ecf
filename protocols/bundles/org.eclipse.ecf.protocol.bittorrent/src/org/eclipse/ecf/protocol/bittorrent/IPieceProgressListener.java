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
 * The <code>IPieceProgressListener</code> is used to indicate that one of the
 * pieces has just retrieved some amount of additional data from a peer.
 */
public interface IPieceProgressListener {

	/**
	 * This method is called when additional bytes has been written for a piece.
	 * 
	 * @param piece
	 *            the piece's number
	 * @param index
	 *            the position in the piece where the bytes were written to
	 * @param blockLength
	 *            the amount of bytes that has been written
	 */
	public void blockDownloaded(int piece, int index, int blockLength);

}
