package org.eclipse.ecf.provider;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.Platform;

public class Trace {

	public static boolean ON = false;
	protected static boolean isEclipse = false;
	protected static String pluginName = "";
	protected static String debugPrefix = "/debug/";
	
	static {
		try {
			ON = Platform.inDebugMode();
			isEclipse = true;
			pluginName = ProviderPlugin.getDefault().getBundle().getSymbolicName();
		} catch (Exception e) {
			// No eclipse Platform available
		}
	}
	
	public static void setTrace(boolean on) {
		ON = on;
	}
	
	public static Trace create(String key) {
		if (isEclipse) {
			String res = Platform.getDebugOption(pluginName+debugPrefix+key);
			if (res != null) {
				Boolean on = new Boolean(res);
				if (on.booleanValue()) return new Trace(pluginName+"("+key+")");
				else return null;
			} else {
				return null;
			}
		} else return new Trace(key);
	}
	
	String name;
	
	public void dumpStack(Throwable e, String msg) {
	    msg(msg);
	    e.printStackTrace(System.err);
	}
	public void msg(String msg) {
	    System.err.println(name+"["+getTimeString()+"]"+msg);
	}
	
	protected static String getTimeString() {
		Date d = new Date();
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy;HH:mm:ss:SSS");
		return df.format(d);
	}
	protected Trace(String str) {
		name = str;
	}
	public static void setThreadDebugGroup(Object obj) {
		// Do nothing
	}

}
