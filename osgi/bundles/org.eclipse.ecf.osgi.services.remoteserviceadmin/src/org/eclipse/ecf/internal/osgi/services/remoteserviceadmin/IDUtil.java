/****************************************************************************
 * Copyright (c) 2016 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IIDFactory;
import org.eclipse.ecf.core.identity.Namespace;

public class IDUtil {

	public static IIDFactory getIDFactory() {
		return org.eclipse.ecf.remoteservice.util.IDUtil.getIDFactory();
	}

	public static Namespace getNamespaceByName(String namespaceName) {
		return org.eclipse.ecf.remoteservice.util.IDUtil.getNamespaceByName(namespaceName);
	}

	public static Namespace findNamespaceByIdName(String idName) {
		return org.eclipse.ecf.remoteservice.util.IDUtil.findNamespaceByIdName(idName);
	}

	public static Namespace findNamespaceByScheme(String scheme) {
		return org.eclipse.ecf.remoteservice.util.IDUtil.findNamespaceByScheme(scheme);
	}

	public static ID createID(String namespaceName, String idName)
			throws IDCreateException {
		return org.eclipse.ecf.remoteservice.util.IDUtil.createID(namespaceName, idName);
	}

	public static ID createID(Namespace namespace, String idName)
			throws IDCreateException {
		return org.eclipse.ecf.remoteservice.util.IDUtil.createID(namespace, idName);
	}

	public static ID createID(Namespace namespace, Object[] args)
			throws IDCreateException {
		return org.eclipse.ecf.remoteservice.util.IDUtil.createID(namespace, args);
	}
}
