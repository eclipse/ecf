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

public class StringRequestEntity implements IRequestEntity {

	private String content;
	private String contentType;
	private String charset;

	public StringRequestEntity(String content, String contentType, String charset) {
		this.content = content;
		Assert.isNotNull(content);
		this.contentType = contentType;
		this.charset = charset;
	}

	public StringRequestEntity(String content, String contentType) {
		this(content, contentType, null);
	}

	public StringRequestEntity(String content) {
		this(content, null);
	}

	public String getCharset() {
		return charset;
	}

	public String getContent() {
		return content;
	}

	public long getContentLength() {
		return 0;
	}

	public String getContentType() {
		return contentType;
	}

}
