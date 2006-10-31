/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.provider.comm.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.ECFProviderDebugOptions;
import org.eclipse.ecf.internal.provider.ProviderPlugin;

public class ExObjectInputStream extends ObjectInputStream {

	public ExObjectInputStream(InputStream in) throws IOException,
            SecurityException {
        super(in);
    }

    public ExObjectInputStream(InputStream in, boolean backwardCompatibility)
            throws IOException, SecurityException {
        super(in);
        if (backwardCompatibility) {
            try {
                super.enableResolveObject(true);
                debug("resolveObject");
            } catch (Exception e) {
                throw new IOException(
                        "Could not setup backward compatibility object replacers for ExObjectInputStream");
            }
        }
    }

	protected void debug(String msg) {
		Trace.trace(ProviderPlugin.getDefault(), ECFProviderDebugOptions.DEBUG,
				msg);
	}
	
	protected void traceStack(String msg, Throwable e) {
		Trace.catching(ProviderPlugin.getDefault(),
				ECFProviderDebugOptions.EXCEPTIONS_CATCHING, ExObjectInputStream.class,
				msg, e);
	}

}