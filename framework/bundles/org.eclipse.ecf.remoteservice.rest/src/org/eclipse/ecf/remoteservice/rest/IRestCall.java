/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rest;

import java.util.Map;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.IRemoteService;

/**
 * Rest call provides a way for clients to access/call a remote service. Instances
 * can be created via the {@link RestCallFactory} static method.  Created instances
 * typically will be passed to one of the call methods on {@link IRemoteService}.
 */
public interface IRestCall extends IRemoteCall {

	/**
	 * Provides any call-specific request headers.
	 * 
	 * @return a {@link Map} object which contains and additional header parameters
	 *         (String->String). May be <code>null</code>.
	 */
	public Map getRequestHeaders();

}
