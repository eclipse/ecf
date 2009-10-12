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

import java.text.ParseException;

import org.eclipse.ecf.remoteservice.rest.IRestCall;

/**
 * This interface can be used to register services for the resource representation creation process.
 * A sample implementation can be found on {@link XMLResource} for XML.
 */
public interface IRestResource {
	
	/**
	 * Returns the identifier of this resource. This is used to check which resource
	 * should be used to parse a response from a IRestCall. Therefore the result from
	 * {@link IRestCall#getEstimatedResourceIdentifier()} will be compared with the result of this method.
	 * 
	 * @return the identifier for this resource. Cannot be <code>null</code>.
	 */
	public String getIdentifier();
	
	/**
	 * Parse a REST response to a given format i.e. XML or JSON. Implementations 
	 * can be registered with a normal OSGi service.
	 * 
	 * @param responseBody the string representation from the response.
	 * @return the parsed response. Can be <code>null</code>.
	 */
	public Object createRepresentation(String responseBody) throws ParseException;	

}
