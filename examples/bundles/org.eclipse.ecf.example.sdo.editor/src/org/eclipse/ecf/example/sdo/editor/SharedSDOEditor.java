/*******************************************************************************
 * Copyright (c) 2004 Peter Nehrer and Composent, Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Peter Nehrer - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.example.sdo.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.sdo.ISharedDataGraph;
import org.eclipse.ecf.sdo.IUpdateConsumer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.sdo.EDataGraph;
import org.eclipse.emf.ecore.sdo.presentation.SDOEditor;
import org.eclipse.ui.IFileEditorInput;

import commonj.sdo.ChangeSummary;
import commonj.sdo.DataGraph;

/**
 * @author pnehrer
 */
public class SharedSDOEditor extends SDOEditor {

    private class UpdateConsumer implements IUpdateConsumer {
        public boolean consumeUpdate(ISharedDataGraph graph, ID containerID) {
            ChangeSummary changeSummary = graph.getDataGraph()
                    .getChangeSummary();
            changeSummary.endLogging();
            SharedSDOEditor.super.doSave(null);
            changeSummary.beginLogging();
            return true;
        }

        public void updateFailed(ISharedDataGraph graph, ID containerID,
                Throwable cause) {
            EditorPlugin.getDefault().log(
                    new CoreException(new Status(Status.ERROR, EditorPlugin
                            .getDefault().getBundle().getSymbolicName(), 0,
                            "Data graph upate failed.", cause)));
        }
    }

    private ISharedDataGraph sharedDataGraph;

    protected boolean isConnected(IFileEditorInput editorInput) {
        try {
            EditorPlugin.getDefault()
                    .checkConnected(
                            ((IFileEditorInput) editorInput).getFile()
                                    .getProject());
            return true;
        } catch (ECFException e) {
        	return false;
        }
   	
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.emf.ecore.sdo.presentation.SDOEditor#createModel()
     */
    public void createModel() {
        try {
            IFileEditorInput modelFile = (IFileEditorInput) getEditorInput();
            if (isConnected(modelFile)) {
	            String path = modelFile.getFile().getFullPath().toString();
	            URI uri = URI.createPlatformResourceURI(modelFile.getFile()
	                    .getFullPath().toString());
	            if (EditorPlugin.getDefault().isPublished(path)) {
	                sharedDataGraph = EditorPlugin.getDefault().subscribe(path,
	                        new UpdateConsumer());
	                EDataGraph dataGraph = (EDataGraph) sharedDataGraph
	                        .getDataGraph();
	                dataGraph.getDataGraphResource().setURI(uri);
	                editingDomain.getResourceSet().getResources().addAll(
	                        dataGraph.getResourceSet().getResources());
	                dataGraph.setResourceSet(editingDomain.getResourceSet());
	            } else {
	                Resource resource = editingDomain.loadResource(uri.toString());
	                DataGraph dataGraph = (DataGraph) resource.getContents().get(0);
	                sharedDataGraph = EditorPlugin.getDefault().publish(path,
	                        dataGraph, new UpdateConsumer());
	            }
            } else super.createModel();
        } catch (ECFException e) {
            EditorPlugin.getDefault().log(e);
            if (sharedDataGraph != null) {
                sharedDataGraph.dispose();
                sharedDataGraph = null;
            }
            super.createModel();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.emf.ecore.sdo.presentation.SDOEditor#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor progressMonitor) {
        super.doSave(progressMonitor);
        if (sharedDataGraph != null)
            try {
                sharedDataGraph.commit();
            } catch (ECFException e) {
                EditorPlugin.getDefault().log(e);
            }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.emf.ecore.sdo.presentation.SDOEditor#dispose()
     */
    public void dispose() {
        if (sharedDataGraph != null)
            sharedDataGraph.dispose();

        super.dispose();
    }
}