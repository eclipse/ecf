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

import java.util.Collection;

/**
 * Interface for notification on discovered services.
 * <p>
 * <code>DiscoveredServiceNotification</code> objects are immutable.
 * 
 * @Immutable
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
public interface DiscoveredServiceNotification {

	/**
	 * Notification indicating that a service matching the listening criteria
	 * has been discovered.
	 * <p>
	 * The value of <code>AVAILABLE</code> is 0x00000001.
	 */
	public final static int AVAILABLE = 0x00000001;

	/**
	 * Notification indicating that the properties of a previously discovered
	 * service have changed.
	 * <p>
	 * The value of <code>MODIFIED</code> is 0x00000002.
	 */
	public final static int MODIFIED = 0x00000002;

	/**
	 * Notification indicating that a previously discovered service is no longer
	 * known to <code>Discovery</code>.
	 * <p>
	 * The value of <code>UNAVAILABLE</code> is 0x00000004.
	 */
	public final static int UNAVAILABLE = 0x00000004;

	/**
	 * Notification indicating that the properties of a previously discovered
	 * service have changed and the new properties no longer match the
	 * listener's filter.
	 * <p>
	 * The value of <code>MODIFIED_ENDMATCH</code> is 0x00000008.
	 */
	public final static int MODIFIED_ENDMATCH = 0x00000008;

	/**
	 * Returns information currently known to <code>Discovery</code> regarding
	 * the service endpoint.
	 * 
	 * @return metadata of the service <code>Discovery</code> notifies about. Is
	 *         never <code>null</code>.
	 */
	ServiceEndpointDescription getServiceEndpointDescription();

	/**
	 * Returns the type of notification. The type values are:
	 * <ul>
	 * <li>{@link #AVAILABLE}</li>
	 * <li>{@link #MODIFIED}</li>
	 * <li>{@link #MODIFIED_ENDMATCH}</li>
	 * <li>{@link #UNAVAILABLE}</li>
	 * </ul>
	 * 
	 * @return Type of notification regarding known service metadata.
	 */
	int getType();

	/**
	 * Returns interface name criteria of the {@link DiscoveredServiceTracker}
	 * object matching with the interfaces of the
	 * <code>ServiceEndpointDescription</code> and thus caused the notification.
	 * 
	 * @return <code>Collection (&lt;String&gt;)</code> of matching interface
	 *         name criteria of the <code>DiscoveredServiceTracker</code> object
	 *         being notified, or an empty collection if notification hasn't
	 *         been caused by a matching interface name criteria.
	 */
	Collection/* <String> */getInterfaces();

	/**
	 * Returns filters of the <code>DiscoveredServiceTracker</code> object
	 * matching with the properties of the
	 * <code>ServiceEndpointDescription</code> and thus caused the notification.
	 * 
	 * @return <code>Collection (&lt;String&gt;)</code> of matching filters of
	 *         the <code>DiscoveredServiceTracker</code> object being notified,
	 *         or an empty collection if notification hasn't been caused by a
	 *         matching filter criteria.
	 */
	Collection/* <String> */getFilters();
}