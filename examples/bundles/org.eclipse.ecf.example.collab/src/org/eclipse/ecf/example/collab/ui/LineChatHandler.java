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

package org.eclipse.ecf.example.collab.ui;

import java.util.Map;

import org.eclipse.ecf.core.ContainerJoinException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IJoinContext;
import org.eclipse.ecf.example.collab.share.User;


public interface LineChatHandler {

    public String getWindowTitle();
    public String getTreeTopLabel();
    public String getVersionString();
    
    public ID getServerID();
    public User getUser();
    public void chatException(Exception e, String msg);
    public void chatGUIDestroy();
	public void sendStartedTyping(); 
    public void inputText(String aString);
    
    public ID makeObject(ID target, String classname, Map map) 
        throws Exception;

    public Object getObject(ID target);

    public void joinGroup(ID remote, IJoinContext joinContext)
        throws ContainerJoinException;
        
    public void leaveGroup();
    
    public void refreshProject();
    
    public void sendRingMessageToUser(User touser,String msg);
	public void sendPrivateMessageToUser(User touser, String msg);
	
	public void makeProxyObject(ID target,String classname,String name);
	public void messageProxyObject(ID target, String name, String meth, Object [] args);
	public void removeProxyObject(ID target,String name);
	
	public void sendCVSProjectUpdateRequest(User touser, String msg);
	public boolean isCVSShared();
	
	public void sendShowViewWithID(User touser, String id, String secID, Integer mode);
	public void sendShowView(User touser, String id);
}
