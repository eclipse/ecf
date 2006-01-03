package org.eclipse.ecf.core;

/**
 * Factory contract for {@link SharedObjectContainerFactory}
 */
public interface ISharedObjectContainerFactory {
	/**
	 * Make ISharedObjectContainer instance.
	 * 
	 * @param desc
	 *            the ContainerDescription to use to create the instance
	 * @param argTypes
	 *            a String [] defining the types of the args parameter
	 * @param args
	 *            an Object [] of arguments passed to the createInstance method of
	 *            the IContainerInstantiator
	 * @return a valid instance of ISharedObjectContainer
	 * @throws ContainerInstantiationException
	 */
	public ISharedObjectContainer createSharedObjectContainer(
			ContainerDescription desc, String[] argTypes, Object[] args)
			throws ContainerInstantiationException;

	/**
	 * Make ISharedObjectContainer instance.
	 * 
	 * @param descriptionName
	 *            the ContainerDescription name to lookup
	 * @return a valid instance of ISharedObjectContainer
	 * @throws ContainerInstantiationException
	 */
	public ISharedObjectContainer createSharedObjectContainer(
			String descriptionName) throws ContainerInstantiationException;

	/**
	 * Make ISharedObjectContainer instance.
	 * 
	 * @param descriptionName
	 *            the ContainerDescription name to lookup
	 * @param args
	 *            the Object [] of arguments passed to the
	 *            IContainerInstantiator.createInstance method
	 * @return a valid instance of ISharedObjectContainer
	 * @throws ContainerInstantiationException
	 */
	public ISharedObjectContainer createSharedObjectContainer(
			String descriptionName, Object[] args)
			throws ContainerInstantiationException;

	/**
	 * Make ISharedObjectContainer instance. 
	 * 
	 * @param descriptionName
	 *            the ContainerDescription name to lookup
	 * @param argsTypes
	 *            the String [] of argument types of the following args
	 * @param args
	 *            the Object [] of arguments passed to the
	 *            IContainerInstantiator.createInstance method
	 * @return a valid instance of ISharedObjectContainer
	 * @throws ContainerInstantiationException
	 */
	public ISharedObjectContainer createSharedObjectContainer(
			String descriptionName, String[] argsTypes, Object[] args)
			throws ContainerInstantiationException;
}