package org.eclipse.ecf.core;

import java.net.URI;
import java.util.List;

public interface IContainerFactory {

	/*
	 * Add a ContainerDescription to the set of known
	 * SharedObjectContainerDescriptions.
	 * 
	 * @param scd the ContainerDescription to add to this factory
	 * @return ContainerDescription the old description of the same
	 * name, null if none found
	 */
	public ContainerDescription addDescription(
			ContainerDescription scd);

	/**
	 * Get a collection of the ContainerDescriptions currently known to
	 * this factory.  This allows clients to query the factory to determine what if
	 * any other ContainerDescriptions are currently registered with
	 * the factory, and if so, what they are.
	 * 
	 * @return List of ContainerDescription instances
	 */
	public List getDescriptions();

	/**
	 * Check to see if a given named description is already contained by this
	 * factory
	 * 
	 * @param scd
	 *            the ContainerDescription to look for
	 * @return true if description is already known to factory, false otherwise
	 */
	public boolean containsDescription(ContainerDescription scd);

	/**
	 * Get the known ContainerDescription given it's name.
	 * 
	 * @param name
	 * @return ContainerDescription found
	 * @throws ContainerInstantiationException
	 */
	public ContainerDescription getDescriptionByName(String name)
			throws ContainerInstantiationException;

	/**
	 * Make IContainer instance. Given a
	 * ContainerDescription object, a String [] of argument types,
	 * and an Object [] of parameters, this method will
	 * <p>
	 * <ul>
	 * <li>lookup the known ContainerDescriptions to find one of
	 * matching name</li>
	 * <li>if found, will retrieve or create an
	 * IContainerInstantiator for that description</li>
	 * <li>Call the IContainerInstantiator.makeInstance method to
	 * return an instance of IContainer</li>
	 * </ul>
	 * 
	 * @param desc
	 *            the ContainerDescription to use to create the
	 *            instance
	 * @param argTypes
	 *            a String [] defining the types of the args parameter
	 * @param args
	 *            an Object [] of arguments passed to the makeInstance method of
	 *            the IContainerInstantiator
	 * @return a valid instance of IContainer
	 * @throws ContainerInstantiationException
	 */
	public IContainer makeContainer(
			ContainerDescription desc, String[] argTypes,
			Object[] args) throws ContainerInstantiationException;

	/**
	 * Make IContainer instance. Given a
	 * ContainerDescription name, this method will
	 * <p>
	 * <ul>
	 * <li>lookup the known ContainerDescriptions to find one of
	 * matching name</li>
	 * <li>if found, will retrieve or create an
	 * IContainerInstantiator for that description</li>
	 * <li>Call the IContainerInstantiator.makeInstance method to
	 * return an instance of IContainer</li>
	 * </ul>
	 * 
	 * @param descriptionName
	 *            the ContainerDescription name to lookup
	 * @return a valid instance of IContainer
	 * @throws ContainerInstantiationException
	 */
	public IContainer makeContainer(
			String descriptionName)
			throws ContainerInstantiationException;

	/**
	 * Make IContainer instance. Given a
	 * ContainerDescription name, this method will
	 * <p>
	 * <ul>
	 * <li>lookup the known ContainerDescriptions to find one of
	 * matching name</li>
	 * <li>if found, will retrieve or create an
	 * IContainerInstantiator for that description</li>
	 * <li>Call the IContainerInstantiator.makeInstance method to
	 * return an instance of IContainer</li>
	 * </ul>
	 * 
	 * @param descriptionName
	 *            the ContainerDescription name to lookup
	 * @param args
	 *            the Object [] of arguments passed to the
	 *            IContainerInstantiator.makeInstance method
	 * @return a valid instance of IContainer
	 * @throws ContainerInstantiationException
	 */
	public IContainer makeContainer(
			String descriptionName, Object[] args)
			throws ContainerInstantiationException;

	/**
	 * Make IContainer instance. Given a
	 * ContainerDescription name, this method will
	 * <p>
	 * <ul>
	 * <li>lookup the known ContainerDescriptions to find one of
	 * matching name</li>
	 * <li>if found, will retrieve or create an
	 * IContainerInstantiator for that description</li>
	 * <li>Call the IContainerInstantiator.makeInstance method to
	 * return an instance of IContainer</li>
	 * </ul>
	 * 
	 * @param descriptionName
	 *            the ContainerDescription name to lookup
	 * @param argsTypes
	 *            the String [] of argument types of the following args
	 * @param args
	 *            the Object [] of arguments passed to the
	 *            IContainerInstantiator.makeInstance method
	 * @return a valid instance of IContainer
	 * @throws ContainerInstantiationException
	 */
	public IContainer makeContainer(
			String descriptionName, String[] argsTypes, Object[] args)
			throws ContainerInstantiationException;

	/**
	 * Make container given URI.  The given URI provides a scheme that is used
	 * to lookup both the Namespace for the new ID instance, and the name of the 
	 * container factory extension.  For example, the following URI:  
	 * 
	 * protocol.name.foo:bar.com:3222/path
	 * 
	 * Would result in a namespace lookup with identifier 'protocol.name.foo', a container
	 * instantiator lookup with 'protocol.name.foo'
	 */
	public IContainer makeContainer(URI aURI) throws ContainerInstantiationException;
	
	/**
	 * Remove given description from set known to this factory.
	 * 
	 * @param scd
	 *            the ContainerDescription to remove
	 * @return the removed ContainerDescription, null if nothing
	 *         removed
	 */
	public ContainerDescription removeDescription(
			ContainerDescription scd);

}