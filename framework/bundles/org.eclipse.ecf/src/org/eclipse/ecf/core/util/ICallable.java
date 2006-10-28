/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.util;

/**
 * Interface defining a block that can be called, can return an Object result
 * and throw an arbitrary Throwable
 * 
 */
public interface ICallable {
	/** Perform some action that returns a result or throws an exception
	 * @return result from calling action
	 * @throws Throwable
	 */
	Object call() throws Throwable;
}