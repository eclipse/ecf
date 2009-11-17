/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest;

/**
 * Rest parameter used for defining an {@link IRestCallable}
 */
public interface IRestParameter {

	/**
	 * Get the name for the parameter.  Must not return <code>null</code>.
	 * @return String the parameter name.
	 */
	public String getName();

	/**
	 * Get the parameter value.  Should not return <code>null</code>.
	 * @return String the parameter value.
	 */
	public String getValue();

}
