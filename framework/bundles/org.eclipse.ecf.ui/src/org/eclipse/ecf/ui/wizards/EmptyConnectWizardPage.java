package org.eclipse.ecf.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class EmptyConnectWizardPage extends WizardPage {

	protected EmptyConnectWizardPage(String pageName) {
		super(pageName);
	}

	public void createControl(Composite parent) {
		Font font = parent.getFont();

		Composite outerContainer = new Composite(parent, SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		outerContainer.setFont(font);
		Label label = new Label(outerContainer, SWT.NONE);
		label.setText("Connect finished");
		label.setFont(font);
		setControl(outerContainer);
		setPageComplete(true);
	}

}
