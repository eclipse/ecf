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

	protected static ConnectionFactory instance = null;

	static {
		instance = new ConnectionFactory();
	}

	public static ConnectionFactory getDefault() {
		return instance;
	}

	protected ConnectionFactory() {

	}

	public ConnectionTypeDescription addDescription(
			ConnectionTypeDescription scd) {
		String method = "addDescription";
		Trace.entering(ECFPlugin.getDefault(),
				ECFDebugOptions.METHODS_ENTERING, ConnectionFactory.class,
				method, scd);
		ConnectionTypeDescription result = addDescription0(scd);
		Trace.exiting(ECFPlugin.getDefault(), ECFDebugOptions.METHODS_EXITING,
				ConnectionFactory.class, method, result);
		return result;
	}

	protected ConnectionTypeDescription addDescription0(
			ConnectionTypeDescription n) {
		if (n == null)
			return null;
		return (ConnectionTypeDescription) connectiontypes.put(n.getName(), n);
	}

	public boolean containsDescription(ConnectionTypeDescription scd) {
		return containsDescription0(scd);
	}

	protected boolean containsDescription0(ConnectionTypeDescription scd) {
		if (scd == null)
			return false;
		return connectiontypes.containsKey(scd.getName());
	}

	public ConnectionTypeDescription getDescription(
			ConnectionTypeDescription scd) {
		return getDescription0(scd);
	}

	protected ConnectionTypeDescription getDescription0(
			ConnectionTypeDescription scd) {
		if (scd == null)
			return null;
		return (ConnectionTypeDescription) connectiontypes.get(scd.getName());
	}

	protected ConnectionTypeDescription getDescription0(String name) {
		if (name == null)
			return null;
		return (ConnectionTypeDescription) connectiontypes.get(name);
	}

	public ConnectionTypeDescription getDescriptionByName(String name) {
		Trace.entering(ECFPlugin.getDefault(),
				ECFDebugOptions.METHODS_ENTERING, ConnectionFactory.class,
				"getDescriptionByName", name);
		ConnectionTypeDescription result = getDescription0(name);
		Trace.exiting(ECFPlugin.getDefault(), ECFDebugOptions.METHODS_EXITING,
				ConnectionFactory.class, "getDescriptionByName", result);
		return result;
	}

	public List getDescriptions() {
		return getDescriptions0();
	}

	protected List getDescriptions0() {
		return new ArrayList(connectiontypes.values());
	}

	public ISynchAsynchConnection createSynchAsynchConnection(
			ISynchAsynchEventHandler handler, ConnectionTypeDescription desc,
			Object[] args) throws ConnectionCreateException {
		if (handler == null)
			throw new ConnectionCreateException("handler cannot be null");
		return createSynchAsynchConnection(handler, desc, null, args);
	}

	protected void throwConnectionCreateException(String message,
			Throwable cause, String method) throws ConnectionCreateException {
		ConnectionCreateException except = (cause == null) ? new ConnectionCreateException(
				message)
				: new ConnectionCreateException(message, cause);
		Trace.throwing(ECFPlugin.getDefault(),
				ECFDebugOptions.EXCEPTIONS_THROWING, ConnectionFactory.class,
				method, except);
		throw except;
	}

	public ISynchAsynchConnection createSynchAsynchConnection(
			ISynchAsynchEventHandler handler, ConnectionTypeDescription desc,
			String[] argTypes, Object[] args) throws ConnectionCreateException {
		String method = "createSynchAsynchConnection";
		Trace.entering(ECFPlugin.getDefault(),
				ECFDebugOptions.METHODS_ENTERING, ConnectionFactory.class,
				method, new Object[] { handler, desc,
						Trace.getArgumentsString(argTypes),
						Trace.getArgumentsString(args) });
		if (handler == null)
			throwConnectionCreateException(
					"ISynchAsynchEventHandler cannot be null", null, method);
		if (desc == null)
			throwConnectionCreateException(
					"ConnectionTypeDescription cannot be null", null, method);
		ConnectionTypeDescription cd = desc;
		if (cd == null)
			throwConnectionCreateException("ConnectionTypeDescription '"
					+ desc.getName() + "' not found", null, method);
		ISynchAsynchConnectionInstantiator instantiator = null;
		Class clazzes[] = null;
		try {
			instantiator = (ISynchAsynchConnectionInstantiator) cd
					.getInstantiator();
			clazzes = AbstractFactory.getClassesForTypes(argTypes, args, cd
					.getClassLoader());
		} catch (Exception e) {
			throwConnectionCreateException(
					"createSynchAsynchConnection exception with description:"
							+ desc, e, method);
		}
		if (instantiator == null)
			throwConnectionCreateException(
					"Instantiator for ConnectionTypeDescription '"
							+ cd.getName() + "' is null", null, method);
		// Ask instantiator to actually create instance
		ISynchAsynchConnection result = instantiator.createInstance(desc,
				handler, clazzes, args);
		if (result == null)
			throwConnectionCreateException("Instantiator returned null for '"
					+ cd.getName() + "'", null, method);
		Trace.exiting(ECFPlugin.getDefault(), ECFDebugOptions.METHODS_EXITING,
				ConnectionFactory.class, method, result);
		return result;

	}

	public ISynchAsynchConnection createSynchAsynchConnection(
			ISynchAsynchEventHandler handler, String descriptionName)
			throws ConnectionCreateException {
		return createSynchAsynchConnection(handler,
				getDescriptionByName(descriptionName), null, null);
	}

	public ISynchAsynchConnection createSynchAsynchConnection(
			ISynchAsynchEventHandler handler, String descriptionName,
			Object[] args) throws ConnectionCreateException {
		return createSynchAsynchConnection(handler,
				getDescriptionByName(descriptionName), args);
	}

	public ISynchAsynchConnection createSynchAsynchConnection(
			ISynchAsynchEventHandler handler, String descriptionName,
			String[] argTypes, Object[] args) throws ConnectionCreateException {
		return createSynchAsynchConnection(handler,
				getDescriptionByName(descriptionName), argTypes, args);
	}

	public ConnectionTypeDescription removeDescription(
			ConnectionTypeDescription scd) {
		String method = "removeDescription";
		Trace.entering(ECFPlugin.getDefault(),
				ECFDebugOptions.METHODS_ENTERING, ConnectionFactory.class,
				method, scd);
		ConnectionTypeDescription result = removeDescription0(scd);
		Trace.exiting(ECFPlugin.getDefault(), ECFDebugOptions.METHODS_EXITING,
				ConnectionFactory.class, method, scd);
		return result;
	}

	protected ConnectionTypeDescription removeDescription0(
			ConnectionTypeDescription n) {
		if (n == null)
			return null;
		return (ConnectionTypeDescription) connectiontypes.remove(n.getName());
	}
}