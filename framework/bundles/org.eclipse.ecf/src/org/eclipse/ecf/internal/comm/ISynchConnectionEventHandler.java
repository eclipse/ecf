package org.eclipse.ecf.internal.comm;

import java.io.IOException;

public interface ISynchConnectionEventHandler extends IConnectionEventHandler {

	public Object handleSynchEvent(ConnectionEvent event) throws IOException;
}
