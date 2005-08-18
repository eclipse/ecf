package org.eclipse.ecf.core.identity;

import java.net.URI;
import java.util.List;

public interface IIDFactory {
	/**
	 * Add the given Namespace to our table of available Namespaces
	 * 
	 * @param n
	 *            the Namespace to add
	 * @return Namespace the namespace already in table (null if Namespace not
	 *         previously in table)
	 * @exception SecurityException
	 *                thrown if caller does not have appropriate
	 *                NamespacePermission for given namespace
	 */
	public Namespace addNamespace(Namespace n) throws SecurityException;

	/**
	 * Check whether table contains given Namespace instance
	 * 
	 * @param n
	 *            the Namespace to look for
	 * @return true if table does contain given Namespace, false otherwise
	 * @exception SecurityException
	 *                thrown if caller does not have appropriate
	 *                NamespacePermission for given namespace
	 */
	public boolean containsNamespace(Namespace n) throws SecurityException;

	public List getNamespaces();

	/**
	 * Get the given Namespace instance from table
	 * 
	 * @param n
	 *            the Namespace to look for
	 * @return Namespace
	 * @exception SecurityException
	 *                thrown if caller does not have appropriate
	 *                NamespacePermission for given namespace
	 */
	public Namespace getNamespace(Namespace n) throws SecurityException;

	public Namespace getNamespaceByName(String name) throws SecurityException;

	public ID makeGUID() throws IDInstantiationException;

	public ID makeGUID(int length) throws IDInstantiationException;

	/**
	 * Make a new identity. Given a Namespace instance, constructor argument
	 * types, and an array of arguments, return a new instance of an ID
	 * belonging to the given Namespace
	 * 
	 * @param n
	 *            the Namespace to which the ID belongs
	 * @param argTypes
	 *            a String [] of the arg types for the ID instance constructor
	 * @param args
	 *            an Object [] of the args for the ID instance constructor
	 * @exception IDInstantiationException
	 *                thrown if class for instantiator or instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(Namespace n, String[] argTypes, Object[] args)
			throws IDInstantiationException;

	/**
	 * Make a new identity. Given a Namespace name, constructor argument types,
	 * and an array of arguments, return a new instance of an ID belonging to
	 * the given Namespace
	 * 
	 * @param namespacename
	 *            the name of the Namespace to which the ID belongs
	 * @param argTypes
	 *            a String [] of the arg types for the ID instance constructor
	 * @param args
	 *            an Object [] of the args for the ID instance constructor
	 * @exception IDInstantiationException
	 *                thrown if class for instantiator or instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(String namespacename, String[] argTypes, Object[] args)
			throws IDInstantiationException;

	/**
	 * Make a new identity. Given a Namespace, and an array of instance
	 * constructor arguments, return a new instance of an ID belonging to the
	 * given Namespace
	 * 
	 * @param n
	 *            the Namespace to which the ID will belong
	 * @param args
	 *            an Object [] of the args for the ID instance constructor
	 * @exception Exception
	 *                thrown if class for instantiator or instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(Namespace n, Object[] args)
			throws IDInstantiationException;

	/**
	 * Make a new identity. Given a Namespace name, and an array of instance
	 * constructor arguments, return a new instance of an ID belonging to the
	 * given Namespace
	 * 
	 * @param n
	 *            the name of the Namespace to which the ID will belong
	 * @param args
	 *            an Object [] of the args for the ID instance constructor
	 * @exception Exception
	 *                thrown if class for instantiator or instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(String namespacename, Object[] args)
			throws IDInstantiationException;

	/**
	 * Make a new identity instance from a URI. Returns a new instance of an ID
	 * belonging to the Namespace associated with the URI <b>scheme</b>. The
	 * URI scheme (e.g. http) is used to lookup the Namespace instance, and the
	 * entire URI is then passed to the IDInstantiator as a single item Object
	 * [].
	 * 
	 * @param uri
	 *            the URI to use to make ID.
	 * @param args
	 *            an Object [] of the args for the ID instance constructor
	 * @exception Exception
	 *                thrown if class for instantiator or iD instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(URI uri) throws IDInstantiationException;

	/**
	 * Make a new identity instance from a namespace name and URI.
	 * 
	 * @param namespacename
	 *            the name of the namespace to use to make the ID
	 * @param uri
	 *            the URI to use to make ID.
	 * @param args
	 *            an Object [] of the args for the ID instance constructor
	 * @exception Exception
	 *                thrown if class for instantiator or iD instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(String namespacename, URI uri)
			throws IDInstantiationException;

	public ID makeStringID(String idstring) throws IDInstantiationException;

	public ID makeLongID(Long l) throws IDInstantiationException;

	public ID makeLongID(long l) throws IDInstantiationException;

	/**
	 * Remove the given Namespace from our table of available Namespaces
	 * 
	 * @param n
	 *            the Namespace to remove
	 * @return Namespace the namespace already in table (null if Namespace not
	 *         previously in table)
	 * @exception SecurityException
	 *                thrown if caller does not have appropriate
	 *                NamespacePermission for given namespace
	 */
	public Namespace removeNamespace(Namespace n) throws SecurityException;
}