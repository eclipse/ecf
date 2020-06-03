/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
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

package org.eclipse.ecf.internal.filetransfer.ui.hyperlink;

import java.net.URI;
import org.eclipse.ecf.internal.filetransfer.ui.GetFileHandler;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 */
public class SCPHyperlink implements IHyperlink {

	Shell shell;
	IRegion region;
	URI uri;

	public SCPHyperlink(Shell shell, IRegion region, URI uri) {
		this.shell = shell;
		this.region = region;
		this.uri = uri;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ecf.ui.hyperlink.AbstractURLHyperlink#open()
	 */
	public void open() {
		GetFileHandler getFileHandler = new GetFileHandler();
		getFileHandler.openStartFileDownloadDialog(shell, uri.toString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkRegion()
	 */
	public IRegion getHyperlinkRegion() {
		return region;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getHyperlinkText()
	 */
	public String getHyperlinkText() {
		return uri.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.hyperlink.IHyperlink#getTypeLabel()
	 */
	public String getTypeLabel() {
		return null;
	}
}
