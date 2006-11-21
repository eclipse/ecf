package org.eclipse.ecf.ui.wizards;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.ui.IConnectWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

public class EmptyConnectWizard extends Wizard implements
		IConnectWizard {

	protected IContainer container;

	public boolean performFinish() {
		return true;
	}

	public void init(IWorkbench workbench,
			IContainer container) {
		this.container = container;
	}

	public void addPages() {
		addPage(new EmptyConnectWizardPage("emptyConnectWizardPage"));
	}
}
