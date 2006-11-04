/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.provider.generic;

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.provider.IContainerInstantiator;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.ECFProviderDebugOptions;
import org.eclipse.ecf.internal.provider.ProviderPlugin;

public class GenericContainerInstantiator implements IContainerInstantiator {
	public static final String TCPCLIENT_NAME = "ecf.generic.client";

	public static final String TCPSERVER_NAME = "ecf.generic.server";

	public GenericContainerInstantiator() {
		super();
	}

	protected void debug(String msg) {
		Trace.trace(ProviderPlugin.getDefault(), ECFProviderDebugOptions.DEBUG,
				msg);
	}

	protected void traceStack(String msg, Throwable e) {
		Trace.catching(ProviderPlugin.getDefault(),
				ECFProviderDebugOptions.EXCEPTIONS_CATCHING, SOContainer.class,
				msg, e);
	}

	protected ID getIDFromArg(Object arg) throws IDCreateException {
		if (arg instanceof ID)
			return (ID) arg;
		if (arg instanceof String) {
			String val = (String) arg;
			if (val == null || val.equals("")) {
				return IDFactory.getDefault().createGUID();
			} else
				return IDFactory.getDefault().createStringID((String) arg);
		} else if (arg instanceof Integer) {
			return IDFactory.getDefault()
					.createGUID(((Integer) arg).intValue());
		} else
			return IDFactory.getDefault().createGUID();
	}

	protected Integer getIntegerFromArg(Object arg)
			throws NumberFormatException {
		if (arg instanceof Integer)
			return (Integer) arg;
		else if (arg != null) {
			return new Integer((String) arg);
		} else
			return new Integer(-1);
	}

	protected class GenericContainerArgs {
		ID id;

		Integer keepAlive;

		public GenericContainerArgs(ID id, Integer keepAlive) {
			this.id = id;
			this.keepAlive = keepAlive;
		}

		public ID getID() {
			return id;
		}

		public Integer getKeepAlive() {
			return keepAlive;
		}
	}

	protected GenericContainerArgs getClientArgs(String[] argDefaults,
			Object[] args) throws IDCreateException {
		ID newID = null;
		Integer ka = null;
		if (argDefaults != null && argDefaults.length > 0) {
			if (argDefaults.length == 2) {
				newID = getIDFromArg(argDefaults[0]);
				ka = getIntegerFromArg(argDefaults[1]);
			} else
				ka = getIntegerFromArg(argDefaults[0]);
		}
		if (args != null && args.length > 0) {
			if (args.length == 2) {
				newID = getIDFromArg(args[0]);
				ka = getIntegerFromArg(args[1]);
			} else
				ka = getIntegerFromArg(args[0]);
		}
		if (newID == null)
			newID = IDFactory.getDefault().createGUID();
		if (ka == null)
			ka = new Integer(0);
		return new GenericContainerArgs(newID, ka);
	}

	protected GenericContainerArgs getServerArgs(String[] argDefaults,
			Object[] args) throws IDCreateException {
		ID newID = null;
		Integer ka = null;
		if (argDefaults != null && argDefaults.length > 0) {
			if (argDefaults.length == 2) {
				newID = getIDFromArg(argDefaults[0]);
				ka = getIntegerFromArg(argDefaults[1]);
			} else
				newID = getIDFromArg(argDefaults[0]);
		}
		if (args != null && args.length > 0) {
			if (args.length == 2) {
				newID = getIDFromArg(args[0]);
				ka = getIntegerFromArg(args[1]);
			} else
				newID = getIDFromArg(args[0]);
		}
		if (newID == null)
			newID = IDFactory.getDefault().createGUID();
		if (ka == null)
			ka = new Integer(0);
		return new GenericContainerArgs(newID, ka);
	}

	public IContainer createInstance(ContainerTypeDescription description,
			Object[] args) throws ContainerCreateException {
		boolean isClient = true;
		if (description.getName().equals(TCPSERVER_NAME)) {
			debug("creating server");
			isClient = false;
		} else {
			debug("creating client");
		}
		try {
			GenericContainerArgs gcargs = null;
			String[] argDefaults = description.getArgDefaults();
			if (isClient)
				gcargs = getClientArgs(argDefaults, args);
			else
				gcargs = getServerArgs(argDefaults, args);
			// new ID must not be null
			if (isClient) {
				return new TCPClientSOContainer(new SOContainerConfig(gcargs
						.getID()), gcargs.getKeepAlive().intValue());
			} else {
				return new TCPServerSOContainer(new SOContainerConfig(gcargs
						.getID()), gcargs.getKeepAlive().intValue());
			}
		} catch (Exception e) {
			traceStack(
					"Exception in GenericContainerInstantiator.createInstance",
					e);
			throw new ContainerCreateException(
					"Exception creating generic container", e);
		}
	}
}