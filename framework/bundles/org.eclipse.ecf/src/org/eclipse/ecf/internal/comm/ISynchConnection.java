package org.eclipse.ecf.internal.comm;

import java.io.IOException;

import org.eclipse.ecf.core.identity.ID;

public interface ISynchConnection extends IConnection {

	public Object sendSynch(ID receiver, byte [] data) throws IOException;
	public Object sendSynch(ID receiver, Object data) throws IOException;
}
