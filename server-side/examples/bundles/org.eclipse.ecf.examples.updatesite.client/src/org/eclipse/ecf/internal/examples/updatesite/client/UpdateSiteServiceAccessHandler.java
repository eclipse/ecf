/****************************************************************************
 * Copyright (c) 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Markus Alexander Kuppe (Versant GmbH) - https://bugs.eclipse.org/259041
 *****************************************************************************/

package org.eclipse.ecf.internal.examples.updatesite.client;

import java.net.MalformedURLException;
import java.net.URI;
import org.eclipse.core.commands.*;
import org.eclipse.ecf.discovery.IServiceInfo;
import org.eclipse.ecf.discovery.ui.DiscoveryHandlerUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.SiteBookmark;
import org.eclipse.update.internal.ui.model.UpdateModel;
import org.eclipse.update.ui.UpdateManagerUI;

public class UpdateSiteServiceAccessHandler extends AbstractHandler {
	private static final String NAME = "name"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IServiceInfo serviceInfo = DiscoveryHandlerUtil.getActiveIServiceInfoChecked(event);

		try {
			final URI location = serviceInfo.getLocation();
			final String name = serviceInfo.getServiceProperties().getPropertyString(NAME);

			final UpdateModel model = UpdateUI.getDefault().getUpdateModel();
			final SiteBookmark bookmark = new SiteBookmark(name, location.toURL(), false);
			bookmark.setSelected(true);
			model.addBookmark(bookmark);
			model.saveBookmarks();
			UpdateManagerUI.openInstaller(Display.getDefault().getActiveShell());
		} catch (MalformedURLException e) {
			throw new ExecutionException(e.getMessage(), e);
		}
		return null;
	}
}
