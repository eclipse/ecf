package org.eclipse.ecf.core.comm.provider;

import org.eclipse.ecf.core.comm.ConnectionInstantiationException;
import org.eclipse.ecf.core.comm.ISynchAsynchConnection;
import org.eclipse.ecf.core.comm.ISynchAsynchConnectionEventHandler;

public interface ISynchAsynchConnectionInstantiator {
    public ISynchAsynchConnection makeInstance(ISynchAsynchConnectionEventHandler handler, Class [] clazzes, Object [] args) throws ConnectionInstantiationException;
}
