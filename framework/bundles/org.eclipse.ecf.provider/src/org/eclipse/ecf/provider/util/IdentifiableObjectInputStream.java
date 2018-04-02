/*******************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.provider.util;

import java.io.*;
import org.eclipse.ecf.core.util.OSGIObjectInputStream;
import org.osgi.framework.Bundle;

/**
 * Restores Java objects from the underlying stream by using the classloader
 * returned from the call to given IClassLoaderMapper with the Namespace/ID
 * specified by the associated IdentifiableObjectOutputStream.
 * 
 */
public class IdentifiableObjectInputStream extends OSGIObjectInputStream {
	IClassLoaderMapper mapper;

	public IdentifiableObjectInputStream(IClassLoaderMapper map, InputStream ins) throws IOException {
		super(null, ins);
		this.mapper = map;
	}

	/**
	 * @since 4.8
	 */
	public IdentifiableObjectInputStream(Bundle b, InputStream ins) throws IOException {
		super(b, ins);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
	 */
	@SuppressWarnings("unchecked")
	protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		String name = readUTF();
		if (name == null || mapper == null) {
			return super.resolveClass(desc);
		}
		ClassLoader cl = mapper.mapNameToClassLoader(name);
		if (cl == null)
			return super.resolveClass(desc);
		return Class.forName(desc.getName(), true, cl);
	}
}
