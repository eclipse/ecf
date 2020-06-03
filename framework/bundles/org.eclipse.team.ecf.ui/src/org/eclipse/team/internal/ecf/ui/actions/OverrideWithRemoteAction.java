/****************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.team.internal.ecf.ui.actions;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.team.internal.ecf.ui.Messages;
import org.eclipse.team.ui.synchronize.*;

public class OverrideWithRemoteAction extends SynchronizeModelAction {

	public OverrideWithRemoteAction(ISynchronizePageConfiguration configuration) {
		super(Messages.OverrideWithRemoteAction_ActionLabel, configuration);
	}

	//	protected boolean updateSelection(IStructuredSelection selection) {
	//		if (super.updateSelection(selection)) {
	//			Object[] array = selection.toArray();
	//			for (int i = 0; i < array.length; i++) {
	//				if (array[i] instanceof ISynchronizeModelElement) {
	//					ISynchronizeModelElement modelElement = (ISynchronizeModelElement) array[i];
	//					IResource resource = modelElement.getResource();
	//					if (resource == null
	//							|| resource.getType() != IResource.FILE) {
	//						return false;
	//					}
	//				}
	//			}
	//
	//			return true;
	//		}
	//		return false;
	//	}

	protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		return new OverrideWithRemoteOperation(configuration, elements);
	}

}
