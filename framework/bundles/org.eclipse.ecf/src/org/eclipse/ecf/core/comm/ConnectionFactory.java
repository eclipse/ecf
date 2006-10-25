/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.comm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.eclipse.ecf.core.comm.provider.ISynchAsynchConnectionInstantiator;
import org.eclipse.ecf.core.util.AbstractFactory;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.core.ECFDebugOptions;
import org.eclipse.ecf.internal.core.ECFPlugin;

public class ConnectionFactory {
	private static Hashtable connectiontypes = new Hashtable();

	public final static ConnectionTypeDescription addDescription(
			ConnectionTypeDescription scd) {
		debug("addDescription(" + scd + ")");
		return addDescription0(scd);
	}

	protected static ConnectionTypeDescription addDescription0(
			ConnectionTypeDescription n) {
		if (n == null)
			return null;
		return (ConnectionTypeDescription) connectiontypes.put(n.getName(), n);
	}

	public final static boolean containsDescription(ConnectionTypeDescription scd) {
		return containsDescription0(scd);
	}

	protected static boolean containsDescription0(ConnectionTypeDescription scd) {
		if (scd == null)
			return false;
		return connectiontypes.containsKey(scd.getName());
	}

	private static void debug(String msg) {
		Trace.trace(ECFPlugin.getDefault(),msg);
	}
	
	private static void dumpStack(String msg, Throwable e) {
		Trace.catching(ECFPlugin.getDefault(), ECFDebugOptions.EXCEPTIONS_CATCHING, ConnectionFactory.class, "dumpStack", e);
	}

	public final static ConnectionTypeDescription getDescription(
			ConnectionTypeDescription scd) {
		return getDescription0(scd);
	}

	protected static ConnectionTypeDescription getDescription0(
			ConnectionTypeDescription scd) {
		if (scd == null)
			return null;
		return (ConnectionTypeDescription) connectiontypes.get(scd.getName());
	}

	protected static ConnectionTypeDescription getDescription0(String name) {
		if (name == null)
			return null;
		return (ConnectionTypeDescription) connectiontypes.get(name);
	}

	public final static ConnectionTypeDescription getDescriptionByName(String name) {
		return getDescription0(name);
	}

	public static final List getDescriptions() {
		return getDescriptions0();
	}

	protected static List getDescriptions0() {
		return new ArrayList(connectiontypes.values());
	}

	public static ISynchAsynchConnection createSynchAsynchConnection(
			ISynchAsynchConnectionEventHandler handler,
			ConnectionTypeDescription desc, Object[] args)
			throws ConnectionInstantiationException {
		if (handler == null)
			throw new ConnectionInstantiationException("handler cannot be null");
		return createSynchAsynchConnection(handler, desc, null, args);
	}

	public static ISynchAsynchConnection createSynchAsynchConnection(
			ISynchAsynchConnectionEventHandler handler,
			ConnectionTypeDescription desc, String[] argTypes, Object[] args)
			throws ConnectionInstantiationException {
		debug("createSynchAsynchConnection(" + handler + "," + desc + ","
				+ Trace.getArgumentsString(argTypes) + ","
				+ Trace.getArgumentsString(args) + ")");
		if (handler == null)
			throw new ConnectionInstantiationException(
					"ISynchAsynchConnectionEventHandler cannot be null");
		if (desc == null)
			throw new ConnectionInstantiationException(
					"ConnectionTypeDescription cannot be null");
		ConnectionTypeDescription cd = desc;
		if (cd == null)
			throw new ConnectionInstantiationException("ConnectionTypeDescription "
					+ desc.getName() + " not found");
		ISynchAsynchConnectionInstantiator instantiator = null;
		Class clazzes[] = null;
		try {
			instantiator = (ISynchAsynchConnectionInstantiator) cd
					.getInstantiator();
			clazzes = AbstractFactory.getClassesForTypes(argTypes, args, cd
					.getClassLoader());
			if (instantiator == null)
				throw new InstantiationException(
						"Instantiator for ConnectionTypeDescription "
								+ cd.getName() + " is null");
		} catch (Exception e) {
			ConnectionInstantiationException newexcept = new ConnectionInstantiationException(
					"createSynchAsynchConnection exception with description: "
							+ desc + ": " + e.getClass().getName() + ": "
							+ e.getMessage());
			newexcept.setStackTrace(e.getStackTrace());
			dumpStack("Exception in createSynchAsynchConnection", newexcept);
			throw newexcept;
		}
		// Ask instantiator to actually create instance
		return instantiator.createInstance(desc, handler, clazzes, args);
	}

	public static ISynchAsynchConnection createSynchAsynchConnection(
			ISynchAsynchConnectionEventHandler handler, String descriptionName)
			throws ConnectionInstantiationException {
		if (handler == null)
			throw new ConnectionInstantiationException(
					"ISynchAsynchConnectionEventHandler cannot be null");
		ConnectionTypeDescription desc = getDescriptionByName(descriptionName);
		if (desc == null)
			throw new ConnectionInstantiationException(
					"Connection type named '" + descriptionName + "' not found");
		return createSynchAsynchConnection(handler, desc, null, null);
	}

	public static ISynchAsynchConnection createSynchAsynchConnection(
			ISynchAsynchConnectionEventHandler handler, String descriptionName,
			Object[] args) throws ConnectionInstantiationException {
		if (handler == null)
			throw new ConnectionInstantiationException(
					"ISynchAsynchConnectionEventHandler cannot be null");
		ConnectionTypeDescription desc = getDescriptionByName(descriptionName);
		if (desc == null)
			throw new ConnectionInstantiationException(
					"Connection type named '" + descriptionName + "' not found");
		return createSynchAsynchConnection(handler, desc, args);
	}

	public static ISynchAsynchConnection createSynchAsynchConnection(
			ISynchAsynchConnectionEventHandler handler, String descriptionName,
			String[] argTypes, Object[] args)
			throws ConnectionInstantiationException {
		if (handler == null)
			throw new ConnectionInstantiationException(
					"ISynchAsynchConnectionEventHandler cannot be null");
		ConnectionTypeDescription desc = getDescriptionByName(descriptionName);
		if (desc == null)
			throw new ConnectionInstantiationException(
					"Connection type named '" + descriptionName + "' not found");
		return createSynchAsynchConnection(handler, desc, argTypes, args);
	}

	public final static ConnectionTypeDescription removeDescription(
			ConnectionTypeDescription scd) {
		debug("removeDescription(" + scd + ")");
		return removeDescription0(scd);
	}

	protected static ConnectionTypeDescription removeDescription0(
			ConnectionTypeDescription n) {
		if (n == null)
			return null;
		return (ConnectionTypeDescription) connectiontypes.remove(n.getName());
	}
}