package org.eclipse.ecf.core;


public interface ISharedObjectContainerFactory {

	/**
	 * Make ISharedObjectContainer instance. 
	 * 
	 * @param desc
	 *            the ContainerDescription to use to create the
	 *            instance
	 * @param argTypes
	 *            a String [] defining the types of the args parameter
	 * @param args
	 *            an Object [] of arguments passed to the makeInstance method of
	 *            the IContainerInstantiator
	 * @return a valid instance of ISharedObjectContainer
	 * @throws ContainerInstantiationException
	 */
	public ISharedObjectContainer makeSharedObjectContainer(
			ContainerDescription desc, String[] argTypes,
			Object[] args) throws ContainerInstantiationException;

	/**
	 * Make ISharedObjectContainer instance. 
	 * 
	 * @param descriptionName
	 *            the ContainerDescription name to lookup
	 * @return a valid instance of ISharedObjectContainer
	 * @throws ContainerInstantiationException
	 */
	public ISharedObjectContainer makeSharedObjectContainer(
			String descriptionName)
			throws ContainerInstantiationException;

	/**
	 * Make ISharedObjectContainer instance. 
	 * 
	 * @param descriptionName
	 *            the ContainerDescription name to lookup
	 * @param args
	 *            the Object [] of arguments passed to the
	 *            IContainerInstantiator.makeInstance method
	 * @return a valid instance of ISharedObjectContainer
	 * @throws ContainerInstantiationException
	 */
	public ISharedObjectContainer makeSharedObjectContainer(
			String descriptionName, Object[] args)
			throws ContainerInstantiationException;

	/**
	 * Make ISharedObjectContainer instance. 
	 * 	 * 
	 * @param descriptionName
	 *            the ContainerDescription name to lookup
	 * @param argsTypes
	 *            the String [] of argument types of the following args
	 * @param args
	 *            the Object [] of arguments passed to the
	 *            IContainerInstantiator.makeInstance method
	 * @return a valid instance of ISharedObjectContainer
	 * @throws ContainerInstantiationException
	 */
	public ISharedObjectContainer makeSharedObjectContainer(
			String descriptionName, String[] argsTypes, Object[] args)
			throws ContainerInstantiationException;

}