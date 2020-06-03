/****************************************************************************
 * Copyright (c) 2015 Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Scott Lewis - initial API and implementation
 * 
 * @since 3.3
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteserviceadmin.ui.endpoint.model;

/**
 * @since 3.3
 */
public class EndpointPropertyNode extends AbstractEndpointNode {

	private final String propertyName;
	private String propertyAlias;
	private String nameValueSeparator = ": "; //$NON-NLS-1$

	public EndpointPropertyNode(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyAlias() {
		return this.propertyAlias;
	}

	public void setPropertyAlias(String propertyAlias) {
		this.propertyAlias = propertyAlias;
	}

	public String getNameValueSeparator() {
		return nameValueSeparator;
	}

	public void setNameValueSeparator(String nameValueSeparator) {
		this.nameValueSeparator = nameValueSeparator;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public Object getPropertyValue() {
		return getEndpointDescriptionProperties().get(propertyName);
	}

}
