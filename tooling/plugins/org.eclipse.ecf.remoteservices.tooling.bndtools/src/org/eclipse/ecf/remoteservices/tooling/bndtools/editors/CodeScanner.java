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

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;

/**
 * JavaScript code scanner for source code highlighting.
 * @author Benjamin gurok
 * @author Paul Verest
 */
public class CodeScanner extends RuleBasedScanner {

    private TextAttribute commentAttribute = new TextAttribute(EditorColors.getColor(Constants.KEY_COLOR_COMMENT), null, SWT.NORMAL);
    private TextAttribute docAttribute = new TextAttribute(EditorColors.getColor(Constants.KEY_COLOR_DOC), null, SWT.NORMAL);
    private TextAttribute keywordAttribute = new TextAttribute(EditorColors.getColor(Constants.KEY_COLOR_KEYWORD), null, SWT.BOLD);
    private TextAttribute stringAttribute = new TextAttribute(EditorColors.getColor(Constants.KEY_COLOR_STRING), null, SWT.NORMAL);
    private TextAttribute numberAttribute = new TextAttribute(EditorColors.getColor(Constants.KEY_COLOR_NUMBER), null, SWT.NORMAL);
    private TextAttribute normalAttribute = new TextAttribute(EditorColors.getColor(Constants.KEY_COLOR_NORMAL), null, SWT.NORMAL);

    public CodeScanner() {
        createRules();
    }

    public TextAttribute getCommentAttribute() {
        return commentAttribute;
    }

    public TextAttribute getDocAttribute() {
        return docAttribute;
    }

    /**
     * Use the default Eclipse higlighting scheme.
     */
    private void createRules() {
        Token keywordToken = new Token(keywordAttribute);
        Token commentToken = new Token(commentAttribute);
        Token docToken = new Token(docAttribute);
        Token stringToken = new Token(stringAttribute);
        Token numberToken = new Token(numberAttribute);
        Token normalToken = new Token(normalAttribute);

        setDefaultReturnToken(normalToken);

        setRules(new IRule[] { new EndOfLineRule("//", commentToken),//$NON-NLS-2$
                new KeywordRule(keywordToken),//$NON-NLS-2$
                new MultiLineRule("/**", "*/", docToken, (char) 0, false), //$NON-NLS-2$
                new MultiLineRule("/*", "*/", commentToken, (char) 0, false), //$NON-NLS-2$
                new SingleLineRule("\"", "\"", stringToken, '\\'), //$NON-NLS-2$
                // Regexp
                new SingleLineRule("/", "/", stringToken, '\\'), //$NON-NLS-2$
                new SingleLineRule("'", "'", stringToken, '\\'), //$NON-NLS-2$
                new WhitespaceRule(new IWhitespaceDetector() {
                    public boolean isWhitespace(char c) {
                        return Character.isWhitespace(c);
                    }
                }),//$NON-NLS-2$
                new WordRule(new WordDetector(), normalToken),//$NON-NLS-2$
                new NumberRule(numberToken) });
    }

    private static class KeywordRule extends WordRule {

        public KeywordRule(Token token) {
            super(new WordDetector());
            for (String word : Words.KEYWORDS) {
                addWord(word, token);
            }
         }
    }

    private static class WordDetector implements IWordDetector {

        public boolean isWordPart(char c) {
            return Character.isJavaIdentifierPart(c);
        }

        public boolean isWordStart(char c) {
            return Character.isJavaIdentifierStart(c);
        }

    }
}
