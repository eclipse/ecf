/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.provider.filetransfer.httpclient;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthState;

public class NTLMProxyDetector {

	private static final String PROXY_NTLM_VALUE = "NTLM"; //$NON-NLS-1$

	public static boolean detectNTLMProxy(HttpMethodBase method) {
		if (method == null)
			return false;
		AuthState authState = method.getProxyAuthState();
		if (authState == null)
			return false;
		AuthScheme authScheme = authState.getAuthScheme();
		if (authScheme == null)
			return false;
		String schemeName = authScheme.getSchemeName();
		if (schemeName == null)
			return false;
		return schemeName.equalsIgnoreCase(PROXY_NTLM_VALUE);
	}
}
