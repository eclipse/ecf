/****************************************************************************
 * Copyright (c) 2007 Composent, Inc. and others.
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

package org.eclipse.ecf.docshare;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.ui.ide.FileStoreEditorInput;

/**
 * @since 2.1
 */
public class DocShareEditorInput extends FileStoreEditorInput {

	private final String user;
	private final String fileName;

	public DocShareEditorInput(IFileStore fileStore, String user, String file) {
		super(fileStore);
		this.user = user;
		this.fileName = file;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return user + ": " + fileName; //$NON-NLS-1$
	}

}