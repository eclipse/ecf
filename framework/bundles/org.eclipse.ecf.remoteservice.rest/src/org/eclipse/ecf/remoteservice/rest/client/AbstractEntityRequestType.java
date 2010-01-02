/*******************************************************************************
* Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Composent, Inc. - initial API and implementation
******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.client;

import java.io.*;
import java.util.Map;
import org.apache.commons.httpclient.methods.*;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.client.IRemoteCallParameter;
import org.eclipse.ecf.remoteservice.client.IRemoteCallable;

public abstract class AbstractEntityRequestType extends AbstractRequestType {

	public static final Object CONTENT_LENGTH_PARAM_NAME = "contentLength"; //$NON-NLS-1$
	public static final Object CONTENT_TYPE_PARAM_NAME = "contentType"; //$NON-NLS-1$
	public static final Object CHARSET_PARAM_NAME = "charset"; //$NON-NLS-1$

	public AbstractEntityRequestType(Map defaultRequestHeaders) {
		super(defaultRequestHeaders);
	}

	public AbstractEntityRequestType() {
		super();
	}

	protected RequestEntity generateRequestEntity(String uri, IRemoteCall call, IRemoteCallable callable, IRemoteCallParameter paramDefault, Object paramToSerialize) throws NotSerializableException {
		if (paramToSerialize instanceof RequestEntity)
			return (RequestEntity) paramToSerialize;
		if (paramToSerialize instanceof InputStream) {
			String contentType = getContentType(call, callable, paramDefault);
			long contentLength = getContentLength(call, callable, paramDefault);
			return new InputStreamRequestEntity((InputStream) paramToSerialize, contentLength, contentType);
		} else if (paramToSerialize instanceof String) {
			String charset = getCharset(call, callable, paramDefault);
			String contentType = getContentType(call, callable, paramDefault);
			try {
				return new StringRequestEntity((String) paramToSerialize, contentType, charset);
			} catch (UnsupportedEncodingException e) {
				throw new NotSerializableException("Could not create request entity from call parameters: " + e.getMessage()); //$NON-NLS-1$
			}
		} else if (paramToSerialize instanceof byte[]) {
			String contentType = getContentType(call, callable, paramDefault);
			return new ByteArrayRequestEntity((byte[]) paramToSerialize, contentType);
		} else if (paramToSerialize instanceof File) {
			String contentType = getContentType(call, callable, paramDefault);
			return new FileRequestEntity((File) paramToSerialize, contentType);
		}
		throw new NotSerializableException("Remote call parameter with name=" + paramDefault.getName() + " is incorrect type for creating request entity."); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected String getCharset(IRemoteCall call, IRemoteCallable callable, IRemoteCallParameter paramDefault) {
		IRemoteCallParameter[] defaultParameters = callable.getDefaultParameters();
		if (defaultParameters != null) {
			for (int i = 0; i < defaultParameters.length; i++) {
				if (CHARSET_PARAM_NAME.equals(defaultParameters[i].getName())) {
					Object o = defaultParameters[i].getValue();
					if (o instanceof String) {
						return (String) o;
					}
				}
			}
		}
		return null;
	}

	protected long getContentLength(IRemoteCall call, IRemoteCallable callable, IRemoteCallParameter paramDefault) {
		IRemoteCallParameter[] defaultParameters = callable.getDefaultParameters();
		if (defaultParameters != null) {
			for (int i = 0; i < defaultParameters.length; i++) {
				if (CONTENT_LENGTH_PARAM_NAME.equals(defaultParameters[i].getName())) {
					Object o = defaultParameters[i].getValue();
					if (o instanceof Number) {
						return ((Number) o).longValue();
					} else if (o instanceof String) {
						try {
							return Integer.parseInt((String) o);
						} catch (NumberFormatException e) {
							return InputStreamRequestEntity.CONTENT_LENGTH_AUTO;
						}
					}
				}
			}
		}
		return InputStreamRequestEntity.CONTENT_LENGTH_AUTO;
	}

	private String getContentType(IRemoteCall call, IRemoteCallable callable, IRemoteCallParameter paramDefault) {
		IRemoteCallParameter[] defaultParameters = callable.getDefaultParameters();
		if (defaultParameters != null) {
			for (int i = 0; i < defaultParameters.length; i++) {
				if (CONTENT_TYPE_PARAM_NAME.equals(defaultParameters[i].getName())) {
					Object o = defaultParameters[i].getValue();
					if (o instanceof String) {
						return (String) o;
					}
				}
			}
		}
		return null;
	}
}
