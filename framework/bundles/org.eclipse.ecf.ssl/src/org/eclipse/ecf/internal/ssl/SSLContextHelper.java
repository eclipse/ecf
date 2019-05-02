/*******************************************************************************
 * Copyright (c) Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 ******************************************************************************/
package org.eclipse.ecf.internal.ssl;

import java.security.SecureRandom;
import java.util.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class SSLContextHelper {

	private static final String[] jreProtocols = new String[] { "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3" };

	public static SSLContext getSSLContext(String protocols) {
		SSLContext resultContext = null;
		if (protocols != null) {

			String[] httpsProtocols = protocols.split(",");
			// trim to make sure
			for (int i = 0; i < httpsProtocols.length; i++)
				httpsProtocols[i] = httpsProtocols[i].trim();
			// Now put into defaultProtocolsList in order of jreProtocols
			List<String> splitProtocolsList = Arrays.asList(httpsProtocols);
			List<String> defaultProtocolsList = new ArrayList();
			for (int i = 0; i < jreProtocols.length; i++)
				if (splitProtocolsList.contains(jreProtocols[i]))
					defaultProtocolsList.add(jreProtocols[i]);
			// In order of jre protocols, attempt to create and init SSLContext
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
		return resultContext;
	}
}
