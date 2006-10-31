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

public interface IServiceProperties {
	/**
	 * Get property names. This should return an Enumeration of String objects
	 * that identify all of the names in this IServiceProperties instance
	 * 
	 * @return Enumeration of all service property names as Strings
	 */
	public Enumeration getPropertyNames();

	/**
	 * Get property name as String. Returns a valid String if there is a
	 * property of the given name. Returns null if there is no property by that
	 * name, or if the property has some other type than String.
	 * 
	 * @param name
	 *            the name of the property to return
	 * @return the property as a String
	 */
	public String getPropertyString(String name);

	/**
	 * Get property name as byte[]. Returns a non-null byte[] if there is a
	 * property of the given name. Returns null if there is no property by that
	 * name, or if the property has some other type than byte[].
	 * 
	 * @param name
	 *            the name of the property to return
	 * @return the property as a byte[]
	 */
	public byte[] getPropertyBytes(String name);

	/**
	 * Get property as an Object. Returns a non-null Object if there is a
	 * property of the given name. Returns null if there is no property by that
	 * name.
	 * 
	 * @param name
	 *            the name of the property to return
	 * @return the property as an Object
	 */
	public Object getProperty(String name);
}
