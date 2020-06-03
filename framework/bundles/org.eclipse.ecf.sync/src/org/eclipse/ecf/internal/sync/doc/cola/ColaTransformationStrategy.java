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

import java.io.Serializable;

public interface ColaTransformationStrategy extends Serializable {

	ColaDocumentChangeMessage getOperationalTransform(ColaDocumentChangeMessage remoteIncomingMsg, ColaDocumentChangeMessage localAppliedMsg, boolean localMsgHighPrio);
}
