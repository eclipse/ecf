/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.provider;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.core.runtime.Platform;

public class Trace {
    public static final String tracePrefix = "(trace)";
    
    public static boolean ON = true;
    protected static boolean isEclipse = false;
    protected static String pluginName = "";
    protected static String debugPrefix = "/debug/";
    protected static Trace errTrace = new Trace("org.eclipse.ecf.provider.Trace.err");
    
    static {
        try {
            ON = Platform.inDebugMode();
            String bundleName = ProviderPlugin.getDefault().getBundle().getSymbolicName();
            String val = System.getProperty(bundleName+".Trace");
            if (val != null) {
                setTrace(true);
                isEclipse = false;
                // No eclipse Platform available
                System.out.println("WARNING:  Eclipse platform not being use for trace...overridden by system property org.eclipse.ecf.provider.Trace");                    
            } else {
                isEclipse = true;
                pluginName = bundleName;
            }
        } catch (Exception e) {
            try {
                String val = System.getProperty("org.eclipse.ecf.provider.Trace");
                if (val != null) {
                    setTrace(true);
                    isEclipse = false;
                    // No eclipse Platform available
                    System.out.println("WARNING:  Eclipse platform not available for trace...using system.out for org.eclipse.ecf.provider");                    
                } else {
                    System.out.println(Trace.class.getName()+": OFF");
                }
            } catch (Exception except) {
            }
        }
    }

    public static void setTrace(boolean on) {
        ON = on;
    }

    public static Trace create(String key) {
        if (isEclipse) {
            String res = "";
            try {
                res = Platform.getDebugOption(pluginName + debugPrefix + key);
            } catch (Exception e) {
                // ignore...this means that the Platform class not found.
            }
            if (res != null) {
                Boolean on = new Boolean(res);
                if (on.booleanValue())
                    return new Trace(pluginName + "(" + key + ")");
                else
                    return null;
            } else {
                return null;
            }
        } else
            return new Trace(key);
    }

    String name;

    public void dumpStack(Throwable e, String msg) {
        msg(msg);
        e.printStackTrace(System.err);
    }

    public static void errDumpStack(Throwable e, String msg) {
    	errTrace.dumpStack(e,msg);
    }
    public void msg(String msg) {
        StringBuffer sb = new StringBuffer(name);
        sb.append(getTimeString()).append(msg);
        System.out.println(sb.toString());
    }
    public static void errMsg(String msg) {
    	errTrace.msg(msg);
    }
    protected static String getTimeString() {
        Date d = new Date();
        SimpleDateFormat df = new SimpleDateFormat("[MM/dd/yy;HH:mm:ss:SSS]");
        return df.format(d);
    }

    protected Trace(String str) {
        name = tracePrefix+str;
    }

}