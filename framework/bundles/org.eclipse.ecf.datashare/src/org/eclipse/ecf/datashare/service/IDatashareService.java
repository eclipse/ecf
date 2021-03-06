/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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

package org.eclipse.ecf.datashare.service;

import org.eclipse.ecf.datashare.IChannelContainerAdapter;

/**
 * OSGI datashare service interface.  This interface should be registered
 * by providers when they wish to expose datashare services to OSGI
 * service clients.
 */
public interface IDatashareService extends IChannelContainerAdapter {
	//
}
