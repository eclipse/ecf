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

package org.eclipse.ecf.example.collab.share;

import org.eclipse.core.resources.IResource;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContext;
import org.eclipse.ui.IWorkbenchWindow;


/**
 * Interface describing access to the Eclipse resource that is associated
 * with a given collaboration group.
 * 
 * @author slewis
 */
public interface EclipseProject {

	public ID getID();
	
	public IResource getResource();
	public IWorkbenchWindow getWorkbenchWindow();
	
	public ISharedObjectContext getContext();

	public void createProxyObject(ID target,String classname,String name);
	public void messageProxyObject(ID target, String name, String meth, Object [] args);
	public void removeProxyObject(ID target,String name);

	public User getUser();
	public User getUserForID(ID user);
	public void sendPrivateMessageToUser(User touser, String msg);
	public void sendShowTextMsg(String msg);
	
}
