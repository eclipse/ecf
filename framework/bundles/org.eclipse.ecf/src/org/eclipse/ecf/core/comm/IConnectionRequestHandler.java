package org.eclipse.ecf.core.comm;

public interface IConnectionRequestHandler {
    public Object checkConnect(String hostname, Object data, IConnection conn)
            throws Exception;
}