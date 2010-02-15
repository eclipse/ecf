/*******************************************************************************
 * Copyright (c) 2010 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.discovery;

/**
 * Interface of trackers for discovered remote services.
 * <p>
 * When a service implementing this interface is registered with the framework,
 * then <code>Discovery</code> will notify it about remote services matching one
 * of the provided criteria and will keep notifying it on changes of information
 * known to Discovery regarding this services.
 * 
 * <code>Discovery</code> may deliver notifications on discovered services to a
 * <code>DiscoveredServiceTracker</code> out of order and may concurrently call
 * and/or reenter a <code>DiscoveredServiceTracker</code>.
 * 
 * @ThreadSafe
 * @version $Revision: 1.2 $
 * 
 * @deprecated This interface is deprecated because at the time of ECF
 *             3.0/Galileo release, it seems likely that this class will be
 *             moved, or renamed, or undergo major changes after the release of
 *             ECF 3.0. This deprecation is therefore intended as a notice to
 *             consumers about these upcoming changes in the RFC119
 *             specification, and the consequent changes to these OSGi-defined
 *             classes.
 */
public interface DiscoveredServiceTracker {

	/**
	 * Optional ServiceRegistration property which contains service interfaces
	 * this tracker is interested in.
	 * <p>
	 * Value of this property is of type
	 * <code>Collection (&lt;String&gt;)</code>. May be <code>null</code> or
	 * empty.
	 */
	public static final String INTERFACE_MATCH_CRITERIA = "osgi.remote.discovery.interest.interfaces"; //$NON-NLS-1$

	/**
	 * Optional ServiceRegistration property which contains filters for services
	 * this tracker is interested in.
	 * <p>
	 * Note that these filters need to take into account service publication
	 * properties which are not necessarily the same as properties under which a
	 * service is registered. See {@link ServicePublication} for some standard
	 * properties used to publish service metadata.
	 * <p>
	 * The following sample filter will make <code>Discovery</code> notify the
	 * <code>DiscoveredServiceTracker</code> about services providing interface
	 * 'my.company.foo' of version '1.0.1.3':
	 * <code>"(&amp;(service.interface=my.company.foo)(service.interface.version=my.company.foo|1.0.1.3))"</code>.
	 * <p>
	 * Value of this property is of type
	 * <code>Collection (&lt;String&gt;)</code>. May be <code>null</code>. or
	 * empty
	 */
	public static final String FILTER_MATCH_CRITERIA = "osgi.remote.discovery.interest.filters"; //$NON-NLS-1$

	/**
	 * Receives notification that information known to <code>Discovery</code>
	 * regarding a remote service has changed.
	 * <p>
	 * The tracker is only notified about remote services which fulfill the
	 * matching criteria, either one of the interfaces or one of the filters,
	 * provided as properties of this service.
	 * <p>
	 * If multiple criteria match, then the tracker is notified about each of
	 * them. This can be done either by a single notification callback or by
	 * multiple subsequent ones.
	 * 
	 * @param notification
	 *            the <code>DiscoveredServiceNotification</code> object
	 *            describing the change. Is never <code>null</code>.
	 */
	void serviceChanged(DiscoveredServiceNotification notification);
}
