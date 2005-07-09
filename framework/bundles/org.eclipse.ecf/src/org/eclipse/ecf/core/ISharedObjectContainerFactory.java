package org.eclipse.ecf.core;

import java.util.List;

public interface ISharedObjectContainerFactory {

	/*
	 * Add a SharedObjectContainerDescription to the set of known
	 * SharedObjectContainerDescriptions.
	 * 
	 * @param scd the SharedObjectContainerDescription to add to this factory
	 * @return SharedObjectContainerDescription the old description of the same
	 * name, null if none found
	 */
	public SharedObjectContainerDescription addDescription(
			SharedObjectContainerDescription scd);

	/**
	 * Get a collection of the SharedObjectContainerDescriptions currently known to
	 * this factory.  This allows clients to query the factory to determine what if
	 * any other SharedObjectContainerDescriptions are currently registered with
	 * the factory, and if so, what they are.
	 * 
	 * @return List of SharedObjectContainerDescription instances
	 */
	public List getDescriptions();

	/**
	 * Check to see if a given named description is already contained by this
	 * factory
	 * 
	 * @param scd
	 *            the SharedObjectContainerDescription to look for
	 * @return true if description is already known to factory, false otherwise
	 */
	public boolean containsDescription(SharedObjectContainerDescription scd);

	/**
	 * Get the known SharedObjectContainerDescription given it's name.
	 * 
	 * @param name
	 * @return SharedObjectContainerDescription found
	 * @throws SharedObjectContainerInstantiationException
	 */
	public SharedObjectContainerDescription getDescriptionByName(String name)
			throws SharedObjectContainerInstantiationException;

	/**
	 * Make ISharedObjectContainer instance. Given a
	 * SharedObjectContainerDescription object, a String [] of argument types,
	 * and an Object [] of parameters, this method will
	 * <p>
	 * <ul>
	 * <li>lookup the known SharedObjectContainerDescriptions to find one of
	 * matching name</li>
	 * <li>if found, will retrieve or create an
	 * ISharedObjectContainerInstantiator for that description</li>
	 * <li>Call the ISharedObjectContainerInstantiator.makeInstance method to
	 * return an instance of ISharedObjectContainer</li>
	 * </ul>
	 * 
	 * @param desc
	 *            the SharedObjectContainerDescription to use to create the
	 *            instance
	 * @param argTypes
	 *            a String [] defining the types of the args parameter
	 * @param args
	 *            an Object [] of arguments passed to the makeInstance method of
	 *            the ISharedObjectContainerInstantiator
	 * @return a valid instance of ISharedObjectContainer
	 * @throws SharedObjectContainerInstantiationException
	 */
	public ISharedObjectContainer makeSharedObjectContainer(
			SharedObjectContainerDescription desc, String[] argTypes,
			Object[] args) throws SharedObjectContainerInstantiationException;

	/**
	 * Make ISharedObjectContainer instance. Given a
	 * SharedObjectContainerDescription name, this method will
	 * <p>
	 * <ul>
	 * <li>lookup the known SharedObjectContainerDescriptions to find one of
	 * matching name</li>
	 * <li>if found, will retrieve or create an
	 * ISharedObjectContainerInstantiator for that description</li>
	 * <li>Call the ISharedObjectContainerInstantiator.makeInstance method to
	 * return an instance of ISharedObjectContainer</li>
	 * </ul>
	 * 
	 * @param descriptionName
	 *            the SharedObjectContainerDescription name to lookup
	 * @return a valid instance of ISharedObjectContainer
	 * @throws SharedObjectContainerInstantiationException
	 */
	public ISharedObjectContainer makeSharedObjectContainer(
			String descriptionName)
			throws SharedObjectContainerInstantiationException;

	/**
	 * Make ISharedObjectContainer instance. Given a
	 * SharedObjectContainerDescription name, this method will
	 * <p>
	 * <ul>
	 * <li>lookup the known SharedObjectContainerDescriptions to find one of
	 * matching name</li>
	 * <li>if found, will retrieve or create an
	 * ISharedObjectContainerInstantiator for that description</li>
	 * <li>Call the ISharedObjectContainerInstantiator.makeInstance method to
	 * return an instance of ISharedObjectContainer</li>
	 * </ul>
	 * 
	 * @param descriptionName
	 *            the SharedObjectContainerDescription name to lookup
	 * @param args
	 *            the Object [] of arguments passed to the
	 *            ISharedObjectContainerInstantiator.makeInstance method
	 * @return a valid instance of ISharedObjectContainer
	 * @throws SharedObjectContainerInstantiationException
	 */
	public ISharedObjectContainer makeSharedObjectContainer(
			String descriptionName, Object[] args)
			throws SharedObjectContainerInstantiationException;

	/**
	 * Make ISharedObjectContainer instance. Given a
	 * SharedObjectContainerDescription name, this method will
	 * <p>
	 * <ul>
	 * <li>lookup the known SharedObjectContainerDescriptions to find one of
	 * matching name</li>
	 * <li>if found, will retrieve or create an
	 * ISharedObjectContainerInstantiator for that description</li>
	 * <li>Call the ISharedObjectContainerInstantiator.makeInstance method to
	 * return an instance of ISharedObjectContainer</li>
	 * </ul>
	 * 
	 * @param descriptionName
	 *            the SharedObjectContainerDescription name to lookup
	 * @param argsTypes
	 *            the String [] of argument types of the following args
	 * @param args
	 *            the Object [] of arguments passed to the
	 *            ISharedObjectContainerInstantiator.makeInstance method
	 * @return a valid instance of ISharedObjectContainer
	 * @throws SharedObjectContainerInstantiationException
	 */
	public ISharedObjectContainer makeSharedObjectContainer(
			String descriptionName, String[] argsTypes, Object[] args)
			throws SharedObjectContainerInstantiationException;

	/**
	 * Remove given description from set known to this factory.
	 * 
	 * @param scd
	 *            the SharedObjectContainerDescription to remove
	 * @return the removed SharedObjectContainerDescription, null if nothing
	 *         removed
	 */
	public SharedObjectContainerDescription removeDescription(
			SharedObjectContainerDescription scd);

}