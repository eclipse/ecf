/****************************************************************************
 * Copyright (c) 2004 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.filetransfer;

/**
 * Adapter for setting rate control on IFileTransferInfo instances that expose
 * expose this adapter interface via
 * {@link IFileTransfer#getAdapter(Class adapter)}. To use this interface,
 * clients should do the following:
 * 
 * <pre>
 *   IFileTransfer fileTransfer;
 *   IFileTransferRateControl rateController = (IFileTransferRateControl) fileTransfer.getAdapter(IFileTransferRateControl.class);
 *   if (rateController !=null) {
 *      ... use it
 *   } else {
 *      ... does not support rate control
 *   }
 * </pre>
 */
public interface IFileTransferRateControl {
	/**
	 * Set maximum download speed in bytes/second. Specifying a maximum download
	 * speed of 0 indicates that any exiting rate cap should be removed, and the
	 * transfer should proceed as fast as possible a
	 * 
	 * @param maxDownloadSpeed
	 *            in bytes/second
	 */
	public void setMaxDownloadSpeed(long maxDownloadSpeed);

	/**
	 * Set maximum upload speed in bytes/second. Specifying a maximum upload
	 * speed of 0 indicates that any exiting rate cap should be removed, and the
	 * transfer should proceed as fast as possible
	 * 
	 * @param maxUploadSpeed
	 *            in bytes/second
	 */
	public void setMaxUploadSpeed(long maxUploadSpeed);
}
