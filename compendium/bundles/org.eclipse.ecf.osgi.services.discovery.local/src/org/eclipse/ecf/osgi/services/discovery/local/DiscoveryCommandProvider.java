/*******************************************************************************
 * Copyright (c) 2009 Markus Alexander Kuppe.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (ecf-dev_eclipse.org <at> lemmster <dot> de) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.discovery.local;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ecf.osgi.services.discovery.ServiceEndpointDescription;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.SAXException;

public class DiscoveryCommandProvider implements CommandProvider {

	private static final String LINE_SEPARATOR = System
			.getProperty("line.separator"); //$NON-NLS-0$

	private FileBasedDiscoveryImpl discovery = null;

	public DiscoveryCommandProvider(FileBasedDiscoveryImpl discovery) {
		super();
		this.discovery = discovery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.osgi.framework.console.CommandProvider#getHelp()
	 */
	public String getHelp() {
		return LINE_SEPARATOR //$NON-NLS-0$
				+ Messages
						.getString("DiscoveryCommandProvider.CommandHeadline") //$NON-NLS-1$
				+ LINE_SEPARATOR //$NON-NLS-0$
				+ Messages.getString("DiscoveryCommandProvider.CommandHelp") //$NON-NLS-1$
				+ LINE_SEPARATOR; //$NON-NLS-0$
	}

	public void _publish(CommandInterpreter ci) {
		Collection seds = getServiceEndpointDescriptions(ci);
		for (Iterator iterator = seds.iterator(); iterator.hasNext();) {
			ServiceEndpointDescription sed = (ServiceEndpointDescription) iterator
					.next();
			discovery.publishService(sed);
		}
	}

	public void _unpublish(CommandInterpreter ci) {
		Collection seds = getServiceEndpointDescriptions(ci);
		for (Iterator iterator = seds.iterator(); iterator.hasNext();) {
			ServiceEndpointDescription sed = (ServiceEndpointDescription) iterator
					.next();
			discovery.unpublishService(sed);
		}
	}

	private Collection getServiceEndpointDescriptions(CommandInterpreter ci) {
		String path = ci.nextArgument();
		try {
			URL url = new URL(path);
			InputStream inputStream = url.openStream();
			return new ServiceDescriptionParser().load(inputStream);
		} catch (IOException e) {
			ci.print(NLS.bind(Messages
					.getString("DiscoveryCommandProvider.IOException"), path)); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			ci
					.print(Messages
							.getString("DiscoveryCommandProvider.ParserConfigurationException")); //$NON-NLS-1$
		} catch (SAXException e) {
			ci.print(NLS.bind(Messages
					.getString("DiscoveryCommandProvider.SAXException"), path)); //$NON-NLS-1$
		}
		return new ArrayList();
	}
}
