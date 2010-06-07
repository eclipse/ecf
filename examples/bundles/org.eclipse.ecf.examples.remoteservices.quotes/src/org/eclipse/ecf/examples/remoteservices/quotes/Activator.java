package org.eclipse.ecf.examples.remoteservices.quotes;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		Executors.newSingleThreadExecutor().execute(new Runnable() {

			@Override
			public void run() {

				try {
					Bundle[] bundles = Activator.getContext().getBundles();
					ArrayList<Bundle> quotesBundles = new ArrayList<Bundle>();
					for (Bundle bundle : bundles) {
						if (bundle.getSymbolicName().contains(
								"services.quotes.")) {
							bundle.stop();
							quotesBundles.add(bundle);
						}
					}

					System.out
							.println("Number of quote service implementations: "
									+ quotesBundles.size());

					quotesBundles.get(0).start();
					int activeBundle = 0;
					int counter = 15;

					while (Activator.getContext().getBundle().getState() == Bundle.ACTIVE) {

						Thread.sleep(500);

						counter--;
						if (counter == 0) {
							counter = 15;
							quotesBundles.get(activeBundle).stop();
							Thread.sleep(1000);

							activeBundle++;
							if (activeBundle == quotesBundles.size()) {
								activeBundle = 0;
							}
							quotesBundles.get(activeBundle).start();
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
