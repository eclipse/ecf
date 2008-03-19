/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.examples.updatesite.client;

import java.net.*;
import java.util.Arrays;
import java.util.List;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.identity.IServiceID;
import org.eclipse.ecf.discovery.ui.views.IServiceAccessHandler;
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.SiteBookmark;
import org.eclipse.update.internal.ui.model.UpdateModel;
import org.eclipse.update.ui.UpdateManagerUI;

public class UpdateSiteServiceAccessHandler implements IServiceAccessHandler {

	static final String SERVICE = Messages.UpdateSiteServiceAccessHandler_UPDATESITE_SERVICE;
	static final String PATH = "path"; //$NON-NLS-1$
	static final String NAME = "name"; //$NON-NLS-1$
	static final String BROWSER_PATH_SUFFIX = Messages.UpdateSiteServiceAccessHandler_UPDATESITE_INDEX_HTML;

	static final IContributionItem[] EMPTY_CONTRIBUTION = {};

	public UpdateSiteServiceAccessHandler() {
		// nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.discovery.ui.views.IServiceAccessHandler#getContributionsForService(org.eclipse.ecf.discovery.IServiceInfo)
	 */
	public IContributionItem[] getContributionsForService(IServiceInfo serviceInfo) {
		final IServiceID serviceID = serviceInfo.getServiceID();
		final List serviceTypes = Arrays.asList(serviceID.getServiceTypeID().getServices());
		String protocol = null;
		if (serviceTypes.contains(SERVICE))
			protocol = "http"; //$NON-NLS-1$
		if (protocol == null)
			return EMPTY_CONTRIBUTION;
		final URI location = serviceInfo.getLocation();
		final StringBuffer buf = new StringBuffer(protocol);
		buf.append("://").append(location.getHost()); //$NON-NLS-1$
		if (location.getPort() != -1)
			buf.append(":").append(location.getPort()); //$NON-NLS-1$ 
		final String path = serviceInfo.getServiceProperties().getPropertyString(PATH);
		if (path != null) {
			if (!path.startsWith("/"))buf.append("/"); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append(path);
		}
		final String name = serviceInfo.getServiceProperties().getPropertyString(NAME);
		final String urlString = buf.toString();
		final IAction openUpdateSiteAction = new Action() {
			public void run() {
				addURLToUpdateSite(name, urlString);
			}
		};

		openUpdateSiteAction.setText(Messages.UpdateSiteServiceAccessHandler_OPEN_INSTALLER_MENU_TEXT);

		final Action browserAction = new Action() {
			public void run() {
				openBrowser(urlString + BROWSER_PATH_SUFFIX);
			}
		};
		browserAction.setText(Messages.UpdateSiteServiceAccessHandler_OPEN_BROWSER_MENU_TEXT);

		return new IContributionItem[] {new ActionContributionItem(openUpdateSiteAction), new ActionContributionItem(browserAction)};
	}

	private void addURLToUpdateSite(String name, String urlString) {
		try {
			final UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			final SiteBookmark bookmark = new SiteBookmark(name, new URL(urlString), false);
			bookmark.setSelected(true);
			model.addBookmark(bookmark);
			model.saveBookmarks();

			UpdateManagerUI.openInstaller(Display.getDefault().getActiveShell());
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		}
	}

	protected void openBrowser(String urlString) {
		final IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			support.createBrowser(null).openURL(new URL(urlString));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
