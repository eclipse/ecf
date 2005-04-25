/*******************************************************************************
 * Copyright (c) 2005 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.datashare;

/**
 * Interface used by service implementations to notify publishing applications
 * of the publication status.
 * 
 * @author pnehrer
 */
public interface IPublicationCallback {

	/**
	 * Notifies implementor that the give data graph has been successfully
	 * published.
	 * 
	 * @param graph
	 *            data graph that has been published
	 */
	void dataPublished(ISharedData graph);

	/**
	 * Notifies the implementor that the publication failed.
	 * 
	 * @param graph
	 *            shared data graph whose publication failed
	 * @param cause
	 *            exception that is the cause of the failure
	 */
	void publicationFailed(ISharedData graph, Throwable cause);
}
