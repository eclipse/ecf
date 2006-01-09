package org.eclipse.ecf.core;

import java.util.List;

/**
 * Container factory contract {@link SharedObjectFactory} for default implementation.
 */
public interface ISharedObjectFactory {
	/*
	 * Add a SharedObjectDescription to the set of known SharedObjectDescription.
	 * 
	 * @param scd the SharedObjectDescription to add to this factory @return
	 * SharedObjectDescription the old description of the same name, null if none
	 * found
	 */
	public SharedObjectDescription addDescription(SharedObjectDescription description);

	/**
	 * Get a collection of the SharedObjectDescriptions currently known to this
	 * factory. This allows clients to query the factory to determine what if
	 * any other SharedObjectDescriptions are currently registered with the
	 * factory, and if so, what they are.
	 * 
	 * @return List of SharedObjectDescription instances
	 */
	public List getDescriptions();

	/**
	 * Check to see if a given named description is already contained by this
	 * factory
	 * 
	 * @param description
	 *            the SharedObjectDescription to look for
	 * @return true if description is already known to factory, false otherwise
	 */
	public boolean containsDescription(SharedObjectDescription description);

	/**
	 * Get the known SharedObjectDescription given it's name.
	 * 
	 * @param name
	 * @return SharedObjectDescription found
	 * @throws SharedObjectInstantiationException
	 */
	public SharedObjectDescription getDescriptionByName(String name)
			throws SharedObjectInstantiationException;

	/**
	 * Create ISharedObject instance. Given a SharedObjectDescription object, a String []
	 * of argument types, and an Object [] of parameters, this method will
	 * <p>
	 * <ul>
	 * <li>lookup the known SharedObjectDescriptions to find one of matching name</li>
	 * <li>if found, will retrieve or create an ISharedObjectInstantiator for that
	 * description</li>
	 * <li>Call the ISharedObjectInstantiator.createInstance method to return an
	 * instance of ISharedObject</li>
	 * </ul>
	 * 
	 * @param desc
	 *            the SharedObjectDescription to use to create the instance
	 * @param argTypes
	 *            a String [] defining the types of the args parameter
	 * @param args
	 *            an Object [] of arguments passed to the createInstance method of
	 *            the ISharedObjectInstantiator
	 * @return a valid instance of ISharedObject
	 * @throws SharedObjectInstantiationException
	 */
	public ISharedObject createSharedObject(SharedObjectDescription desc,
			String[] argTypes, Object[] args)
			throws SharedObjectInstantiationException;

	/**
	 * Create ISharedObject instance. Given a SharedObjectDescription name, this method
	 * will
	 * <p>
	 * <ul>
	 * <li>lookup the known SharedObjectDescriptions to find one of matching name</li>
	 * <li>if found, will retrieve or create an ISharedObjectInstantiator for that
	 * description</li>
	 * <li>Call the ISharedObjectInstantiator.createInstance method to return an
	 * instance of ISharedObject</li>
	 * </ul>
	 * 
	 * @param descriptionName
	 *            the SharedObjectDescription name to lookup
	 * @return a valid instance of ISharedObject
	 * @throws SharedObjectInstantiationException
	 */
	public ISharedObject createSharedObject(String descriptionName)
			throws SharedObjectInstantiationException;

	/**
	 * Create ISharedObject instance. Given a SharedObjectDescription name, this method
	 * will
	 * <p>
	 * <ul>
	 * <li>lookup the known SharedObjectDescriptions to find one of matching name</li>
	 * <li>if found, will retrieve or create an ISharedObjectInstantiator for that
	 * description</li>
	 * <li>Call the ISharedObjectInstantiator.createInstance method to return an
	 * instance of ISharedObject</li>
	 * </ul>
	 * 
	 * @param descriptionName
	 *            the SharedObjectDescription name to lookup
	 * @param args
	 *            the Object [] of arguments passed to the
	 *            ISharedObjectInstantiator.createInstance method
	 * @return a valid instance of IContainer
	 * @throws SharedObjectInstantiationException 
	 */
	public ISharedObject createSharedObject(String descriptionName, Object[] args)
			throws SharedObjectInstantiationException;

	/**
	 * Create ISharedObject instance. Given a SharedObjectDescription name, this method
	 * will
	 * <p>
	 * <ul>
	 * <li>lookup the known SharedObjectDescriptions to find one of matching name</li>
	 * <li>if found, will retrieve or create an ISharedObjectInstantiator for that
	 * description</li>
	 * <li>Call the ISharedObjectInstantiator.createInstance method to return an
	 * instance of ISharedObject</li>
	 * </ul>
	 * 
	 * @param descriptionName
	 *            the SharedObjectDescription name to lookup
	 * @param argsTypes
	 *            the String [] of argument types of the following args
	 * @param args
	 *            the Object [] of arguments passed to the
	 *            ISharedObjectInstantiator.createInstance method
	 * @return a valid instance of ISharedObject
	 * @throws SharedObjectInstantiationException
	 */
	public ISharedObject createSharedObject(String descriptionName, String[] argsTypes,
			Object[] args) throws SharedObjectInstantiationException;

	/**
	 * Remove given description from set known to this factory.
	 * 
	 * @param scd
	 *            the SharedObjectDescription to remove
	 * @return the removed SharedObjectDescription, null if nothing removed
	 */
	public SharedObjectDescription removeDescription(SharedObjectDescription scd);
}