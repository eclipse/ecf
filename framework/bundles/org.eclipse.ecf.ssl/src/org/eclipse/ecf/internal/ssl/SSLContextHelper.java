/****************************************************************************
 * Copyright (c) Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.ssl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class SSLContextHelper {

	private static final String[] jreProtocols = new String[] { "TLSv1.3", "TLSv1.2", "TLSv1.2", "TLSv1.1", "TLSv1",
			"SSLv3" };

	public static SSLContext getSSLContext(String protocols) {
		SSLContext resultContext = null;
		try {
			resultContext = SSLContext.getDefault();
		} catch (NoSuchAlgorithmException pkiNotAvailableERR) {
			if (protocols != null) {
				String[] httpsProtocols = protocols.split(",");
				// trim to make sure
				for (int i = 0; i < httpsProtocols.length; i++)
					httpsProtocols[i] = httpsProtocols[i].trim();
				// Now put into defaultProtocolsList in order of jreProtocols
				List<String> splitProtocolsList = Arrays.asList(httpsProtocols);
				List<String> defaultProtocolsList = new ArrayList();
				for (String jreProtocol : jreProtocols) {
					if (splitProtocolsList.contains(jreProtocol)) {
						defaultProtocolsList.add(jreProtocol);
					}
				}
				// In order of jre protocols, attempt to create and init
				// SSLContext
				for (String protocol : defaultProtocolsList) {
					try {
						resultContext = SSLContext.getInstance(protocol);
						resultContext.init(null, new TrustManager[] { new ECFTrustManager() }, new SecureRandom());
						break;
					} catch (Exception e) {
						// just continue to look for SSLContexts with the next
						// protocolName
					}
				}
			}
		}
		return resultContext;
	}
}
