package org.eclipse.ecf.core.comm;

import java.io.IOException;

import org.eclipse.ecf.core.identity.ID;

public interface ISynchConnection extends IConnection {

    public Object sendSynch(ID receiver, byte[] data) throws IOException;
}