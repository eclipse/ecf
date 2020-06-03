/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.provider.comm.tcp;

import java.io.*;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.ECFProviderDebugOptions;
import org.eclipse.ecf.internal.provider.ProviderPlugin;

public class ExObjectOutputStream extends ObjectOutputStream {

	public ExObjectOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	public ExObjectOutputStream(OutputStream out, boolean backwardCompatibility) throws IOException, SecurityException {
		this(out);
		if (backwardCompatibility) {
			try {
				super.enableReplaceObject(true);
				debug("replaceObject"); //$NON-NLS-1$
			} catch (Exception e) {
				throw new IOException("Exception setting up ExObjectOutputStream: " + e.getMessage()); //$NON-NLS-1$
			}
		}
	}

	protected void debug(String msg) {
		Trace.trace(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.DEBUG, msg);
	}

	protected void traceStack(String msg, Throwable e) {
		Trace.catching(ProviderPlugin.PLUGIN_ID, ECFProviderDebugOptions.EXCEPTIONS_CATCHING, ExObjectOutputStream.class, msg, e);
	}

}