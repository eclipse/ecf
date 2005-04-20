package org.eclipse.ecf.internal.datashare;

import org.eclipse.ecf.core.ISharedObjectConfig;
import org.eclipse.ecf.core.SharedObjectInitException;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.Event;

public interface IBootstrap {

    void setAgent(Agent agent);

    void init(ISharedObjectConfig config) throws SharedObjectInitException;

    void handleEvent(Event event);

    void dispose(ID containerID);

}