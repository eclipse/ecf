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

import java.io.InputStream;
import org.eclipse.core.runtime.Assert;

public class InputStreamRequestEntity implements IRequestEntity {

	public static final int CONTENT_LENGTH_AUTO = -2;

	private InputStream content;
	private String contentType;
	private long contentLength;

	public InputStreamRequestEntity(InputStream content, long contentLength, String contentType) {
		this.content = content;
		Assert.isNotNull(content);
		this.contentLength = contentLength;
		this.contentType = contentType;
	}

	public InputStreamRequestEntity(InputStream content, long contentLength) {
		this(content, contentLength, null);
	}

	public InputStreamRequestEntity(InputStream content, String contentType) {
		this(content, CONTENT_LENGTH_AUTO, contentType);
	}

	public InputStreamRequestEntity(InputStream content) {
		this(content, null);
	}

	public String getCharset() {
		return null;
	}

	public InputStream getContent() {
		return content;
	}

	public long getContentLength() {
		return contentLength;
	}

	public String getContentType() {
		return contentType;
	}

}
