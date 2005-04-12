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
package org.eclipse.ecf.presence;

import java.util.Map;

import org.eclipse.ecf.core.util.ECFException;

public interface IAccountManager {

	public void changePassword(String newpassword) throws ECFException;
	public void createAccount(String username, String password, Map attributes) throws ECFException;
	public void deleteAccount() throws ECFException;
	public String getAccountInstructions();
	public String[] getAccountAttributeNames();
	public Object getAccountAttribute(String name);
	public boolean supportsCreation();
}
