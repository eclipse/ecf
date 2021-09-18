/****************************************************************************
 * Copyright (c) 2021 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.ecf.core.util;

import java.io.Serializable;
import org.osgi.framework.Version;

/**
 * @since 3.10
 */
public final class SerVersion implements Serializable {
	private static final long serialVersionUID = 4785952431053485808L;
	private String versionStr;

	public SerVersion(Version v) {
		this.versionStr = v.toString();
	}

	public Version toVersion() {
		return Version.parseVersion(this.versionStr);
	}
}