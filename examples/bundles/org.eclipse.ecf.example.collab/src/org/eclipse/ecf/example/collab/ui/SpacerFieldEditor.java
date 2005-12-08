package org.eclipse.ecf.example.collab.ui;

import org.eclipse.swt.widgets.Composite;

/**
 * A field editor for adding space to a preference page.
 */
public class SpacerFieldEditor extends LabelFieldEditor {
	// Implemented as an empty label field editor.
	public SpacerFieldEditor(Composite parent) {
		super("", parent);
	}
}
