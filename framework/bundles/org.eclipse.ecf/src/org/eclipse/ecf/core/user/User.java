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

package org.eclipse.ecf.core.user;

import java.util.Map;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.user.IUser;

public class User implements IUser {

	protected ID id;
	protected String name;
	protected Map properties;
	
    public User(ID userID) {
        this(userID,userID.getName());
    }
    public User(ID userID, String name) {
        this(userID,name,null);
    }
	public User(ID userID, String name, Map properties) {
        if (userID == null) throw new NullPointerException("userID must not be null");
		this.id = userID;
		this.name = name;
		this.properties = properties;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.user.IUser#getProperties()
	 */
	public Map getProperties() {
		return properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.IIdentifiable#getID()
	 */
	public ID getID() {
		return id;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
    
    public Object getAdapter(Class clazz) {
        return null;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("User[");
        sb.append("id="+getID()).append(";name="+getName()).append(";props="+getProperties()).append("]");
        return sb.toString();
    }
}
