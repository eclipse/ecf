package org.eclipse.ecf.core.comm;

public interface IConnectionEventHandler {

    public boolean handleSuspectEvent(ConnectionEvent event);
    public void handleDisconnectEvent(DisconnectConnectionEvent event);
    public Object getAdapter(Class clazz);
}