/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.filetransfer;

/**
 * An object that describes a file range specification.  Object implementations of this
 * class can be 
 */
public interface IFileRangeSpecification {

	/**
	 * Get the start position to start from.  The position is in bytes, and byte 0 is the first byte
	 * of the file, N-1 is the last position in the file, where N is the length of the file in bytes.  
	 * @return the position in the file (in bytes) to start from.  If the returned start position is
	 * less than 0, or equal to or greater than N, then it is an invalid range specification and
	 * will result in a InvalidPartialFileTransferRequestException.
	 */
	public long getStartPosition();

	/**
	 * Get the end position of transfer range.  The position is in bytes, and byte 0 is the first byte
	 * of the file, N-1 is the last position in the file, where N is the length of the file in bytes.  
	 * @return the position in the file (in bytes) to indicate the end of range to retrieve.  If equal to -1,
	 * then this means that no end position is specified, and the download will continue to the end of file.  If >= 0,
	 * but less than the {@link #getStartPosition()} then this range specification is invalid.  If greater than or
	 * equal to N (where N is length of the file in bytes), then the remaining part of the given file will
	 * be downloaded.
	 */
	public long getEndPosition();

}
