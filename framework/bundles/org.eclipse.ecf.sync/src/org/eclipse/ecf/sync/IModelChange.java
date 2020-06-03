/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.sync;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Model change interface.  This super interface is
 * a 'tag' interface and does not define any methods.
 * See sub-interfaces that extend this interface.
 * 
 *  @since 2.1
 */
public interface IModelChange extends IAdaptable {

	/**
	 * Apply the change to a model.  Clients may use this method
	 * to apply the change to a model of appropriate type (e.g. IDocument 
	 * for IDocumentChange).
	 * 
	 * @param model the model to apply this change to.  Must not be <code>null</code>.
	 * Should be of type appropriate to the model.
	 * 
	 * @throws ModelUpdateException thrown if model is <code>null</code>, of incorrect
	 * type, or cannot be changed.
	 */
	public void applyToModel(Object model) throws ModelUpdateException;
	
}
