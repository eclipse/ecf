/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.resource;

import java.util.Map;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.rest.IRestCallable;

/**
 * This interface can be used to register services for the resource
 * representation creation process. A sample implementation can be found on
 * {@link XMLResource} for XML.
 */
public interface IRestResourceProcessor {

	/**
	 * Parse a REST responseBody in an expected format i.e. XML or JSON. Then return the 
	 * correct type of resulting representation.
	 * @param call the IRemoteCall used to initiate the rest call.  Will not be <code>null</code>.
	 * @param callable the IRestCallable used to initiate the rest call.  Will not be <code>null</code>.
	 * @param responseHeaders any http response headers (in map as <String>,<String>).  Will not be <code>null</code>, but may be empty.
	 * @param responseBody
	 *            the string representation from the response.
	 * 
	 * @return the parsed and processed response. Can be <code>null</code>.  Should be of type expected by clients.
	 * 
	 * @throws ECFException thrown if the response cannot be parsed, or the representation cannot be created.
	 */
	public Object createResponseRepresentation(IRemoteCall call, IRestCallable callable, Map responseHeaders, String responseBody) throws ECFException;

}
