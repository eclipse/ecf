package org.eclipse.ecf.provider;

import org.eclipse.core.runtime.Platform;

public class Trace {

	public static boolean ON = Platform.inDebugMode();
	
	public static Trace create(String key) {
		return new Trace(key);
	}
	
	public static void errDumpStack(Throwable e, String msg) {
	    System.err.println(msg);
	    e.printStackTrace(System.err);
	}
	public void dumpStack(Throwable e, String msg) {
	    System.err.println(msg);
	    e.printStackTrace(System.err);
	}
	public void msg(String msg) {
	    System.err.println(msg);
	}
	protected Trace(String str) {
	}
	public static void setThreadDebugGroup(Object obj) {
		// Do nothing
	}

}
