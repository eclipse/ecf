/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.security.IConnectContext;

/**
 * Contract for ECF communications container<br><br>
 * IContainer instances are used by clients to define a context for
 * communications.  
 * <br>
 * <br>
 * The typical lifecycle of an ECF communications container is:
 * <ol>
 * <li>Create an IContainer instance via a {@link ContainerFactory}</li>
 * <li><b>Optional</b>: Setup client-specific protocol adapters for communicating via specific protocols</li>
 * <li>Connect the container to a remote process or group</li>
 * <li>Engage in communication via protocol adapaters</li>
 * <li>Disconnect</li>
 * </ol>
 * For example, to create and connect an ECF "generic client":
 *
 * <pre>
 *      // Create container instance via factory
 * 		IContainer container = ContainerFactory.getDefault().createContainer("ecf.generic.client");
 * 
 *      // Get presence protocol adapter
 * 		IPresenceContainer presence = (IPresenceContainer) container.getAdapter(IPresenceContainer.class);
 *      // ... setup presence listeners and local input here using presence
 *      
 *      // Connect
 *      container.connect(target,targetConnectContext);
 *      
 *      // Engage in appropriate communications here using protocol adapter(s)
 *      // Manage protocol adapters as needed when finished
 *      
 *      // Disconnect
 *      container.disconnect();
 * </pre>
 * 
 */
public interface IContainer extends IAdaptable, IIdentifiable {
	/**
	 * Connect to a target remote process or process group. The target
	 * identified by the first parameter (targetID) is connected the
	 * implementation class. If authentication information is required, the
	 * required information is given via via the second parameter
	 * (connectContext).
	 * 
	 * Callers note that depending upon the provider implementation this method
	 * may block.  It is suggested that callers use a separate thread to call
	 * this method.
	 * 
	 * This method provides an implementation independent way for container
	 * implementations to connect, authenticate, and communicate with a remote
	 * service or group of services. Providers are responsible for implementing
	 * this operation in a way appropriate to the given remote service (or
	 * group) via expected protocol.
	 * 
	 * @param targetID
	 *            the ID of the remote server or group to connect to
	 * @param connectContext
	 *            any required context to allow this container to authenticate
	 * @exception ContainerConnectException
	 *                thrown if communication cannot be established with remote
	 *                service
	 */
	public void connect(ID targetID, IConnectContext connectContext)
			throws ContainerConnectException;

	/**
	 * Get the target ID that this container instance has connected
	 * to. Returns null if not connected.
	 * 
	 * @return ID of the target we are connected to. Null if currently not
	 *         connected.
	 */
	public ID getConnectedID();

	/**
	 * Get the Namespace expected by the remote target container.  Must not return null.
	 * 
	 * @return Namespace the namespace by the target for a call to connect()
	 */
	public Namespace getConnectNamespace();

	/**
	 * Disconnect. This operation will disconnect the local container instance
	 * from any previously joined target or group. Subsequent calls to
	 * getConnectedID() will return null.
	 */
	public void disconnect();

	/**
	 * This specialization of IAdaptable.getAdapter() returns additional
	 * services supported by this container. A container that supports
	 * additional services over and above the methods on <code>IContainer</code>
	 * should return them using this method. It is recommended that clients use
	 * this method rather than instanceof checks and downcasts to find out about
	 * the capabilities of a specific container.
	 * <p>
	 * Typically, after obtaining an IContainer, a client would use this method
	 * as a means to obtain a more meaningful interface to the container. This
	 * interface may or may not extend IContainer. For example, a client could
	 * use the following code to obtain an instance of ISharedObjectContainer:
	 * </p>
	 * 
	 * <pre>
	 * IContainer newContainer = ContainerFactory.createContainer(type);
	 * ISharedObjectContainer soContainer = (ISharedObjectContainer) newContainer
	 * 		.getAdapter(ISharedObjectContainer.class);
	 * if (soContainer == null)
	 * 	throw new ContainerCreateException(message);
	 * </pre>
	 * 
	 * <p>
	 * Implementations of this method should delegate to
	 * <code>Platform.getAdapterManager().getAdapter()</code> if the service
	 * cannot be provided directly to ensure extensibility by third-party
	 * plug-ins.
	 * </p>
	 * 
	 * @param serviceType
	 *            the service type to look up
	 * @return the service instance castable to the given class, or
	 *         <code>null</code> if this container does not support the given
	 *         service
	 */
	public Object getAdapter(Class serviceType);

	/**
	 * Dispose this IContainer instance.  The container instance will be made
	 * inactive after the completion of this method and will be unavailable for
	 * subsequent usage.  NOTE:  This method is not intended to be called
	 * by clients.
	 * 
	 */
	public void dispose();

	/**
	 * Add listener to IContainer. Listener's handleEvent method will be
	 * synchronously called when container methods are called. Minimally, the
	 * events delivered to the listener are as follows <br>
	 * <table BORDER=1 BORDERCOLOR="#000000" CELLPADDING=4 CELLSPACING=0>
	 * <tr>
	 * <td>container action</td>
	 * <td>Event</td>
	 * </tr>
	 * <tr>
	 * <td>connect start</td>
	 * <td>IContainerConnectingEvent</td>
	 * </tr>
	 * <tr>
	 * <td>connect complete</td>
	 * <td>IContainerConnectedEvent</td>
	 * </tr>
	 * <tr>
	 * <td>disconnect start</td>
	 * <td>IContainerDiscnnectingEvent</td>
	 * </tr>
	 * <tr>
	 * <td>disconnect complete</td>
	 * <td>IContainerDisconnectedEvent</td>
	 * </tr>
	 * </table>
	 * 
	 * @param listener
	 *            the IContainerListener to add
	 * @param filter
	 *            the filter to define types of container events to receive.
	 *            Provider implementations may choose to use a filter to
	 *            determine a subset of possible events to deliver to listener
	 */
	public void addListener(IContainerListener listener, String filter);

	/**
	 * Remove listener from IContainer.
	 * 
	 * @param listener
	 *            the IContainerListener to remove
	 */
	public void removeListener(IContainerListener listener);

}
