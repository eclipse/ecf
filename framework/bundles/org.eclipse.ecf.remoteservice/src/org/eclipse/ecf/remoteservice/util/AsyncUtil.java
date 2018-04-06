/*******************************************************************************
* Copyright (c) 2018 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.util;

import java.util.Arrays;
import org.eclipse.ecf.remoteservice.Constants;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;

/**
 * @since 8.13
 */
public class AsyncUtil {

	public static boolean isOSGIAsync(IRemoteServiceReference ref) {
		if (ref == null)
			return false;
		Object p = ref.getProperty(Constants.OSGI_ASYNC_INTENT);
		if (p != null)
			return true;
		// If service.intents has values, and the osgi.async is present then it's also yes
		String[] serviceIntents = (String[]) ref.getProperty(Constants.OSGI_SERVICE_INTENTS);
		if (serviceIntents != null && Arrays.asList(serviceIntents).contains(Constants.OSGI_ASYNC_INTENT))
			return true;
		// otherwise no
		return false;
	}
}
