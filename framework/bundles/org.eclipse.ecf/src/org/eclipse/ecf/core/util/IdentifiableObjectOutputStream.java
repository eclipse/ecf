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
package org.eclipse.ecf.core.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.eclipse.ecf.core.identity.ID;

/**
 * Stores Java objects in the underlying stream in an manner that allows corresponding
 * input stream to use ID to lookup appropriate associated classloader (via IClassLoaderMapper).
 * 
 * @author pnehrer
 * @author slewis
 */
public class IdentifiableObjectOutputStream extends ObjectOutputStream {

    ID objectID = null;
    
    public IdentifiableObjectOutputStream(ID id, OutputStream outs) throws IOException {
        super(outs);
        this.objectID = id;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.io.ObjectOutputStream#annotateClass(java.lang.Class)
     */
    protected void annotateClass(Class cl) throws IOException {
        String name = objectID.getName();
        writeUTF(name);
    }

}
