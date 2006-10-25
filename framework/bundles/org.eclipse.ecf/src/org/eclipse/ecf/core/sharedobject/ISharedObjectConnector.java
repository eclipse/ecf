/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.sharedobject;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.events.ISharedObjectEvent;
import org.eclipse.ecf.core.util.AsynchResult;
import org.eclipse.ecf.core.util.QueueException;

/**
 * Implementers which represent the one-way associations between SharedObject
 * instances within the scope of a given ISharedObjectContainer
 * 
 * @see ISharedObjectManager#connectSharedObjects(ID, ID[])
 */
public interface ISharedObjectConnector {
	public ID getSender();

	public ID[] getReceivers();

	public void enqueue(ISharedObjectEvent event) throws QueueException;

	public void enqueue(ISharedObjectEvent[] events) throws QueueException;

	public AsynchResult[] callAsynch(ISharedObjectEvent arg) throws Exception;

	public void dispose();
}