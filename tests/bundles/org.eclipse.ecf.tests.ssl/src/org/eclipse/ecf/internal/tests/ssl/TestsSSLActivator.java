package org.eclipse.ecf.internal.tests.ssl;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class TestsSSLActivator implements BundleActivator {

	private static final String TEST_FILES_DIR = "test_files/";

	private static TestsSSLActivator plugin;

	private BundleContext context;

	public static TestsSSLActivator getDefault() {
		return plugin;
	}

	public BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		plugin = this;
		this.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		this.context = null;
	}

	public File getTestEntryFile(String entryPath) throws IOException {
		URL entryURL = context.getBundle().getEntry(TEST_FILES_DIR + entryPath);
		if (entryURL == null) {
			return null;
		}
		return new File(FileLocator.toFileURL(entryURL).toExternalForm().substring(5));
	}

}
