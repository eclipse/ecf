package org.eclipse.ecf.internal.impl.standalone;

public class Debug {

	public static boolean ON = false;
	protected static Debug staticDebug = null;
	
	public static Debug create(String key) {
		return new Debug(key);
	}
	
	static {
		try {
			staticDebug = Debug.create("org.composent.api.impl.Debug");
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	}
	public static void errDumpStack(Throwable e, String msg) {
		if (staticDebug != null) staticDebug.dumpStack(e,msg);
	}
	public void dumpStack(Throwable e, String msg) {
	    msg(msg);
	    e.printStackTrace(System.err);
	}
	public void msg(String msg) {
	    System.err.println(msg);
	}
	protected Debug(String key) {
	}
	public static void setThreadDebugGroup(Object obj) {
		// Do nothing
	}

}
