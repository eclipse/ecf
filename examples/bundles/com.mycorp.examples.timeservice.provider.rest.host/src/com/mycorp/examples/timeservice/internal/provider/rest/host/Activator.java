package com.mycorp.examples.timeservice.internal.provider.rest.host;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.util.ExtensionRegistryRunnable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	public void start(final BundleContext context) throws Exception {
		SafeRunner.run(new ExtensionRegistryRunnable(context) {
			@Override
			protected void runWithoutRegistry() throws Exception {
				context.registerService(ContainerTypeDescription.class, new ContainerTypeDescription(TimeServiceServerContainer.TIMESERVICE_HOST_CONFIG_NAME, new TimeServiceServerContainerInstantiator(), "TimeService REST Server", true,false), null);
			}
		});
	}

	public void stop(BundleContext context) throws Exception {
	}

}
