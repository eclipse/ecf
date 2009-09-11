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

/**
 * If a POJO is used as a REST service object than it has to implement this interface
 * to get called if a response from the service was received. Otherwise the POJO
 * gets no content.
 */
public interface IRestResponseProcessor {
	
	/**
	 * This method is called if the response from a rest service was received.
	 * 
	 * @param response the parsed resource representation.
	 */
	public void processResource(Object response);

}
