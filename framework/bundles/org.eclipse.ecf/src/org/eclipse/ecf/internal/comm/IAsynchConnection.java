package org.eclipse.ecf.internal.comm;

import java.io.IOException;

import org.eclipse.ecf.core.identity.ID;

public interface IAsynchConnection extends IConnection {

	public void sendAsynch(ID receiver, byte [] data) throws IOException;
	public void sendAsynch(ID receiver, Object data) throws IOException;
}
