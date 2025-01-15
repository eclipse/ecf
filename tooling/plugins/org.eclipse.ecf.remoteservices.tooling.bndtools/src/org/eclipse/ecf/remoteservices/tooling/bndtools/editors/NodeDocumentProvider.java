/*******************************************************************************
 * Copyright (c) 2020 Paul Verest, Benjamin Gurok, Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Apache Public License v2.0 which 
 * accompanies this distribution, and is available at 
 * https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Contributors: Paul Verest, Benjamin Gurok, and Composent, Inc. - initial 
 * API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservices.tooling.bndtools.editors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class NodeDocumentProvider extends FileDocumentProvider {

    @Override
    protected IDocument createDocument(Object element) throws CoreException {
        IDocument doc = super.createDocument(element);
        if (doc != null) {
            IDocumentPartitioner partitioner = new FastPartitioner(new PartitionScanner(), PartitionScanner.PARTITION_TYPES);
            partitioner.connect(doc);
            doc.setDocumentPartitioner(partitioner);
        }
        return doc;
    }

    /**
     * Alternative implementation of the method that does not require file to be
     * a physical file.
     */
    @Override
    public boolean isDeleted(Object element) {
        if (element instanceof IFileEditorInput) {
            IFileEditorInput input = (IFileEditorInput) element;

            IProject project = input.getFile().getProject();
            if (project != null && !project.exists()) {
                return true;
            }

            return !input.getFile().exists();
        }
        return super.isDeleted(element);
    }

}
