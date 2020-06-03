/****************************************************************************
 * Copyright (c) 2008 Mustafa K. Isik and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Mustafa K. Isik - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.sync.doc.cola;

import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.sync.Activator;
import org.eclipse.ecf.internal.sync.SyncDebugOptions;

//TODO make this to be something like a marker interface, does not need to be a class
public class ColaReplacementTransformationStategy implements ColaTransformationStrategy {

	private static final long serialVersionUID = -7295023855308474804L;
	private static ColaReplacementTransformationStategy INSTANCE;

	private ColaReplacementTransformationStategy() {
		// default constructor is private to enforce singleton property via
		// static factory method
	}

	public static ColaTransformationStrategy getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ColaReplacementTransformationStategy();
		}
		return INSTANCE;
	}

	public ColaDocumentChangeMessage getOperationalTransform(ColaDocumentChangeMessage remoteMsg, ColaDocumentChangeMessage appliedLocalMsg, boolean localMsgHighPrio) {
		Trace.entering(Activator.PLUGIN_ID, SyncDebugOptions.METHODS_ENTERING, this.getClass(), "getOperationalTransform", new Object[] {remoteMsg, appliedLocalMsg, new Boolean(localMsgHighPrio)}); //$NON-NLS-1$

		Trace.exiting(Activator.PLUGIN_ID, SyncDebugOptions.METHODS_EXITING, this.getClass(), "getOperationalTransform", null); //$NON-NLS-1$

		return null;
	}

}
