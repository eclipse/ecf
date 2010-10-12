/*******************************************************************************
 * Copyright (c) 2010 Naumen. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Pavel Samolisov - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservice.rpc.identity;

import java.net.URI;
import java.net.URL;
import java.util.*;
import org.eclipse.ecf.core.identity.*;
import org.eclipse.ecf.internal.remoteservice.rpc.Messages;

/**
 * This class represents a {@link Namespace} for {@link RPCClientContainer}s.
 */
public class RPCNamespace extends Namespace {

	private static final long serialVersionUID = -4255624538742281975L;

	/**
	 * The name of this namespace.
	 */
	public static final String NAME = "ecf.xmlrpc.namespace"; //$NON-NLS-1$

	/**
	 * The scheme of this namespace.
	 */
	public static final String SCHEME = "xmlrpc"; //$NON-NLS-1$

	public RPCNamespace() {
		// nothing
	}

	public RPCNamespace(String name, String desc) {
		super(name, desc);
	}

	private String getInitFromExternalForm(Object[] args) {
		if (args == null || args.length < 1 || args[0] == null)
			return null;
		if (args[0] instanceof String) {
			final String arg = (String) args[0];
			if (arg.startsWith(getScheme() + Namespace.SCHEME_SEPARATOR)) {
				final int index = arg.indexOf(Namespace.SCHEME_SEPARATOR);
				if (index >= arg.length())
					return null;
				return arg.substring(index + 1);
			}
		}
		return null;
	}

	/**
	 * Creates an instance of an {@link RPCD}. The parameters must contain specific information.
	 * 
	 * @param parameters a collection of attributes to call the right constructor on {@link RPCID}.
	 * @return an instance of {@link RPCID}. Will not be <code>null</code>.
	 */
	public ID createInstance(Object[] parameters) throws IDCreateException {
		URI uri = null;
		try {
			final String init = getInitFromExternalForm(parameters);
			if (init != null) {
				uri = URI.create(init);
				return new RPCID(this, uri);
			}
			if (parameters != null) {
				if (parameters[0] instanceof URI)
					return new RPCID(this, (URI) parameters[0]);
				else if (parameters[0] instanceof String)
					return new RPCID(this, URI.create((String) parameters[0]));
				else if (parameters[0] instanceof URL)
					return new RPCID(this, URI.create(((URL) parameters[0]).toExternalForm()));
				else if (parameters[0] instanceof RPCID)
					return (ID) parameters[0];
			}
			throw new IllegalArgumentException(Messages.RPC_INVALID_PARAMETERS_TO_ID_CREATION);
		} catch (Exception e) {
			throw new IDCreateException(Messages.RPC_COULD_NOT_CREATE_ID, e);
		}
	}

	public Class[][] getSupportedParameterTypes() {
		return new Class[][] { {String.class}, {Integer.class}, {Boolean.class}, {Double.class}, {Date.class},
				{byte[].class}, {Object[].class}, {List.class}, {Map.class}};
	}

	public String getScheme() {
		return SCHEME;
	}
}