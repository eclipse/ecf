package org.eclipse.ecf.provider.comm.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.eclipse.ecf.provider.Trace;

public class ExObjectInputStream extends ObjectInputStream {

    private boolean replace = false;

    public static final Trace debug = Trace.create("connection");

    public ExObjectInputStream(InputStream in) throws IOException,
            SecurityException {
        super(in);
    }

    public ExObjectInputStream(InputStream in, boolean backwardCompatibility)
            throws IOException, SecurityException {
        super(in);
        if (backwardCompatibility) {
            try {
                super.enableResolveObject(true);
                replace = true;
                debug("resolveObject");
            } catch (Exception e) {
                throw new IOException(
                        "Could not setup backward compatibility object replacers for ExObjectInputStream");
            }
        }
    }

    protected void debug(String msg) {
        if (Trace.ON && debug != null) {
            debug.msg(msg);
        }    	
    }
    protected void dumpStack(String msg, Throwable e) {
        if (Trace.ON && debug != null) {
            debug.dumpStack(e,msg);
        }    	
    }
}