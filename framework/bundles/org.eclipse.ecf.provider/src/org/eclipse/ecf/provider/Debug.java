package org.eclipse.ecf.provider;

public class Debug {

	public static boolean ON = false;
	
	public static Debug create(String key) {
		return new Debug(key);
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
	protected Debug(String str) {
	}
	public static void setThreadDebugGroup(Object obj) {
		// Do nothing
	}

}
