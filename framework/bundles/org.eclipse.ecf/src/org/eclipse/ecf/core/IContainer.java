package org.eclipse.ecf.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.security.IJoinContext;

/**
 * Distributed container contract
 */
public interface IContainer extends IAdaptable, IIdentifiable {

	/**
	 * Join a container group. The group to join is identified by the first
	 * parameter (groupID) using any required authentication provided via the
	 * second parameter (joinContext). This method provides an implementation
	 * independent way for container implementations to connect, authenticate,
	 * and communicate with a remote service or group of services. Providers are
	 * responsible for implementing this operation in a way appropriate to the
	 * given remote service and expected protocol.
	 * 
	 * @param groupID
	 *            the ID of the remote service to join
	 * @param joinContext
	 *            any required context to allow this container to authenticate
	 *            on join with remote containers
	 * @exception ContainerJoinException
	 *                thrown if communication cannot be established with remote
	 *                service
	 */
	public void joinGroup(ID groupID, IJoinContext joinContext)
			throws ContainerJoinException;

	/**
	 * Get the group id that this container has joined. Return null if no group
	 * has previously been joined.
	 * 
	 * @return ID of the group previously joined
	 */
	public ID getGroupID();

	/**
	 * Leave a container group. This operation will disconnect the local
	 * container instance from any previously joined group.
	 */
	public void leaveGroup();
	
	/**
	 * This specialization of IAdaptable.getAdapter() returns additional
	 * services supported by this container. A container that supports
	 * additional services over and above the methods on <code>IContainer</code>
	 * should return them using this method. It is recommended that clients use
	 * this method rather than instanceof checks and downcasts to find out about
	 * the capabilities of a specific container.
	 * <p>
	 * Typically, after obtaining an IContainer, a client would use this
	 * method as a means to obtain a more meaningful interface to the container.
	 * This interface may or may not extend IContainer. For example, a client
	 * could use the following code to obtain an instance of
	 * ISharedObjectContainer:
	 * </p>
	 * <pre>
	 * IContainer newContainer = ContainerFactory.makeContainer(type);
     * ISharedObjectContainer soContainer = (ISharedObjectContainer) 
     *                                       newContainer.getAdapter(ISharedObjectContainer.class);
     * if (soContainer==null) throw new ContainerInstantiationException(message);
     * </pre>
	 * <p>
	 * Implementations of this method should delegate to
	 * <code>Platform.getAdapterManager().getAdapter()</code> if the service
	 * cannot be provided directly to ensure extensibility by third-party
	 * plug-ins.
	 * </p>
	 * @param serviceType the service type to look up
	 * @return the service instance castable to the given class, or
	 * <code>null</code> ifnthis container does not support the given service
	 */
	public Object getAdapter(Class serviceType);

	/**
	 * Dispose this ISharedObjectContainer instance. The container instance will
	 * be made inactive after the completion of this method and will be
	 * unavailable for subsequent usage
	 * 
	 */
	public void dispose();


}
