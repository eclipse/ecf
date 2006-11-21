package org.eclipse.ecf.ui.wizards;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.ui.ContainerHolder;
import org.eclipse.ecf.ui.IConfigurationWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;

public class EmptyConfigurationWizard extends Wizard implements
		IConfigurationWizard {

	protected IContainer container;
	
	protected ContainerTypeDescription containerDescription;
	
	protected Object [] containerParameters = null;
	
	protected ContainerTypeDescription getContainerTypeDescription() {
		return containerDescription;
	}
	
	public boolean performFinish() {
		try {
			container = ContainerFactory.getDefault()
			.createContainer(containerDescription, containerParameters);
			return true;
		} catch (ContainerCreateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public ContainerHolder getConfigurationResult() {
		return new ContainerHolder(containerDescription, container);
	}

	public void init(IWorkbench workbench,
			ContainerTypeDescription containerDescription) {
		this.containerDescription = containerDescription;
	}

	public void addPages() {
		addPage(new FinishedConfigurationWizardPage("finishPage"));
	}
}
