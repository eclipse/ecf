package org.eclipse.ecf.internal.comm;


public interface IConnectionRequestHandler
{
    public Object checkConnect(String hostname, Object data, IConnection conn) throws Exception;
}