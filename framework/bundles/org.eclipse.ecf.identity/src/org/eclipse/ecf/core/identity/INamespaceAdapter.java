/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.core.identity;


/**
 * Namespace adapter.  This adapter allows
 * adapter factories to modify the ID creation, and 
 * scheme info for namespaces.
 * 
 */
public interface INamespaceAdapter {
	
	public ID createInstance(Namespace namespace, Object[] parameters)
	throws IDCreateException;

	public String[] getSupportedSchemes(Namespace namespace);

	public Class[][] getSupportedParameterTypes(Namespace namespace);

}
