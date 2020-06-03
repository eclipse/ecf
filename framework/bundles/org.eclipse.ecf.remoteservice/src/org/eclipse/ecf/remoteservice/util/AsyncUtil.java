/****************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
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
