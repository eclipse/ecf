package org.eclipse.ecf.internal.comm;

import org.eclipse.ecf.internal.comm.ConnectionInstantiationException;

public interface IAsynchConnectionInstantiator {
    public ISynchAsynchConnection makeInstance(Class [] clazzes, Object [] args) throws ConnectionInstantiationException;
}
