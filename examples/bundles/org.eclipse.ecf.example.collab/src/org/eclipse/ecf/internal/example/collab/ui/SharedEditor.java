/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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
package org.eclipse.ecf.internal.example.collab.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

/**
 * A skeleton shared editor based on TextEditor.
 * 
 */
public class SharedEditor extends TextEditor implements IEditorPart {

	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);

		if (input instanceof FileEditorInput) {
			//IFile file = ((FileEditorInput) input).getFile();
		}

	}
}
