package org.eclipse.ecf.ui.wizards;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.ui.IConfigurationWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

public class EmptyConfigurationWizard extends Wizard implements
		IConfigurationWizard {

	protected IContainer container;
	
	public boolean performFinish() {
		return true;
	}

	public IContainer getConfigurationResult() {
		return container;
	}

	public void init(IWorkbench workbench,
			ContainerTypeDescription containerDescription) {
		try {
			container = ContainerFactory.getDefault()
			.createContainer(containerDescription, null);
		} catch (ContainerCreateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
