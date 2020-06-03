/****************************************************************************
 * Copyright (c) 2010 Eugen Reiswich.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Eugen Reiswich - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.tests.provider.xmpp.remoteservice;

import org.eclipse.ecf.provider.xmpp.identity.XMPPID;

public interface IExampleService {

	public XMPPID getClientID();
}