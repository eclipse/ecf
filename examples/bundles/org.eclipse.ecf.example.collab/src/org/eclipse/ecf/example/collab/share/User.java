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

import java.io.Serializable;
import java.util.Vector;

import org.eclipse.ecf.core.identity.ID;

public class User implements Serializable {
	
	protected Vector userFields;
	protected ID userID;
	protected String nickname;

	public User(ID userID, String username, Vector userFields) {
		this.userID = userID;
		this.nickname = username;
		this.userFields = userFields;
	}

	public Vector getUserFields() {
		return userFields;
	}

	public ID getUserID() {
		return userID;
	}
	public String getNickname() {
		return nickname;
	}

	public void setUserFields(Vector uF) {
		this.userFields = uF;
	}
	public void setNickname(String name) {
		this.nickname = name;
	}

	public String toString() {
		if (nickname != null)
			return nickname;
		else
			return userID.getName();
	}
}
