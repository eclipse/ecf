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

package org.eclipse.ecf.sync.doc;

import org.eclipse.ecf.sync.IModelChange;

/**
 * Local document change.  Instances of this class represent
 * local changes to a replicated document. 
 * 
 * @since 2.1
 */
public interface IDocumentChange extends IModelChange {
	/**
	 * Get offset in document where change has or will occur.
	 * @return int the offset
	 */
	public int getOffset();

	/**
	 * Get length of text that was replaced.  
	 * @return length of replaced text
	 */
	public int getLengthOfReplacedText();

	/**
	 * Get the new text.
	 * @return String text.  Will not return <code>null</code>, but
	 * may return empty string.
	 */
	public String getText();
}
