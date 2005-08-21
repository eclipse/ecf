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

	/**
	 * Get a list of the current Namespace instances exposed by this factory.
	 * 
	 * @return List<Namespace> of Namespace instances
	 * @exception SecurityException
	 *                thrown if caller does not have appropriate
	 *                NamespacePermission for given namespace
	 */
	public List getNamespaces() throws SecurityException;

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

	/**
	 * Get a Namespace instance by its string name.  
	 * 
	 * @param name the name to use for lookup
	 * @return Namespace instance.  Null if not found.
	 * @exception SecurityException
	 *                thrown if caller does not have appropriate
	 *                NamespacePermission for given namespace
	 */
	public Namespace getNamespaceByName(String name) throws SecurityException;

	/**
	 * Make a GUID using SHA-1 hash algorithm and a default of 16bits of data length. The
	 * value is Base64 encoded to allow for easy display. 
	 * 
	 * @return new ID instance
	 * @throws IDInstantiationException if ID cannot be constructed
	 */
	public ID makeGUID() throws IDInstantiationException;

	/**
	 * Make a GUID using SHA-1 hash algorithm and a default of 16bits of data length.  The
	 * value is Base64 encoded to allow for easy display. 
	 * 
	 * @param length the byte-length of data used to create a GUID
	 * @return new ID instance
	 * @throws IDInstantiationException if ID cannot be constructed
	 */
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
	 * @exception IDInstantiationException
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
	 * @exception IDInstantiationException
	 *                thrown if class for instantiator or ID instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(String namespacename, Object[] args)
			throws IDInstantiationException;

	/**
	 * Make a new identity instance from a namespace and String.
	 * 
	 * @param namespace
	 *            the namespace to use to make the ID
	 * @param uri
	 *            the String uri to use to make the ID
	 * @exception IDInstantiationException
	 *                thrown if class for instantiator or ID instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(Namespace namespace, String uri)
			throws IDInstantiationException;

	/**
	 * Make a new identity instance from a namespacename and URI.  The namespacename
	 * is first used to lookup the namespace with getNamespaceByName(), and then
	 * the result is passed into makeID(Namespace,String).
	 * 
	 * @param namespacename
	 *            the namespacename to use to make the ID
	 * @param uri
	 *            the String uri to use to make the ID
	 * @exception IDInstantiationException
	 *                thrown if class for instantiator or ID instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(String namespace, String uri)
			throws IDInstantiationException;

	/**
	 * Make a new identity instance from a namespace and URI.
	 * 
	 * @param namespace
	 *            the namespace to use to make the ID
	 * @param uri
	 *            the URI to use to make ID.
	 * @exception IDInstantiationException
	 *                thrown if class for instantiator or ID instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(Namespace namespace, URI uri)
			throws IDInstantiationException;

	/**
	 * Make a new identity instance from a namespace name and URI.
	 * 
	 * @param namespacename
	 *            the name of the namespace to use to make the ID
	 * @param uri
	 *            the URI to use to make ID.
	 * @exception IDInstantiationException
	 *                thrown if class for instantiator or ID instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeID(String namespacename, URI uri)
			throws IDInstantiationException;

	/**
	 * Make a an ID from a String
	 * @param idstring the String to use as this ID's unique value.  Note:  It is incumbent upon
	 * the caller of this method to be sure that the given string allows the resulting ID to
	 * satisfy the ID contract for global uniqueness within the associated Namespace.
	 * 
	 * @return valid ID instance
	 * @throws IDInstantiationException
	 *                thrown if class for instantiator or ID instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeStringID(String idstring) throws IDInstantiationException;

	/**
	 * Make a an ID from a Long
	 * @param l the Long to use as this ID's unique value.  Note:  It is incumbent upon
	 * the caller of this method to be sure that the given Long allows the resulting ID to
	 * satisfy the ID contract for global uniqueness within the associated Namespace.
	 * 
	 * @return valid ID instance
	 * @throws IDInstantiationException
	 *                thrown if class for instantiator or ID instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
	public ID makeLongID(Long l) throws IDInstantiationException;

	/**
	 * Make a an ID from a long
	 * @param l the long to use as this ID's unique value.  Note:  It is incumbent upon
	 * the caller of this method to be sure that the given long allows the resulting ID to
	 * satisfy the ID contract for global uniqueness within the associated Namespace.
	 * 
	 * @return valid ID instance
	 * @throws IDInstantiationException
	 *                thrown if class for instantiator or ID instance can't be
	 *                loaded, if something goes wrong during instance
	 *                construction
	 */
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