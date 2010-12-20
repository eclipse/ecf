/*******************************************************************************
 * Copyright (c) 2010-2011 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class EndpointDescriptionWriter implements IEndpointDescriptionWriter {

	protected String indent = "  ";

	public void writeEndpointDescriptions(Writer writer,
			EndpointDescription[] endpointDescriptions) throws IOException {

		writeEndpointDescriptionsElementOpen(0, writer);
		for (int i = 0; i < endpointDescriptions.length; i++) {
			writeEndpointDescription(1, writer, endpointDescriptions[i]);
		}
		writeEndpointDescriptionsElementClose(0, writer);
	}

	private Writer newLine(Writer writer) throws IOException {
		return writer.append("\n");
	}

	private Writer indent(int indentLevel, Writer writer) throws IOException {
		for (int i = 0; i < indentLevel; i++)
			writer.append(indent);
		return writer;
	}

	private void writeEndpointDescription(int indentLevel, Writer writer,
			EndpointDescription endpointDescription) throws IOException {
		writeEndpointDescriptionElementOpen(indentLevel, writer);
		writeProperties(indentLevel, writer, endpointDescription);
		writeEndpointDescriptionElementClose(indentLevel, writer);
	}

	private void writeEndpointDescriptionElementClose(int indentLevel,
			Writer writer) throws IOException {
		indent(indentLevel, writer);
		writer.append("</endpoint-description>");
		newLine(writer);
	}

	private void writeProperties(int indentLevel, Writer writer,
			EndpointDescription endpointDescription) throws IOException {
		Map<String, Object> properties = endpointDescription.getProperties();
		for (String name : properties.keySet())
			writeProperty(indentLevel + 1, name, properties.get(name), writer);
	}

	private void writeProperty(int indentLevel, String name, Object value,
			Writer writer) throws IOException {
		if (value != null) {
			String multiValueType = getMultiValueType(value);
			if (multiValueType != null) {
				writeMultiValueProperty(indentLevel, name, value, writer);
			} else {
				String valueType = getValueType(value);
				writeValueProperty(indentLevel, name, valueType, value, writer);
			}
		}
	}

	private void writeValueProperty(int indentLevel, String name,
			String valueType, Object value, Writer writer) throws IOException {
		indent(indentLevel, writer);
		writer.append("<property name=\"").append(name)
				.append("\" value-type=\"").append(valueType)
				.append("\" value=").append(value.toString()).append("\"/>");
		newLine(writer);
	}

	private void writeMultiValueProperty(int indentLevel, String name,
			Object value, Writer writer) {
		// TODO Auto-generated method stub

	}

	private String getMultiValueType(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	private String getValueType(Object value) {
		// first determine if is array
		if (value instanceof String)
			return "String";
		else if (value instanceof Long)
			return "Long";
		else if (value instanceof Double)
			return "Double";
		else if (value instanceof Float)
			return "Float";
		else if (value instanceof Integer)
			return "Integer";
		else if (value instanceof Byte)
			return "Byte";
		else if (value instanceof Character)
			return "Character";
		else if (value instanceof Boolean)
			return "Boolean";
		else if (value instanceof Short)
			return "Short";
		return null;
	}

	private void writeEndpointDescriptionElementOpen(int indentLevel,
			Writer writer) throws IOException {
		indent(indentLevel, writer);
		writer.append("<endpoint-description>");
		newLine(writer);
	}

	private void writeEndpointDescriptionsElementClose(int indentLevel,
			Writer writer) throws IOException {
		indent(indentLevel, writer);
		writer.append("<endpoint-descriptions xmlns=\"http://www.osgi.org/xmlns/rsa/v1.0.0\">");
		newLine(writer);
	}

	private void writeEndpointDescriptionsElementOpen(int indentLevel,
			Writer writer) throws IOException {
		indent(indentLevel, writer);
		writer.append("</endpoint-descriptions>");
		newLine(writer);
	}

}
