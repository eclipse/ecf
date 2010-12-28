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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EndpointDescriptionWriter {

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
			MultiValueProperty multiValueProperty = getMultiValueProperty(name, value);
			if (multiValueProperty != null) {
				multiValueProperty.writeProperty(indentLevel, writer);
				return;
			}
			String valueType = getValueType(value);
			if (valueType != null) writeValueProperty(indentLevel, name, valueType, value, writer);
		}
	}

	abstract class MultiValueProperty {
		private String key;
		private Object value;
		
		public MultiValueProperty(String key, Object value) {
			this.key = key;
			this.value = value;
		}
		
		abstract String getValueType();
		abstract void writePropertyValues(int indentLevel, Writer writer) throws IOException;
		
		public void writeProperty(int indentLevel, Writer writer) throws IOException {
			indent(indentLevel, writer);
			writer.append("<property name=\"").append(key)
					.append("\" value-type=\"").append(getValueType()).append("\">");
			newLine(writer);
			writePropertyValues(indentLevel+1,writer);
			indent(indentLevel,writer);
			writer.append("</property>");
			newLine(writer);
		}
		
		void writePropertyValue(int indentLevel, Object value, Writer writer) throws IOException {
			indent(indentLevel,writer);
			writer.append("<value>").append(value.toString()).append("</value>");
			newLine(writer);
		}
		
		protected Object getValue() {
			return value;
		}
	}
	
	class SetProperty extends MultiValueProperty {
		public SetProperty(String key, Set value) {
			super(key,value);
		}

		public String getValueType() {
			return EndpointDescriptionWriter.this.getValueType(((Set) getValue()).iterator().next());
		}

		void writePropertyValues(int indentLevel, Writer writer) throws IOException {
			Set s = (Set) getValue();
			indent(indentLevel,writer);
			writer.append("<set>");
			newLine(writer);
			for(Iterator i=s.iterator(); i.hasNext(); ) writePropertyValue(indentLevel+1,i.next(),writer);
			indent(indentLevel,writer);
			writer.append("</set>");
		}
		
	}
	
	class ListProperty extends MultiValueProperty {
		public ListProperty(String key, List value) {
			super(key,value);
		}
		public String getValueType() {
			return EndpointDescriptionWriter.this.getValueType(((List) getValue()).iterator().next());
		}
		void writePropertyValues(int indentLevel, Writer writer) throws IOException {
			List l = (List) getValue();
			indent(indentLevel,writer);
			writer.append("<list>");
			newLine(writer);
			for(Iterator i=l.iterator(); i.hasNext(); ) writePropertyValue(indentLevel+1,i.next(),writer);
			indent(indentLevel,writer);
			writer.append("</list>");
			newLine(writer);
		}
	}

	class ArrayProperty extends MultiValueProperty {
		public ArrayProperty(String key, Object[] value) {
			super(key,value);
		}

		public String getValueType() {
			return EndpointDescriptionWriter.this.getValueType(((Object[]) getValue())[0]);
		}
		void writePropertyValues(int indentLevel, Writer writer) throws IOException {
			Object[] a = (Object[]) getValue();
			indent(indentLevel,writer);
			writer.append("<array>");
			newLine(writer);
			for(int i=0; i < a.length; i++) writePropertyValue(indentLevel+1,a[i],writer);
			indent(indentLevel,writer);
			writer.append("</array>");
			newLine(writer);
		}
	}

	private void writeValueProperty(int indentLevel, String name,
			String valueType, Object value, Writer writer) throws IOException {
		indent(indentLevel, writer);
		writer.append("<property name=\"").append(name)
				.append("\" value-type=\"").append(valueType)
				.append("\" value=\"").append(value.toString()).append("\"/>");
		newLine(writer);
	}

	private MultiValueProperty getMultiValueProperty(String key, Object value) {
		if (value instanceof Set) {
			Set s = (Set) value;
			Object first = s.iterator().next();
			if (first == null) return null;
			return new SetProperty(key,s);
		} else if (value instanceof List) {
			List l = (List) value;
			Object first = l.get(0);
			if (first == null) return null;
			return new ListProperty(key,l);
		} else if (value.getClass().isArray()) {
			Object[] a = (Object[]) value;
			if (a.length == 0 || a[0] == null) return null;
			return new ArrayProperty(key,a);
		}
		return null;
	}

	String getValueType(Object value) {
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
		writer.append("</endpoint-descriptions>");
		newLine(writer);
	}

	private void writeEndpointDescriptionsElementOpen(int indentLevel,
			Writer writer) throws IOException {
		indent(indentLevel, writer);
		writer.append("<endpoint-descriptions xmlns=\"http://www.osgi.org/xmlns/rsa/v1.0.0\">");
		newLine(writer);
	}

}
