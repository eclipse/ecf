/**
 * Copyright (c) 2006 Parity Communications, Inc. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sergey Yakovlev - initial API and implementation
 */
package org.eclipse.ecf.provider.rss;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.core.runtime.Platform;

/**
 * A Trace object is used to log messages for a specific system or application component. 
 * Traces are normally named, using a hierarchical dot-separated namespace. 
 *  
 * @author Sergey Yakovlev
 *
 */
public class Trace {
    public static final String tracePrefix = "(trace)";
    
    public static boolean ON = false;
    protected static boolean isEclipse = false;
    protected static String pluginName = "";
    protected static String debugPrefix = "/debug/";
    static {
        try {
            ON = Platform.inDebugMode();
            String val = System.getProperty(RssPlugin.PLUGIN_ID+".Trace");
            if (val != null) {
                setTrace(true);
                isEclipse = false;
                // No eclipse Platform available
                System.out.println("WARNING:  Eclipse platform not available for trace...overridden by system property org.eclipse.ecf.Trace");
            } else {
                isEclipse = true;
                pluginName = RssPlugin.PLUGIN_ID;
            }
        } catch (Exception e) {
            try {
                String val = System.getProperty(RssPlugin.PLUGIN_ID+".Trace");
                if (val != null) {
                    setTrace(true);
                    isEclipse = false;
                    // No eclipse Platform available
                    System.out.println("WARNING:  Eclipse platform not available for trace...using system.out for org.eclipse.ecf");
                } else {
                    System.out.println(Trace.class.getName()+": OFF");
                }
            } catch (Exception except) {
            }
        }
    }
    
    /**
     * Turns on/off logging.
     * @param on true to enable log mode; false to disable it. 
     */
    public static void setTrace(boolean on) {
        ON = on;
    }

    /**
     * Static method to create a Trace instance for a named subsystem.
     * @param key A name for the Trace object.
     * @return a new Trace instance.
     */
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
                if(on.booleanValue()) {
                    return new Trace(pluginName + "(" + key + ")");
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return new Trace(key);
        }
    }

    /**
     * A name for the Trace object. 
     */
    String name;

    /**
     * Log a message, with associated Throwable information.
     * @param e Throwable associated with log message.
     * @param msg The string message.
     */
    public void dumpStack(Throwable e, String msg) {
        msg(msg);
        synchronized(System.err) {
        	e.printStackTrace(System.err);
        }
    }

    /**
     * Log a message.
     * @param msg The string message.
     */
    public void msg(String msg) {
        StringBuffer sb = new StringBuffer(name);
        sb.append(getTimeString()).append(msg);
        synchronized(System.out) {
        	System.out.println(sb.toString());
        }
    }

    /**
     * Returns the current time as a formatted date-time string.
     * @return The formatted date-time string.
     */
    protected static String getTimeString() {
        Date d = new Date();
        SimpleDateFormat df = new SimpleDateFormat("[MM/dd/yy;HH:mm:ss:SSS]");
        return df.format(d);
    }

    /**
     * Protected method to construct a Trace object for a named subsystem.
     * @param str A name for the Trace object.
     */
    protected Trace(String str) {
        name = tracePrefix+str;
    }
    
    /**
     * Converts a String array to a String.
     * @param strings a String array.
     * @return if the argument is null, then a string equal to ""; otherwise, the string representation of the String[] is returned.
     */
    public static String convertStringAToString(String [] strings) {
        if(strings == null) {
        	return "";
        }
        StringBuffer sb = new StringBuffer();
        for(int i=0; i < strings.length; i++) {
            if(strings[i]==null) {
            	sb.append("(null)");
            } else {
            	sb.append(strings[i]);
            }
            if(i != (strings.length-1)) {
            	sb.append(";");
            }
        }
        return sb.toString();
    }
    
    /**
     * Converts an Object array to a String.
     * @param objs an Object array.
     * @return if the argument is null, then a string equal to ""; otherwise, the string representation of the Object[] is returned.
     */
    public static String convertObjectAToString(Object [] objs) {
        if(objs == null) {
        	return "";
        }
        StringBuffer sb = new StringBuffer();
        for(int i=0; i < objs.length; i++) {
            if(objs[i]==null) {
            	sb.append("(null)");
            } else {
            	sb.append(objs[i].toString());
            }
            if(i != (objs.length-1)) {
            	sb.append(";");
            }
        }
        return sb.toString();
    }

    public static void setThreadDebugGroup(Object obj) {
        // Do nothing
    }
}