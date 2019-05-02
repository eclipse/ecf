/*******************************************************************************
* Copyright (c) 2019 Yatta Solutions and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Yatta Solutions - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclient45;

import java.util.Map;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Proxy;

public interface INTLMProxyHandler {
	void handleNTLMProxy(Proxy proxy, int code) throws ECFException;

	void handleSPNEGOProxy(Proxy proxy, int code) throws ECFException;

	boolean allowNTLMAuthentication(Map<?, ?> connectOptions);
}
