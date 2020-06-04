/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer.scp;

import java.net.URL;
import java.util.Map;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;

/**
 *
 */
public interface IScpFileTransfer {

	public IConnectContext getConnectContext();

	public String getUsername();

	public void setUsername(String username);

	public Proxy getProxy();

	public URL getTargetURL();

	public Map getOptions();

}
