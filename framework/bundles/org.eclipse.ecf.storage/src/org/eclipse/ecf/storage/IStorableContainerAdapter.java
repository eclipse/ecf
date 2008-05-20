/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.storage;

import org.eclipse.ecf.core.identity.IIdentifiable;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;

/**
 *
 */
public interface IStorableContainerAdapter extends IIdentifiable {

	public boolean encrypt();

	public String getFactoryName();

	public void handleStore(ISecurePreferences prefs) throws StorageException;

	public void handleRestore(ISecurePreferences prefs) throws StorageException;

}
