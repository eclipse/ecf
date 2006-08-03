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

package org.eclipse.ecf.remoteservice;

import org.eclipse.ecf.core.util.ECFException;

public interface IRemoteService {
	public Object callSynch(IRemoteCall call) throws ECFException;
	public void callAsynch(IRemoteCall call, IRemoteCallListener listener);
	public void fireAsynch(IRemoteCall call) throws ECFException;
	public Object getProxy() throws ECFException;
}
