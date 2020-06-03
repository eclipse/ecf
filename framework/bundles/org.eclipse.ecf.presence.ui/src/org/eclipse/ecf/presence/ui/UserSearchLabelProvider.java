/****************************************************************************
 * Copyright (c) 2008 Marcelo Mayworm.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: 	Marcelo Mayworm - initial API and implementation
 * 
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.presence.ui;

import org.eclipse.ecf.internal.presence.ui.Messages;
import org.eclipse.ecf.presence.search.IResult;
import org.eclipse.ecf.presence.search.IResultList;
import org.eclipse.ecf.ui.SharedImages;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for multiple users viewer. This label provider implements an
 * LabelProvider suitable for use by viewers that accepts LabelProvider as
 * input. This class may be subclassed in order to customize the
 * behavior/display of other label providers.
 * @since 2.0
 * 
 */
public class UserSearchLabelProvider extends LabelProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof IResultList)
			return SharedImages.getImage(SharedImages.IMG_GROUP);
		if (element instanceof IResult)
			return SharedImages.getImage(SharedImages.IMG_USER_AVAILABLE);

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof IResult) {
			return ((IResult) element).getUser().getName();
		} else if (element instanceof IResultList) {
			return ((IResultList) element).getResults().size() + Messages.UserSearchLabelProvider_ContactsFound;
		}
		return element.toString();
	}

}
