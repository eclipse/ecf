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

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ecf.internal.remoteservice.rest.Activator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class is a sample implementation of {@link IRestResource}. This will be used
 * to create XML Resource representations and will be registered when the API is 
 * started, {@link Activator#start(org.osgi.framework.BundleContext)}.
 */
public class XMLResource implements IRestResource {

	public String getIdentifier() {
		return "ecf.rest.resource.xml";
	}

	public Object createRepresentation(String responseBody) throws IllegalArgumentException {
		DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		String errorMsg = "Response can't be parsed, reason: ";
		try {
			DocumentBuilder builder = documentFactory.newDocumentBuilder();
			InputSource src = new InputSource(new StringReader(responseBody));
			Document dom = builder.parse(src);			
			return dom;
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException(errorMsg + e.getMessage());
		} catch (SAXException e) {
			throw new IllegalArgumentException(errorMsg + e.getMessage());
		} catch (IOException e) {
			throw new IllegalArgumentException(errorMsg + e.getMessage());
		}
		
		

	}

}
