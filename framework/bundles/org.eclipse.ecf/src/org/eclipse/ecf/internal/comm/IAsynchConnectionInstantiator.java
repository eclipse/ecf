package org.eclipse.ecf.internal.comm;

import org.eclipse.ecf.internal.comm.ConnectionInstantiationException;
import org.eclipse.ecf.internal.comm.IAsynchConnection;

public interface IAsynchConnectionInstantiator {
    public IAsynchConnection makeInstance(Class [] clazzes, Object [] args) throws ConnectionInstantiationException;
}
