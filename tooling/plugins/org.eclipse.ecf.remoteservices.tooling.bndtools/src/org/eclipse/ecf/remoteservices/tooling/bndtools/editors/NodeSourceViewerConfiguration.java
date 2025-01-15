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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class NodeSourceViewerConfiguration extends SourceViewerConfiguration {

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        PresentationReconciler pr = new PresentationReconciler();
        pr.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
        
        CodeScanner scanner = new CodeScanner();
        setDamagerRepairer(pr, scanner, IDocument.DEFAULT_CONTENT_TYPE);
        setDamagerRepairer(pr, new MultilineCommentScanner(scanner.getCommentAttribute()), PartitionScanner.MULTILINE_COMMENT);
        setDamagerRepairer(pr, new MultilineCommentScanner(scanner.getDocAttribute()), PartitionScanner.GROOVYDOC);
        return pr;
    }

    private void setDamagerRepairer(PresentationReconciler pr, ITokenScanner scanner, String tokenType) {
        DefaultDamagerRepairer damagerRepairer = new DefaultDamagerRepairer(scanner);
        pr.setDamager(damagerRepairer, tokenType);
        pr.setRepairer(damagerRepairer, tokenType);
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return PartitionScanner.CONTENT_TYPES;
    }

//    @Override
//    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
//        // TODO Preferences
//        ContentAssistant contentAssistant = new ContentAssistant();
//        contentAssistant.setInformationControlCreator(new IInformationControlCreator() {
//            public IInformationControl createInformationControl(Shell parent) {
//                DefaultInformationControl control = new DefaultInformationControl(parent, true);
//                return control;
//            }
//        });
//        contentAssistant.setContentAssistProcessor(new NodeContentAssistant(), IDocument.DEFAULT_CONTENT_TYPE);
//        contentAssistant.enableAutoActivation(true);
//        contentAssistant.setAutoActivationDelay(500);
//        return contentAssistant;
//    }

}
