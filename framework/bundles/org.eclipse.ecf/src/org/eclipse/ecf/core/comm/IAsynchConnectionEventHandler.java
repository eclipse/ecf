package org.eclipse.ecf.core.comm;

import java.io.IOException;

public interface IAsynchConnectionEventHandler extends IConnectionEventHandler {
    public void handleAsynchEvent(AsynchConnectionEvent event)
            throws IOException;
}