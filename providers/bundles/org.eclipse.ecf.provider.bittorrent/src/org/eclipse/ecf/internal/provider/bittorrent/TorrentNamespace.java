/****************************************************************************
 * Copyright (c) 2006, 2007 Remy Suen, Composent Inc., and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.bittorrent;

import java.io.File;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;

public final class TorrentNamespace extends Namespace {

	private static final long serialVersionUID = 253376096062553775L;

	private static final String SCHEME = "torrent"; //$NON-NLS-1$

	public ID createInstance(Object[] args) throws IDCreateException {
		if (args == null || args.length == 0) {
			throw new IDCreateException("parameters cannot be null or of 0 length");
		} else {
			File file = null;
			if (args[0] instanceof String) {
				file = new File((String) args[0]);
			} else if (args[0] instanceof File) {
				file = (File) args[0];
			} else {
				throw new IDCreateException("parameter-0 must be of type File or String");
			}

			if (file.isDirectory()) {
				throw new IDCreateException("file="+
						file.getAbsolutePath()+" must not be a directory");
			} else if (file.canRead()) {
				return new TorrentID(this, file);
			} else {
				throw new IDCreateException("file="+file.getAbsolutePath()+" cannot be read");
			}
		}
	}

	public String getScheme() {
		return SCHEME;
	}
	
	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { { String.class }, { File.class } };
	}

}
