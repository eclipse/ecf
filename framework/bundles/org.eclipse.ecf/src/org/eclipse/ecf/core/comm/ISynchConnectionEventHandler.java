package org.eclipse.ecf.core.comm;

import java.io.IOException;

public interface ISynchConnectionEventHandler extends IConnectionEventHandler {

    public Object handleSynchEvent(SynchConnectionEvent event)
            throws IOException;
}