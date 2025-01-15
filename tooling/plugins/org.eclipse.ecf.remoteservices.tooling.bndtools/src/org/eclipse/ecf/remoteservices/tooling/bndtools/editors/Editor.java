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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;


/**
 * copied from NodeclipseNodejsEditor
 * TODO base classes, color theme support
 * 
 * @author Paul Verest
 */
public class Editor extends TextEditor {

    public static final String EDITOR_ID = "org.nodeclipse.enide.editors.pom.grovy.Editor";
    public static final String RULER_CONTEXT = EDITOR_ID + ".ruler";
    public final static String EDITOR_MATCHING_BRACKETS = "matchingBrackets";
    public final static String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor";

    private DefaultCharacterPairMatcher matcher;

    public Editor() {
        setSourceViewerConfiguration(new NodeSourceViewerConfiguration());
    }

    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        setRulerContextMenuId(RULER_CONTEXT);
        setDocumentProvider(new NodeDocumentProvider());
    }

    @Override
    protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        super.configureSourceViewerDecorationSupport(support);

        char[] matchChars = { '(', ')', '[', ']', '{', '}' }; // which brackets
                                                              // to match
        matcher = new DefaultCharacterPairMatcher(matchChars, IDocumentExtension3.DEFAULT_PARTITIONING);
        support.setCharacterPairMatcher(matcher);
        support.setMatchingCharacterPainterPreferenceKeys(EDITOR_MATCHING_BRACKETS, EDITOR_MATCHING_BRACKETS_COLOR);

        // Enable bracket highlighting in the preference store
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(EDITOR_MATCHING_BRACKETS, true);
        store.setDefault(EDITOR_MATCHING_BRACKETS_COLOR, "128,128,128");
    }

}

