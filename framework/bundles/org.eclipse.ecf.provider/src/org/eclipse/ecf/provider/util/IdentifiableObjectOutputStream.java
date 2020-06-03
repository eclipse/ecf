/****************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.provider.util;

import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.ecf.core.util.OSGIObjectOutputStream;

/**
 * Stores Java objects in the underlying stream in an manner that allows
 * corresponding input stream to use ID to lookup appropriate associated
 * classloader (via IClassLoaderMapper).
 * 
 */
public class IdentifiableObjectOutputStream extends OSGIObjectOutputStream {
	String name = null;

	public IdentifiableObjectOutputStream(String name, OutputStream outs) throws IOException {
		super(outs);
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.ObjectOutputStream#annotateClass(java.lang.Class)
	 */
	protected void annotateClass(Class cl) throws IOException {
		writeUTF(name);
	}
}
