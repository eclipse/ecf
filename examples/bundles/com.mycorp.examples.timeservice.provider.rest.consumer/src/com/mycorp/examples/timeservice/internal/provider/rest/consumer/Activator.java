package com.mycorp.examples.timeservice.internal.provider.rest.consumer;

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
				context.registerService(ContainerTypeDescription.class, new ContainerTypeDescription(TimeServiceRestClientContainer.TIMESERVICE_CONSUMER_CONFIG_NAME, new TimeServiceRestClientContainerInstantiator(), "TimeService REST Client", false ,false), null);
			}
		});
	}

	public void stop(BundleContext context) throws Exception {
	}

}
