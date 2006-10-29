/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.filetransfer;

import java.net.URI;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDataEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveDoneEvent;
import org.eclipse.ecf.filetransfer.events.IIncomingFileTransferReceiveStartEvent;

/**
 * Entry point retrieval file transfer adapter.  This adapter interface allows providers to
 * expose file retrieval semantics to clients in a transport independent manner.
 * To be used, a non-null adapter reference must be returned from a call to
 * {@link IContainer#getAdapter(Class)}. Once a non-null reference is
 * retrieved, then it may be used to send a retrieve request. Events will then
 * be asynchronously delivered to the provided listener to complete file
 * transfer.
 * <p>
 * For example, to retrieve a remote file and store it in a local file:
 * 
 * <pre>
 * // Get IRetrieveFileTransferContainerAdapter adapter
 * IRetrieveFileTransferContainerAdapter ftc = (IRetrieveFileTransferContainerAdapter) container
 * 		.getAdapter(IRetrieveFileTransferContainerAdapter.class);
 * if (ftc != null) {
 * 	// Create listener for receiving/responding to asynchronous file transfer events
 * 	IFileTransferListener listener = new IFileTransferListener() {
 * 		public void handleTransferEvent(IFileTransferEvent event) {
 * 			// If incoming receive start event, respond by specifying local file to save to
 * 			if (event instanceof IIncomingFileTransferReceiveStartEvent) {
 * 				IIncomingFileTransferReceiveStartEvent rse = (IIncomingFileTransferReceiveStartEvent) event;
 * 				try {
 * 					rse.receive(new File(&quot;eclipse.org.main.page.html&quot;));
 * 				} catch (IOException e) {
 * 					// Handle exception appropriately 
 * 				}
 * 			}
 * 		}
 * 	};
 * 	// Identify file to retrieve
 * 	URI remoteFileToRetrieve = new URI(&quot;http://www.composent.com/index.html&quot;);
 * 	// Actually make request to start retrieval.  The listener provided will then be notified asynchronously 
 * 	// as file transfer events occur
 * 	ftc.sendRetrieveRequest(remoteFileToRetrieve, listener);
 * }
 * </pre>
 * 
 * Where the IFileTransferEvent subtypes <b>for the receiver</b> will be:
 * <ul>
 * <li>{@link IIncomingFileTransferReceiveStartEvent}</li>
 * <li>{@link IIncomingFileTransferReceiveDataEvent}</li>
 * <li>{@link IIncomingFileTransferReceiveDoneEvent}</li>
 * </ul>
 */
public interface IRetrieveFileTransferContainerAdapter {
	/**
	 * Send request for transfer of a remote file to local file storage. This
	 * method is used to initiate a file retrieve for a remoteFileID (first
	 * parameter). File transfer events are asynchronously delivered a file
	 * transfer listener (second parameter). The given remoteFileID and
	 * transferListener must not be null.
	 * 
	 * @param remoteFileReference
	 *            reference to the remote target file (e.g.
	 *            http://www.eclipse.org/index.html) or a reference to a
	 *            resource that specifies the location of a target file.
	 *            Implementing providers will determine what protocol schemes
	 *            are supported (e.g. ftp, http, torrent, file, etc) and the
	 *            required format of the scheme-specific information. If a
	 *            protocol is specified that is not supported, or the
	 *            scheme-specific information is not well-formed, then an
	 *            IncomingFileTransferException will be thrown. Must not be null
	 * @param transferListener
	 *            a listener for file transfer events. Must not be null
	 * @throws IncomingFileTransferException
	 *             if the provider is not connected or is not in the correct
	 *             state for initiating file transfer
	 */
	public void sendRetrieveRequest(URI remoteFileReference,
			IFileTransferListener transferListener)
			throws IncomingFileTransferException;

}
