/****************************************************************************
 * Copyright (c) 2014 Markus Alexander Kuppe and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservice;

import java.util.Dictionary;
import java.util.Map;

/**
 * @since 8.3
 *
 */
public interface IExtendedRemoteServiceRegistration extends IRemoteServiceRegistration {

	/**
	 * @return A {@link Dictionary} of properties not intended to be used for service advertisement.
	 */
	public Map<String, Object> getExtraProperties();

}
