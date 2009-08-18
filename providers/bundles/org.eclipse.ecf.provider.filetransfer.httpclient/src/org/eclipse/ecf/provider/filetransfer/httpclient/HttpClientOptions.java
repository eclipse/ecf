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

public interface HttpClientOptions {
	// HttpClient response code that indicates that NTLM proxy is asking for authentication
	// and httpclient cannot handle NTLMv2 proxies
	public int NTLM_PROXY_RESPONSE_CODE = 477;
	// System property that indicates that NTLM proxy usage should be forced (i.e. not rejected)
	// The property key is:  org.eclipse.ecf.provider.filetransfer.httpclient.options.ForceNTLMProxy
	// The value of the property must be non-null, but is not otherwise used.
	public String FORCE_NTLM_PROP = "org.eclipse.ecf.provider.filetransfer.httpclient.options.ForceNTLMProxy"; //$NON-NLS-1$
}
