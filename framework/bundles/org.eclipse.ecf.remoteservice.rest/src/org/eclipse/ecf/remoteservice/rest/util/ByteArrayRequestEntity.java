/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.util;

import org.eclipse.core.runtime.Assert;

public class ByteArrayRequestEntity implements IRequestEntity {

	private byte[] content;
	private String contentType;

	public ByteArrayRequestEntity(byte[] content, String contentType) {
		this.content = content;
		Assert.isNotNull(this.content);
		this.contentType = contentType;
	}

	public ByteArrayRequestEntity(byte[] content) {
		this.content = content;
		Assert.isNotNull(this.content);
	}

	public String getCharset() {
		return null;
	}

	public byte[] getContent() {
		return content;
	}

	public long getContentLength() {
		return 0;
	}

	public String getContentType() {
		return contentType;
	}

}
