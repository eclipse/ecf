/*
 * Created on Mar 10, 2005
 *
 */
package org.eclipse.ecf.provider.xmpp.container;

import java.io.Serializable;

public class SharedObjectMsg implements Serializable {
    
    private static final long serialVersionUID = 3257002168199360564L;
    String msg;
    String param;
    
    public SharedObjectMsg(String msg, String param) {
        this.msg = msg;
        this.param = param;
    }
    public String getMsg() {
        return msg;
    }
    public String getParam() {
        return param;
    }
}
