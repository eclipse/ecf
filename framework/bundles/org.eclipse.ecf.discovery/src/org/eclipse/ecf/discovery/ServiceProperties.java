/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.discovery;

import java.util.Enumeration;
import java.util.Properties;

public class ServiceProperties implements IServiceProperties {

	Properties props = new Properties();

	public ServiceProperties() {
		super();
		props = new Properties();
	}

	public ServiceProperties(Properties props) {
		super();
		this.props = (props == null)?new Properties():props;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceProperties#getPropertyNames()
	 */
	public Enumeration getPropertyNames() {
		return props.keys();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceProperties#getPropertyString(java.lang.String)
	 */
	public String getPropertyString(String name) {
		Object val = props.get(name);
		if (val instanceof String) {
			return (String) val;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceProperties#getPropertyBytes(java.lang.String)
	 */
	public byte[] getPropertyBytes(String name) {
		Object val = props.get(name);
		if (val instanceof byte[]) {
			return (byte[]) val;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.discovery.IServiceProperties#getProperty(java.lang.String)
	 */
	public Object getProperty(String name) {
		return props.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceProperties#setProperty(java.lang.String, java.lang.Object)
	 */
	public Object setProperty(String name, Object value) {
		return props.put(name, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceProperties#setPropertyBytes(java.lang.String, byte[])
	 */
	public Object setPropertyBytes(String name, byte[] value) {
		return props.put(name,value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.IServiceProperties#setPropertyString(java.lang.String, java.lang.String)
	 */
	public Object setPropertyString(String name, String value) {
		return props.put(name,value);
	}
}
