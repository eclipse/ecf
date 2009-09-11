/******************************************************************************* 
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.ecf.remoteservice.rest.util;

import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.rest.IRestCall;

/**
 * Service class for creating a {@link IRemoteCall}. This will be used to associate
 * an {@link IRemoteCall} with an {@link IRestCall}. Therefore the {@link #getMethod()}
 * method will used as key.
 */
public class RestRemoteCall implements IRemoteCall {
	
	private String key;

	public RestRemoteCall(String key) {
		this.key = key;
	}

	public String getMethod() {
		return key;
	}

	public Object[] getParameters() {
		return null;
	}

	public long getTimeout() {
		return 0;
	}
}
