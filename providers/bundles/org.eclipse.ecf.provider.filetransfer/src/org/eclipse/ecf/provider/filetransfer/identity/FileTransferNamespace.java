/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.filetransfer.identity;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.internal.provider.filetransfer.Activator;
import org.eclipse.ecf.internal.provider.filetransfer.Messages;

/**
 * URL file namespace class. This defines a namespace that understands how to
 * create IFileID instances from arbitary URLs
 */
public class FileTransferNamespace extends Namespace {

	private static final long serialVersionUID = 8204058147686930765L;

	public static final String PROTOCOL = Messages.FileTransferNamespace_Namespace_Protocol;

	public static final String[] jvmSchemes = new String[] {
			Messages.FileTransferNamespace_Http_Protocol,
			Messages.FileTransferNamespace_Ftp_Protocol,
			Messages.FileTransferNamespace_File_Protocol,
			Messages.FileTransferNamespace_Jar_Protocol };

	public static final String[] localSchemes = new String[] {
			Messages.FileTransferNamespace_Http_Protocol,
			Messages.FileTransferNamespace_Https_Protocol };

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.Namespace#createInstance(java.lang.Object[])
	 */
	public ID createInstance(Object[] args) throws IDCreateException {
		if (args == null || args.length == 0)
			throw new IDCreateException(
					Messages.FileTransferNamespace_Exception_Args_Null);
		try {
			if (args[0] instanceof URL)
				return new FileTransferID(this, (URL) args[0]);
			if (args[0] instanceof String)
				return new FileTransferID(this, new URL((String) args[0]));
		} catch (Exception e) {
			throw new IDCreateException(
					Messages.FileTransferNamespace_Exception_Create_Instance, e);
		}
		throw new IDCreateException(
				Messages.FileTransferNamespace_Exception_Create_Instance_Failed);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.Namespace#getSupportedSchemes()
	 */
	public String[] getSupportedSchemes() {
		Set result = new HashSet();
		String[] platformSchemes = Activator.getDefault()
				.getPlatformSupportedSchemes();
		for (int i = 0; i < jvmSchemes.length; i++)
			result.add(jvmSchemes[i]);
		for (int i = 0; i < platformSchemes.length; i++)
			result.add(platformSchemes[i]);
		for (int i = 0; i < localSchemes.length; i++)
			result.add(localSchemes[i]);
		return (String[]) result.toArray(new String[] {});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.Namespace#getScheme()
	 */
	public String getScheme() {
		return PROTOCOL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.core.identity.Namespace#getSupportedParameterTypesForCreateInstance()
	 */
	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { { URL.class }, { String.class } };
	}

}
