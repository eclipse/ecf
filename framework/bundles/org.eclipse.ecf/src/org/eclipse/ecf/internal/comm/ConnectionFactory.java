package org.eclipse.ecf.internal.comm;

import java.util.Hashtable;

import org.eclipse.ecf.core.util.AbstractFactory;

public class ConnectionFactory {

	private static Hashtable connectiontypes = new Hashtable();

	public final static ConnectionDescription getDescription(ConnectionDescription scd) {
		return getDescription0(scd);
	}
	protected static ConnectionDescription getDescription0(ConnectionDescription scd) {
		if (scd == null)
			return null;
		return (ConnectionDescription) connectiontypes.get(scd.getName());
	}
	protected static ConnectionDescription getDescription0(String name) {
		if (name == null)
			return null;
		return (ConnectionDescription) connectiontypes.get(name);
	}
	public final static ConnectionDescription getDescriptionByName(String name) {
		return getDescription0(name);
	}
	public final static ConnectionDescription removeDescription(ConnectionDescription scd) {
		return removeDescription0(scd);
	}

	protected static ConnectionDescription removeDescription0(ConnectionDescription n) {
		if (n == null)
			return null;
		return (ConnectionDescription) connectiontypes.remove(n.getName());
	}
	public final static ConnectionDescription addDescription(ConnectionDescription scd) {
		return addDescription0(scd);
	}

	protected static ConnectionDescription addDescription0(ConnectionDescription n) {
		if (n == null)
			return null;
		return (ConnectionDescription) connectiontypes.put(n.getName(), n);
	}
	public final static boolean containsDescription(ConnectionDescription scd) {
		return containsDescription0(scd);
	}
	protected static boolean containsDescription0(ConnectionDescription scd) {
		if (scd == null)
			return false;
		return connectiontypes.containsKey(scd.getName());
	}

	public static ISynchAsynchConnection makeSynchAsynchConnection(ConnectionDescription desc,
		String[] argTypes,
		Object[] args)
		throws ConnectionInstantiationException {
        
		if (desc == null)
			throw new ConnectionInstantiationException("AsynchConnectionDescription cannot be null");
		ConnectionDescription cd = getDescription0(desc);
		if (cd == null)
			throw new ConnectionInstantiationException(
				"AsynchConnectionDescription " + desc.getName() + " not found");
		IAsynchConnectionInstantiator instantiator = null;
		Class clazzes[] = null;
		try {
		    instantiator = (IAsynchConnectionInstantiator) cd.getInstantiator();
		    clazzes = AbstractFactory.getClassesForTypes(argTypes, args, cd.getClassLoader());
		    if (instantiator == null)
		        throw new InstantiationException(
				"Instantiator for AsynchConnectionDescription "
					+ cd.getName()
					+ " is null");
		} catch (Exception e) {
		    throw new ConnectionInstantiationException("Exception getting instantiator for '"+desc.getName()+"'",e);
		}
		// Ask instantiator to actually create instance
		return instantiator.makeInstance(clazzes, args);
	}
	public static ISynchAsynchConnection makeSynchAsynchConnection(
		ConnectionDescription desc,
		Object[] args)
		throws ConnectionInstantiationException {
		return makeSynchAsynchConnection(desc, null, args);
	}
	public static ISynchAsynchConnection makeSynchAsynchConnection(
		String descriptionName,
		Object[] args)
		throws ConnectionInstantiationException {
		return makeSynchAsynchConnection(
			getDescriptionByName(descriptionName),
			args);
	}
	public static ISynchAsynchConnection makeAsynchConnection(
		String descriptionName,
		String[] argTypes,
		Object[] args)
		throws ConnectionInstantiationException {
		return makeSynchAsynchConnection(
			getDescriptionByName(descriptionName),
			argTypes,
			args);
	}
	public static ISynchAsynchConnection makeAsynchConnection(
			String descriptionName)
			throws ConnectionInstantiationException {
			return makeSynchAsynchConnection(
				getDescriptionByName(descriptionName),
				null,
				null);
		}

}
