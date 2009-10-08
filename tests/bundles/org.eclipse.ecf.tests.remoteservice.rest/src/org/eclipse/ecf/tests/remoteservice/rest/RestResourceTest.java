/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *   Andre Dietisheim - declarative services tests
 *******************************************************************************/
package org.eclipse.ecf.tests.remoteservice.rest;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.remoteservice.rest.IRestResourceRepresentationFactory;
import org.eclipse.ecf.remoteservice.rest.util.DSUtil;
import org.eclipse.ecf.tests.remoteservice.Activator;
import org.eclipse.equinox.concurrent.future.IExecutor;
import org.eclipse.equinox.concurrent.future.IFuture;
import org.eclipse.equinox.concurrent.future.IProgressRunnable;
import org.eclipse.equinox.concurrent.future.ThreadsExecutor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class RestResourceTest extends TestCase {

	private static final String BUNDLEID_ECFREST = "org.eclipse.ecf.remoteservice.rest";

	private static final String BUNDLEID_DS = "org.eclipse.equinox.ds";
	private IRestResourceRepresentationFactory resourceFactory;
	private BundleContext context;

	protected void setUp() throws Exception {
		this.context = Activator.getDefault().getContext();
		resourceFactory = getResourceFactory(context);
	}

	public void testCreation() {
		assertNotNull(resourceFactory);
	}

	public void testResourceCreation() {
		Object adapter = ((IAdaptable) resourceFactory).getAdapter(List.class);
		assertTrue(adapter instanceof List);
		List resources = (List) adapter;
		assertTrue(resources.size() >= 1);
	}

	public void testKnowsWhetherDSIsRunning()
			throws InvalidSyntaxException, BundleException {
		startBundle(BUNDLEID_DS);
		assertTrue(DSUtil.isRunning(context));

		stopBundle(BUNDLEID_DS);
		assertFalse(DSUtil.isRunning(context));
	}

	private void startBundle(String bundleId) throws BundleException {
		final Bundle bundle = Platform.getBundle(bundleId);
		if (!isBundleActive(bundleId)) {
			bundle.start();
		}
		waitForBundleState(bundle, Bundle.ACTIVE);
	}

	private void waitForBundleState(final Bundle bundle, final int bundleState) {
		final IProgressRunnable runnable = new IProgressRunnable() {
			public Object run(IProgressMonitor monitor) {
				while (bundle.getState() != bundleState) {
					System.err.println("waiting for state " + bundleState
							+ ": current state = " + bundle.getState());
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		};
		IExecutor executor = new ThreadsExecutor();
		IFuture future = executor.execute(runnable, null);
		// wait for bundle state
		try {
			future.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isBundleActive(String bundleId) throws BundleException {
		Bundle bundle = Platform.getBundle(bundleId);
		return bundle != null && bundle.getState() == Bundle.ACTIVE;
	}

	private void stopBundle(String bundleId) throws BundleException {
		Bundle bundle = Platform.getBundle(bundleId);
		if (isBundleActive(bundleId)) {
			bundle.stop();
		}
		waitForBundleState(bundle, Bundle.RESOLVED);
	}

	private IRestResourceRepresentationFactory getResourceFactory(
			BundleContext context) throws InvalidSyntaxException {
		IRestResourceRepresentationFactory factory = null;
		ServiceReference[] serviceReferences = context.getServiceReferences(
				IRestResourceRepresentationFactory.class.getName(), null);
		if (serviceReferences != null) {
			for (int i = 0; i < serviceReferences.length; i++) {
				ServiceReference serviceReference = serviceReferences[i];
				if (BUNDLEID_ECFREST.equals(serviceReference.getBundle()
						.getSymbolicName())) {
					Object service = context.getService(serviceReference);
					if (service instanceof IRestResourceRepresentationFactory) {
						factory = (IRestResourceRepresentationFactory) service;
						break;
					}
				}
			}
		}
		return factory;
	}
}
