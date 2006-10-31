package org.eclipse.ecf.internal.provider;

public interface ECFProviderDebugOptions {

	public static final String DEBUG = ProviderPlugin.PLUGIN_ID + "/debug"; //$NON-NLS-1$

	public static final String EXCEPTIONS_CATCHING = DEBUG
			+ "/exceptions/catching"; //$NON-NLS-1$

	public static final String EXCEPTIONS_THROWING = DEBUG
			+ "/exceptions/throwing"; //$NON-NLS-1$

	public static final String METHODS_ENTERING = DEBUG + "/methods/entering"; //$NON-NLS-1$

	public static final String METHODS_EXITING = DEBUG + "/methods/exiting"; //$NON-NLS-1$

}
