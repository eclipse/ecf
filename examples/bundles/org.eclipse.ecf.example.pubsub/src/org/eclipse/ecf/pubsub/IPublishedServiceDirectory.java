/****************************************************************************
 * Copyright (c) 2006 Ecliptical Software Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Ecliptical Software Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.pubsub;

public interface IPublishedServiceDirectory {

	// TODO initial state currently delivered as an ADD event during listener registration
	// -- should there be a more explicit initial state delivery?
	void addReplicatedServiceListener(IPublishedServiceDirectoryListener listener);

	void removeReplicatedServiceListener(IPublishedServiceDirectoryListener listener);
}
