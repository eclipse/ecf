/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.sharedobject;

import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.core.sharedobject.Activator;
import org.eclipse.ecf.internal.core.sharedobject.SharedObjectDebugOptions;

/**
 * Abstract container adapter factory. This class implements the
 * {@link IAdapterFactory} interface. It checks that the first parameter of the
 * {@link #getAdapter(Object, Class)} method (adaptableObject) is an instance of
 * {@link ISharedObjectContainer}. If it is, then the method
 * {@link #getSharedObjectAdapter(ISharedObjectContainer, Class)} is called with
 * the ISharedObjectContainer and Class passed in as arguments.
 * 
 * @see #getSharedObjectAdapter(ISharedObjectContainer, Class)
 * 
 */
public abstract class AbstractSharedObjectContainerAdapterFactory implements
		IAdapterFactory {

	protected static final int ADD_ADAPTER_ERROR_CODE = 300001;

	protected static final String ADD_ADAPTER_ERROR_MESSAGE = "Exception adding shared object adapter";

	private static final int CREATE_ADAPTER_ID_ERROR_CODE = 300002;

	private static final String CREATE_ADAPTER_ID_ERROR_MESSAGE = null;

	/**
	 * Get an adapter for a given adaptableObject and given adapterType. If the
	 * adaptableObject is an instance of {@link ISharedObjectContainer}, then
	 * this calls the
	 * {@link #getSharedObjectAdapter(ISharedObjectContainer, Class)} method
	 * with the adaptableObject cast to be the container parameter of
	 * {@link #getSharedObjectAdapter(ISharedObjectContainer, Class)}
	 * 
	 * @param adaptableObject
	 *            the {@link ISharedObjectContainer}. If not an instance of
	 *            {@link ISharedObjectContainer} then null is returned
	 * @param adapterType
	 *            the type to return as a result. The return value must
	 *            implement this interface
	 * @return Object result. Null if the adaptableObject is not of type
	 *         {@link ISharedObjectContainer}, or if
	 *         {@link #getSharedObjectAdapter(ISharedObjectContainer, Class)}
	 *         returns null
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (ISharedObjectContainer.class.isInstance(adaptableObject))
			return getSharedObjectAdapter(
					(ISharedObjectContainer) adaptableObject, adapterType);
		else
			return null;
	}

	/**
	 * Get the {@link ISharedObject} adapter for given
	 * {@link ISharedObjectContainer}. The resulting {@link ISharedObject} must
	 * <b>also</b> implement the adapterType interface. Once called, this
	 * method will call the following methods in order:
	 * <p>
	 * </p>
	 * {@link #getAdapterID(ISharedObjectContainer, Class)}
	 * <p>
	 * </p>
	 * {@link #createAdapter(ISharedObjectContainer, Class, ID)}
	 * <p>
	 * </p>
	 * {@link #createAdapterProperties(ISharedObjectContainer, Class, ID, ISharedObject)}
	 * 
	 * @param container
	 *            the {@link ISharedObjectContainer} that will hold the new
	 *            {@link ISharedObject} adapter
	 * @param adapterType
	 *            the type that the {@link ISharedObject} must also implement to
	 *            be an adapter
	 * @return ISharedObject adapter. Must also implement adapterType interface
	 *         class
	 */
	protected synchronized ISharedObject getSharedObjectAdapter(
			ISharedObjectContainer container, Class adapterType) {
		// Get adapter ID for given adapter type
		ID adapterID = getAdapterID(container, adapterType);
		if (adapterID == null)
			return null;
		// Check to see if the container already has the given shared object
		// If so then return it
		ISharedObjectManager manager = container.getSharedObjectManager();
		if (adapterID != null) {
			ISharedObject so = manager.getSharedObject(adapterID);
			if (so != null)
				return so;
		}
		// Now create adapter instance since it's not already there
		ISharedObject adapter = createAdapter(container, adapterType, adapterID);
		if (adapter == null)
			return null;
		Map adapterProperties = createAdapterProperties(container, adapterType,
				adapterID, adapter);
		try {
			manager.addSharedObject(adapterID, adapter, adapterProperties);
		} catch (SharedObjectAddException e) {
			Trace.catching(Activator.getDefault(),
					SharedObjectDebugOptions.EXCEPTIONS_CATCHING,
					AbstractSharedObjectContainerAdapterFactory.class,
					"getSharedObjectAdapter", e);
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault()
							.getBundle().getSymbolicName(),
							ADD_ADAPTER_ERROR_CODE, ADD_ADAPTER_ERROR_MESSAGE,
							e));
			return null;
		}
		return adapter;
	}

	/**
	 * Get properties to associate with new shared object adapter creation
	 * 
	 * @param container
	 *            the container that will contain the new adapter shared object
	 * @param adapterType
	 *            the adapterType for the new shared object
	 * @param sharedObjectID
	 *            the ID for the new shared object adapter
	 * @param sharedObjectAdapter
	 *            the new shared object adapter
	 * @return Map of properties to associated with new shared object adapter.
	 *         If null is returned then no properties will be associated with
	 *         new shared object adapter. This implementation returns null.
	 *         Subclasses may override as appropriate
	 */
	protected Map createAdapterProperties(ISharedObjectContainer container,
			Class adapterType, ID sharedObjectID,
			ISharedObject sharedObjectAdapter) {
		return null;
	}

	/**
	 * Get the adapterID for the given adapterType
	 * 
	 * @param container
	 *            the container the adapter will be added to
	 * @param adapterType
	 *            the type of the adapter
	 * @return ID the ID to use for the adapter. If null is returned, then
	 *         {@link #getSharedObjectAdapter(ISharedObjectContainer, Class)}
	 *         will also return null
	 */
	protected ID getAdapterID(ISharedObjectContainer container,
			Class adapterType) {
		String singletonName = adapterType.getClass().getName();
		try {
			return IDFactory.getDefault().createStringID(singletonName);
		} catch (IDCreateException e) {
			Trace.catching(Activator.getDefault(),
					SharedObjectDebugOptions.EXCEPTIONS_CATCHING,
					AbstractSharedObjectContainerAdapterFactory.class,
					"getAdapterID", e);
			Activator.getDefault().getLog().log(
					new Status(IStatus.ERROR, Activator.getDefault()
							.getBundle().getSymbolicName(),
							CREATE_ADAPTER_ID_ERROR_CODE,
							CREATE_ADAPTER_ID_ERROR_MESSAGE, e));
			return null;
		}
	}

	/**
	 * Create an adapter instance that implements {@link ISharedObject} and
	 * adapterType. The resulting instance must implement both
	 * {@link ISharedObject} and adapterType
	 * 
	 * @param container
	 *            the container that will contain the new adapter instance
	 * @param adapterType
	 *            the adapter type. The returned value must implement this
	 *            interface
	 * @param adapterID
	 *            the ID to use for the new adapter
	 * @return ISharedObject the new adapter. If null is returned, then
	 *         {@link #getSharedObjectAdapter(ISharedObjectContainer, Class)}
	 *         will also return null
	 */
	protected abstract ISharedObject createAdapter(
			ISharedObjectContainer container, Class adapterType, ID adapterID);

	public abstract Class[] getAdapterList();

}
