/****************************************************************************
 * Copyright (c)2008 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ECFCertificateException extends CertificateException {

	private static final long serialVersionUID = 3726926966308967473L;

	private X509Certificate[] certs;
	private String type;

	public ECFCertificateException(String msg, X509Certificate[] certs, String type) {
		super(msg);
		this.certs = certs;
		this.type = type;
	}

	public X509Certificate[] getCertificates() {
		return certs;
	}

	public String getType() {
		return type;
	}

}
