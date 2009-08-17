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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;

public class NTLMProxyDetector {

	private static final String PROXY_AUTHENTICATE = "Proxy-Authenticate"; //$NON-NLS-1$

	private static final Object PROXY_NEGOTIATE_VALUE = "Negotiate"; //$NON-NLS-1$
	private static final Object PROXY_KERBEROS_VALUE = "Kerberos"; //$NON-NLS-1$
	private static final Object PROXY_NTLM_VALUE = "NTLM"; //$NON-NLS-1$

	public static boolean detectNTLMProxy(HttpMethodBase method) {
		if (method == null)
			return false;
		Header[] responseHeaders = method.getResponseHeaders(PROXY_AUTHENTICATE);
		if (responseHeaders == null)
			return false;
		boolean proxyNegotiateValue = false;
		boolean proxyKerberosValue = false;
		boolean proxyNTLMValue = false;
		for (int i = 0; i < responseHeaders.length; i++) {
			String val = responseHeaders[i].getValue();
			if (val != null) {
				val.trim();
				if (val.equals(PROXY_NEGOTIATE_VALUE))
					proxyNegotiateValue = true;
				if (val.equals(PROXY_KERBEROS_VALUE))
					proxyKerberosValue = true;
				if (val.equals(PROXY_NTLM_VALUE))
					proxyNTLMValue = true;
			}
		}
		if (proxyNegotiateValue && proxyKerberosValue && proxyNTLMValue)
			return true;
		return false;
	}
}
