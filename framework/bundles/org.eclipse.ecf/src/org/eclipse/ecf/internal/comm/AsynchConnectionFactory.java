package org.eclipse.ecf.internal.comm;

import java.util.Hashtable;

import org.eclipse.ecf.core.util.AbstractFactory;

public class AsynchConnectionFactory {

	private static Hashtable connectiontypes = new Hashtable();

	static {
	    /*
		AsynchConnectionDescription cdtcp =
			new AsynchConnectionDescription(
				TCPReliableConnection.class.getName(),
				"1.0.0",
				TCPReliableConnection.Creator.class.getName());
		addDescription(cdtcp);
		
		AsynchConnectionDescription cdjms =
			new AsynchConnectionDescription(
				JMSClientReliableConnection.class.getName(),
				"1.0.0",
				JMSClientReliableConnection.Creator.class.getName());
		addDescription(cdjms);
		
		
		AsynchConnectionDescription cdxmpp =
			new AsynchConnectionDescription(
				SmackConnection.class.getName(),
				"1.0.0",
				SmackConnection.Creator.class.getName());
		addDescription(cdxmpp);
		*/
	}
	
	public final static AsynchConnectionDescription getDescription(AsynchConnectionDescription scd) {
		return getDescription0(scd);
	}
	protected static AsynchConnectionDescription getDescription0(AsynchConnectionDescription scd) {
		if (scd == null)
			return null;
		return (AsynchConnectionDescription) connectiontypes.get(scd.getName());
	}
	protected static AsynchConnectionDescription getDescription0(String name) {
		if (name == null)
			return null;
		return (AsynchConnectionDescription) connectiontypes.get(name);
	}
	public final static AsynchConnectionDescription getDescriptionByName(String name) {
		return getDescription0(name);
	}
	public final static AsynchConnectionDescription removeDescription(AsynchConnectionDescription scd) {
		return removeDescription0(scd);
	}

	protected static AsynchConnectionDescription removeDescription0(AsynchConnectionDescription n) {
		if (n == null)
			return null;
		return (AsynchConnectionDescription) connectiontypes.remove(n.getName());
	}
	public final static AsynchConnectionDescription addDescription(AsynchConnectionDescription scd) {
		return addDescription0(scd);
	}

	protected static AsynchConnectionDescription addDescription0(AsynchConnectionDescription n) {
		if (n == null)
			return null;
		return (AsynchConnectionDescription) connectiontypes.put(n.getName(), n);
	}
	public final static boolean containsDescription(AsynchConnectionDescription scd) {
		return containsDescription0(scd);
	}
	protected static boolean containsDescription0(AsynchConnectionDescription scd) {
		if (scd == null)
			return false;
		return connectiontypes.containsKey(scd.getName());
	}

	public static IAsynchConnection makeAsynchConnection(AsynchConnectionDescription desc,
		String[] argTypes,
		Object[] args)
		throws ConnectionInstantiationException {
        
		if (desc == null)
			throw new ConnectionInstantiationException("AsynchConnectionDescription cannot be null");
		AsynchConnectionDescription cd = getDescription0(desc);
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
	public static IAsynchConnection makeAsynchConnection(
		AsynchConnectionDescription desc,
		Object[] args)
		throws ConnectionInstantiationException {
		return makeAsynchConnection(desc, null, args);
	}
	public static IAsynchConnection makeAsynchConnection(
		String descriptionName,
		Object[] args)
		throws ConnectionInstantiationException {
		return makeAsynchConnection(
			getDescriptionByName(descriptionName),
			args);
	}
	public static IAsynchConnection makeAsynchConnection(
		String descriptionName,
		String[] argTypes,
		Object[] args)
		throws ConnectionInstantiationException {
		return makeAsynchConnection(
			getDescriptionByName(descriptionName),
			argTypes,
			args);
	}
	public static IAsynchConnection makeAsynchConnection(
			String descriptionName)
			throws ConnectionInstantiationException {
			return makeAsynchConnection(
				getDescriptionByName(descriptionName),
				null,
				null);
		}

}
