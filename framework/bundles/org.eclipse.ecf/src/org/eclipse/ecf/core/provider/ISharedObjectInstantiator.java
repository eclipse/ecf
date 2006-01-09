/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.provider;

import org.eclipse.ecf.core.ISharedObject;
import org.eclipse.ecf.core.SharedObjectDescription;
import org.eclipse.ecf.core.SharedObjectInstantiationException;

public interface ISharedObjectInstantiator {
	public ISharedObject createInstance(SharedObjectDescription description,
			Class[] argTypes, Object[] args)
			throws SharedObjectInstantiationException;
}