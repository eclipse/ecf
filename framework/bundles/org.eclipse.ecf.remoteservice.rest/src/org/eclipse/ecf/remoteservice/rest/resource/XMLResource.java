/******************************************************************************* 
 * Copyright (c) 2009 EclipseSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.remoteservice.rest.resource;

import java.util.Map;
import org.eclipse.ecf.remoteservice.IRemoteCall;
import org.eclipse.ecf.remoteservice.rest.IRestCallable;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.remoteservice.rest.Activator;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * This class is a sample implementation of {@link IRestResourceProcessor}. This will be
 * used to create XML Resource representations and will be registered when the
 * API is started, {@link Activator#start(org.osgi.framework.BundleContext)}.
 */
public class XMLResource implements IRestResourceProcessor {

	public Object createResponseRepresentation(IRemoteCall call, IRestCallable callable, Map responseHeaders, String responseBody) throws ECFException {
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		String errorMsg = "XML response can't be parsed: {0}"; //$NON-NLS-1$
		try {
			DocumentBuilder builder = documentFactory.newDocumentBuilder();
			InputSource src = new InputSource(new StringReader(responseBody));
			Document dom = builder.parse(src);
			return dom;
		} catch (Exception e) {
			throw new ECFException(NLS.bind(errorMsg, e.getMessage()));
		}

	}

}
