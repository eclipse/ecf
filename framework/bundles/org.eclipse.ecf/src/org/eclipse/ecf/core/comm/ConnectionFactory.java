package org.eclipse.ecf.core.comm;

import java.util.Hashtable;

import org.eclipse.ecf.core.comm.provider.ISynchAsynchConnectionInstantiator;
import org.eclipse.ecf.core.util.AbstractFactory;

public class ConnectionFactory {

    private static Hashtable connectiontypes = new Hashtable();

    static {
        ConnectionDescription cd = new ConnectionDescription(
                (ClassLoader) null, "default",
                "org.eclipse.ecf.provider.comm.tcp.Client$Creator","default connection");
        addDescription(cd);
    }
    public final static ConnectionDescription getDescription(
            ConnectionDescription scd) {
        return getDescription0(scd);
    }
    protected static ConnectionDescription getDescription0(
            ConnectionDescription scd) {
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
    public final static ConnectionDescription removeDescription(
            ConnectionDescription scd) {
        return removeDescription0(scd);
    }

    protected static ConnectionDescription removeDescription0(
            ConnectionDescription n) {
        if (n == null)
            return null;
        return (ConnectionDescription) connectiontypes.remove(n.getName());
    }
    public final static ConnectionDescription addDescription(
            ConnectionDescription scd) {
        return addDescription0(scd);
    }

    protected static ConnectionDescription addDescription0(
            ConnectionDescription n) {
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

    public static ISynchAsynchConnection makeSynchAsynchConnection(
            ISynchAsynchConnectionEventHandler handler,
            ConnectionDescription desc, String[] argTypes, Object[] args)
            throws ConnectionInstantiationException {

        if (handler == null)
            throw new ConnectionInstantiationException("handler cannot be null");
        if (desc == null)
            throw new ConnectionInstantiationException(
                    "ConnectionDescription cannot be null");
        ConnectionDescription cd = getDescription0(desc);
        if (cd == null)
            throw new ConnectionInstantiationException("ConnectionDescription "
                    + desc.getName() + " not found");
        ISynchAsynchConnectionInstantiator instantiator = null;
        Class clazzes[] = null;
        try {
            instantiator = (ISynchAsynchConnectionInstantiator) cd.getInstantiator();
            clazzes = AbstractFactory.getClassesForTypes(argTypes, args, cd
                    .getClassLoader());
            if (instantiator == null)
                throw new InstantiationException(
                        "Instantiator for ConnectionDescription "
                                + cd.getName() + " is null");
        } catch (Exception e) {
            throw new ConnectionInstantiationException(
                    "Exception getting instantiator for '" + desc.getName()
                            + "'", e);
        }
        // Ask instantiator to actually create instance
        return instantiator.makeInstance(handler, clazzes, args);
    }
    public static ISynchAsynchConnection makeSynchAsynchConnection(
            ISynchAsynchConnectionEventHandler handler,
            ConnectionDescription desc, Object[] args)
            throws ConnectionInstantiationException {
        return makeSynchAsynchConnection(handler, desc, null, args);
    }
    public static ISynchAsynchConnection makeSynchAsynchConnection(
            ISynchAsynchConnectionEventHandler handler, String descriptionName,
            Object[] args) throws ConnectionInstantiationException {
        ConnectionDescription desc = getDescriptionByName(descriptionName);
        if (desc == null)
            throw new ConnectionInstantiationException("Connection named '"
                    + descriptionName + "' not found");
        return makeSynchAsynchConnection(handler, desc, args);
    }
    public static ISynchAsynchConnection makeSynchAsynchConnection(
            ISynchAsynchConnectionEventHandler handler, String descriptionName,
            String[] argTypes, Object[] args)
            throws ConnectionInstantiationException {
        ConnectionDescription desc = getDescriptionByName(descriptionName);
        if (desc == null)
            throw new ConnectionInstantiationException("Connection named '"
                    + descriptionName + "' not found");
        return makeSynchAsynchConnection(handler, desc, argTypes, args);
    }
    public static ISynchAsynchConnection makeSynchAsynchConnection(
            ISynchAsynchConnectionEventHandler handler, String descriptionName)
            throws ConnectionInstantiationException {
        ConnectionDescription desc = getDescriptionByName(descriptionName);
        if (desc == null)
            throw new ConnectionInstantiationException("Connection named '"
                    + descriptionName + "' not found");
        return makeSynchAsynchConnection(handler, desc, null, null);
    }

}