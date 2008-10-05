/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.sync.doc;

/**
 * Document synchronization strategy.  Instances implementing this interface
 * expose the ability for clients to synchronize combinations of local
 * and remote documents.  
 * <p></p>
 * Owners that wish to synchronize local and remote changes
 * should call {@link #registerLocalChange(IDocumentChange)} when local
 * changes occur, and then serialize returned {@link IDocumentChangeMessage}s and
 * deliver the change message to remotes.  When remote change messages are received,
 * they should first be deserized via {@link #deserializeRemoteChange(byte[])}, and then
 * passed to {@link #transformRemoteChange(IDocumentChange)} to transform
 * the change so that when the returned IDocumentChanges are applied to the local
 * document its state will be consistent with other client(s).
 */
public interface IDocumentSynchronizationStrategy {

	/**
	 * Register local document change with document synchronization strategy.  This method
	 * should be synchronously called when a local change has
	 * been made to the underlying document.
	 * @param localChange the IDocumentChange made to the local document
	 * @return IDocumentChangeMessage[] an array of document change message to be 
	 * delivered to remote participants.
	 */
	public IDocumentChangeMessage[] registerLocalChange(IDocumentChange localChange);

	/**
	 * Transform remote document change into a set of local document changes to
	 * be synchronously applied to the local document. 
	 * @param remoteChange the remote document change instance to
	 * be transformed by this synchronization strategy.
	 * @return IDocumentChange[] to apply to local document
	 */
	public IDocumentChange[] transformRemoteChange(IDocumentChange remoteChange);

	/**
	 * Deserialization of given byte array to concrete instance of
	 * IDocumentChange object to represent local change to be applied
	 * 
	 * @param bytes the bytes to be deserialized
	 * @return IDocumentChange instance from bytes.  Will not be <code>null</code>.
	 * @throws SerializationException thrown if some problem deserializing given bytes.
	 */
	public IDocumentChange deserializeRemoteChange(byte[] bytes) throws SerializationException;
}
