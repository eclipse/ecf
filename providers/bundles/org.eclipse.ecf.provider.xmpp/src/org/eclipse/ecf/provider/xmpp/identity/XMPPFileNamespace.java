/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.provider.xmpp.identity;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.internal.provider.xmpp.Messages;

/**
 *
 */
public class XMPPFileNamespace extends Namespace {

	private static final long serialVersionUID = 629370079122562988L;

	public static final String SCHEME = "xmppfile"; //$NON-NLS-1$

	public static final String NAME = "ecf.provider.filetransfer.xmpp"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.Namespace#createInstance(java.lang.Object[])
	 */
	public ID createInstance(Object[] parameters) throws IDCreateException {
		if (parameters == null || parameters.length < 2 || !(parameters[0] instanceof XMPPID || !(parameters[1] instanceof String)))
			throw new IDCreateException(Messages.XMPPFileNamespace_EXCEPTION_INVALID_FILEID_PARAMETERS);
		final XMPPID target = (XMPPID) parameters[0];
		final String filename = (String) parameters[1];
		if (target == null)
			throw new IDCreateException(Messages.XMPPFileNamespace_EXCEPTION_FILEID_TARGETID_NOT_NULL);
		if (filename == null)
			throw new IDCreateException(Messages.XMPPFileNamespace_EXCEPTION_FILEID_FILENAME_NOT_NULL);
		return new XMPPFileID(target, filename);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
	 */
	public String getScheme() {
		return SCHEME;
	}

}
