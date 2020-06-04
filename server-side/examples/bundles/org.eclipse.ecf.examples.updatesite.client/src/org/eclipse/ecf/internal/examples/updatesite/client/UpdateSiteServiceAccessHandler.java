/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Markus Alexander Kuppe (Versant GmbH) - https://bugs.eclipse.org/259041
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/

package org.eclipse.ecf.internal.examples.updatesite.client;

import org.eclipse.core.commands.*;

public class UpdateSiteServiceAccessHandler extends AbstractHandler {
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		throw new ExecutionException("UpdateSiteServiceAccessHandler no longer supported"); //$NON-NLS-1$
	}
}
