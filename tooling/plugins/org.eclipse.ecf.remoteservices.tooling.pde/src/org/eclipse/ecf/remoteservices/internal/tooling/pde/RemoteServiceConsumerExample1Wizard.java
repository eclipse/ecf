/****************************************************************************
 * Copyright (c) 2023 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.remoteservices.internal.tooling.pde;

import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.IPluginContentWizard;
import org.eclipse.pde.ui.templates.ITemplateSection;
import org.eclipse.pde.ui.templates.NewPluginTemplateWizard;

public class RemoteServiceConsumerExample1Wizard extends NewPluginTemplateWizard implements
		IPluginContentWizard {

	public void init(IFieldData data) {
		super.init(data);
		setWindowTitle("Remote Services Consumer Example Wizard");
	}

	public ITemplateSection[] createTemplateSections() {
		return new ITemplateSection[] {new RemoteServiceConsumerExample1Template()};
	}

	public String[] getImportPackages() {
		return new String[] { "org.eclipse.ecf.osgi.services.distribution","org.eclipse.ecf.examples.remoteservices.hello","org.osgi.util.tracker","org.eclipse.ecf.remoteservice","org.eclipse.equinox.concurrent.future" };
	}

	public IPluginReference[] getDependencies(String schemaVersion) {
		return new IPluginReference[0];
	}

}

