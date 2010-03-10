/*******************************************************************************
* Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.tests.osgi.services.distribution.localdiscovery;

import java.net.URL;
import java.util.Dictionary;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.osgi.services.discovery.local.DiscoveryCommandProvider;
import org.eclipse.ecf.tests.internal.osgi.services.distribution.localdiscovery.Activator;
import org.eclipse.ecf.tests.osgi.services.distribution.AbstractDistributionTest;
import org.eclipse.ecf.tests.osgi.services.distribution.localdiscovery.generic.DiscoveryCommandProviderServiceTracker;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public abstract class AbstractMultiServiceProxyTest extends
		AbstractDistributionTest {

	protected DiscoveryCommandProvider discoveryCommandProvider;
	protected URL serviceDescriptionURL;
	protected IContainer clientContainer;
	
	protected void setUp() throws Exception {
		discoveryCommandProvider = getDiscoveryCommandProvider();
		serviceDescriptionURL = getServiceDescriptionURL();
		clientContainer = getContainerFactory().createContainer(getClientContainerName());
	}
	
	protected abstract URL getServiceDescriptionURL();
	
	protected void tearDown() throws Exception {
		discoveryCommandProvider = null;
		serviceDescriptionURL = null;
		clientContainer.dispose();
		getContainerManager().removeAllContainers();
	}
	
	protected BundleContext getContext() {
		return Activator.getDefault().getContext();
	}

	protected DiscoveryCommandProvider getDiscoveryCommandProvider() {
		DiscoveryCommandProviderServiceTracker st = new DiscoveryCommandProviderServiceTracker();
		st.open();
		DiscoveryCommandProvider provider = st.getDiscoveryCommandProvider();
		st.close();
		return provider;
	}

	protected void publishServiceDescriptions(final URL url) {
		publishServiceDescriptions(url.toString());
	}

	int argsAccessed = 0;

	protected void publishServiceDescriptions(final String url) {
		discoveryCommandProvider._publish(new CommandInterpreter() {
			public String nextArgument() {
				argsAccessed++;
				return (argsAccessed == 1) ? url : null;
			}

			public Object execute(String cmd) {
				return null;
			}

			public void print(Object o) {
			}

			public void println() {
			}

			public void println(Object o) {
			}

			public void printStackTrace(Throwable t) {
			}

			public void printDictionary(Dictionary dic, String title) {
			}

			public void printBundleResource(Bundle bundle, String resource) {
			}
		});

	}


}
