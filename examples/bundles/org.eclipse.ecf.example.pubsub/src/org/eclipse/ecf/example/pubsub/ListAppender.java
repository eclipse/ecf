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
package org.eclipse.ecf.example.pubsub;

import org.eclipse.ecf.pubsub.model.IModelUpdater;

public class ListAppender implements IModelUpdater {
	
	public static final String ID = "org.eclipse.ecf.example.pubsub.ListAppender";
	
	public void update(Object data, Object update) {
		((AppendableList) data).add(update);
	}
}
