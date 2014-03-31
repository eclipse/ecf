package com.mycorp.examples.internal.timeservice.provider.rest.common;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.util.ExtensionRegistryRunnable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.mycorp.examples.timeservice.provider.rest.common.TimeServiceRestNamespace;

public class Activator implements BundleActivator {

	public void start(final BundleContext context) throws Exception {
		SafeRunner.run(new ExtensionRegistryRunnable(context) {
			@Override
			protected void runWithoutRegistry() throws Exception {
				context.registerService(Namespace.class, new TimeServiceRestNamespace(), null);
			}
		});
	}

	public void stop(BundleContext context) throws Exception {
		// nothing
	}

}
