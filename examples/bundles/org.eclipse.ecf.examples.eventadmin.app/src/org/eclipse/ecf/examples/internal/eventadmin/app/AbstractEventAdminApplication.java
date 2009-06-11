/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.examples.internal.eventadmin.app;

import java.util.Map;
import java.util.Properties;

import org.eclipse.ecf.core.ContainerConnectException;
import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.sharedobject.ISharedObjectContainer;
import org.eclipse.ecf.core.sharedobject.SharedObjectAddException;
import org.eclipse.ecf.examples.eventadmin.EventAdminImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractEventAdminApplication implements IApplication {

	protected BundleContext bundleContext;
	// The following must be set in processArgs
	protected String containerType;
	protected String containerId;
	protected String targetId;
	protected String topic;

	protected ServiceTracker containerManagerTracker;
	private final Object appLock = new Object();
	private boolean done = false;
	protected EventAdminImpl eventAdminImpl;
	protected ServiceRegistration eventAdminRegistration;
	protected IContainer container;

	public Object start(IApplicationContext context) throws Exception {
		// Get BundleContext
		bundleContext = Activator.getContext();
		// Process Arguments
		processArgs(context.getArguments());
		// Create event admin impl
		eventAdminImpl = new EventAdminImpl(bundleContext);
		// Create, configure, and connect container
		createConfigureAndConnectContainer();
		// registerEventAdmin
		registerEventAdmin();
		return new Integer(0);
	}

	protected abstract void processArgs(Map args);

	protected void registerEventAdmin() {
		// Create properties for event admin
		Properties eventAdminProps = new Properties();
		eventAdminProps.put(EventConstants.EVENT_TOPIC, topic);
		// register event admin service
		eventAdminRegistration = bundleContext.registerService(EventAdmin.class
				.getName(), eventAdminImpl, eventAdminProps);
	}

	protected void waitForDone() {
		// then just wait here
		synchronized (appLock) {
			while (!done) {
				try {
					appLock.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}

	public void stop() {
		if (eventAdminRegistration != null) {
			eventAdminRegistration.unregister();
			eventAdminRegistration = null;
		}
		if (container != null) {
			container.dispose();
			getContainerManager().removeAllContainers();
			container = null;
		}
		if (containerManagerTracker != null) {
			containerManagerTracker.close();
			containerManagerTracker = null;
		}
		synchronized (appLock) {
			done = true;
			appLock.notifyAll();
		}
		bundleContext = null;
	}

	protected void connectContainer(IContainer container, String target)
			throws ContainerConnectException {
		if (target != null)
			container.connect(IDFactory.getDefault().createID(
					container.getConnectNamespace(), target), null);
	}

	protected void createConfigureAndConnectContainer()
			throws ContainerCreateException, SharedObjectAddException,
			ContainerConnectException {
		container = createContainer(containerType, containerId);
		addEventAdmin(container, eventAdminImpl, topic);
		connectContainer(container, targetId);
	}

	protected IContainerManager getContainerManager() {
		if (containerManagerTracker == null) {
			containerManagerTracker = new ServiceTracker(bundleContext,
					IContainerManager.class.getName(), null);
			containerManagerTracker.open();
		}
		return (IContainerManager) containerManagerTracker.getService();
	}

	protected IContainer createContainer(String containerType,
			String containerId) throws ContainerCreateException {
		IContainerFactory containerFactory = getContainerManager()
				.getContainerFactory();
		return (containerId == null) ? containerFactory
				.createContainer(containerType) : containerFactory
				.createContainer(containerType, new Object[] { containerId });
	}

	protected void addEventAdmin(IContainer container,
			EventAdminImpl eventAdmin, String topic)
			throws SharedObjectAddException {
		ISharedObjectContainer soContainer = (ISharedObjectContainer) container
				.getAdapter(ISharedObjectContainer.class);
		soContainer.getSharedObjectManager().addSharedObject(
				IDFactory.getDefault().createStringID(topic), eventAdmin, null);
	}
}
