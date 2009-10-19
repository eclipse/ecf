/*******************************************************************************
* Copyright (c) 2009 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.internal.examples.loadbalancing.consumer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.examples.loadbalancing.IDataProcessor;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class DataProcessorConsumerApplication implements IApplication {

	private static final String LB_SVCCONSUMER_CONTAINER_TYPE = "ecf.jms.activemq.tcp.client";
	private static final String DEFAULT_TARGET_ID = "tcp://localhost:61616/exampleTopic";
	private static final String DEFAULT_INPUT_DATA = "hello there";
	
	private BundleContext bundleContext;
	private ServiceTracker containerManagerServiceTracker;
	
	// JMS topic URI that we will connect to in order to lookup/get/use the
	// data processor remote service.  Note that this topicId can be 
	// changed by using the -topicId launch parameter...e.g.
	// -topicId tcp://myjmdnsbrokerdnsname:61616/myTopicName
	private String targetId = DEFAULT_TARGET_ID;
	
	// Container instance that connects us with the ActiveMQ queue as a message
	// producer and publishes the service on the topicId
	private IContainer container;
	
	// Input data that is passed to the data processor
	private String inputData = DEFAULT_INPUT_DATA;

	public Object start(IApplicationContext appContext) throws Exception {
		bundleContext = Activator.getContext();
		// Process Arguments...i.e. set queueId and topicId if specified
		processArgs(appContext);

		// Create container
		container = getContainerManagerService().getContainerFactory().createContainer(LB_SVCCONSUMER_CONTAINER_TYPE);
		// Create targetID
		ID targetID = IDFactory.getDefault().createID(container.getConnectNamespace(),targetId);
		
		// Get remoteServiceContainerAdapter
		IRemoteServiceContainerAdapter remoteServiceAdapter = (IRemoteServiceContainerAdapter) container
		.getAdapter(IRemoteServiceContainerAdapter.class);

		// We'll get the remote service references directly with a blocking call to getRemoteServiceReferences
		// Note that we could also use non-blocking method:  asyncGetRemoteServiceReferences OR we could us
		// the org.eclipse.ecf.remoteservice.util.tracker.RemoteServiceTracker.  For this case we'll just invoke it 
		// directly in caller thread.
		IRemoteServiceReference [] dataProcessorRefs = remoteServiceAdapter.getRemoteServiceReferences(targetID, IDataProcessor.class.getName(), null);
		// Get IRemoteService for first reference (must have at least one)
		Assert.isNotNull(dataProcessorRefs);
		Assert.isLegal(dataProcessorRefs.length > 0);
		
		// Get remote service associated with the first reference
		IRemoteService remoteService = remoteServiceAdapter.getRemoteService(dataProcessorRefs[0]);
		// At this point, the client can choose how to invoke the remote service...e.g. via IRemoteService.callAsync/1 or callSync/2
		// For this example consumer, we'll just get the proxy and invoke it in this thread
		IDataProcessor dataProcessorProxy = (IDataProcessor) remoteService.getProxy();
		System.out.println("Calling remote service ref="+dataProcessorRefs[0]+"\n\tinput data="+inputData);
		// And then simply call it
		String result = dataProcessorProxy.processData(inputData);
		// And print out results
		System.out.println("\tresult="+result);
		// And we're done
		stop();
		return IApplication.EXIT_OK;
	}

	public void stop() {
		if (container != null) {
			container.dispose();
			container = null;
			getContainerManagerService().removeAllContainers();
		}
		if (containerManagerServiceTracker != null) {
			containerManagerServiceTracker.close();
			containerManagerServiceTracker = null;
		}
		bundleContext = null;
	}

	private void processArgs(IApplicationContext appContext) {
		String[] originalArgs = (String[]) appContext.getArguments().get(
				"application.args");
		if (originalArgs == null)
			return;
		for (int i = 0; i < originalArgs.length; i++) {
			if (originalArgs[i].equals("-targetId")) {
				targetId = originalArgs[i + 1];
				i++;
			} else if (originalArgs[i].equals("-inputData")) {
				StringBuffer buf = new StringBuffer();
				for(int j=i+1; j < originalArgs.length; j++) {
					buf.append(originalArgs[j]).append(" ");
				}
				inputData = buf.toString();
				return;
			}
		}
	}

	private IContainerManager getContainerManagerService() {
		if (containerManagerServiceTracker == null) {
			containerManagerServiceTracker = new ServiceTracker(bundleContext,
					IContainerManager.class.getName(), null);
			containerManagerServiceTracker.open();
		}
		return (IContainerManager) containerManagerServiceTracker.getService();
	}

}
