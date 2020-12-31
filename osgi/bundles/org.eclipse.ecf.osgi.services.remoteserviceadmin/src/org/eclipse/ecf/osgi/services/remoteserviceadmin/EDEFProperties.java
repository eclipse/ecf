/****************************************************************************
 * Copyright (c) 2020 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 *   
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.DebugOptions;
import org.eclipse.ecf.internal.osgi.services.remoteserviceadmin.LogUtility;

/**
 * Class to represent EDEF properties for load from .properties file (via
 * {@link #loadEDEFProperties(InputStream)} or
 * {@link #loadEDEFProperties(Reader)}) or via store to .properties file (via
 * {@link #storeEDEFProperties(BufferedWriter, String)} or
 * {@link #storeEDEFProperties(Writer, String)}. This class is used by the
 * EndpointDescriptionLocator class to load from default.properties files as
 * well as properties edeffile.properties to override the values from the
 * edeffile.xml files specified by the Remote-Service header in manifest as per
 * the RSA specification (chap 122 in compendium spec).
 * 
 * @since 4.8
 * 
 */
public class EDEFProperties extends Properties {

	private static final long serialVersionUID = -7351470248095230347L;
	private static int nextInt = 0;
	private static long nextLong = 0;
	private static short nextShort = 0;
	private static byte nextByte = 0;
	private static final String[] typelist = { "array", "set", "list", "uuid", "string", "long", "double", "float", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"short", "int", "integer", "char", "character", "byte", "char", "boolean", "short", "byte" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$

	private static List<String> TYPES = null;

	static {
		TYPES = Arrays.asList(typelist);
	}

	public class Value {
		private String type1 = "string"; //$NON-NLS-1$
		private String type2 = "string"; //$NON-NLS-1$
		private String valueString;
		private Object valueObject;

		boolean isArray() {
			return this.type1.equalsIgnoreCase("array"); //$NON-NLS-1$
		}

		boolean isSet() {
			return this.type1.equalsIgnoreCase("set"); //$NON-NLS-1$
		}

		boolean isList() {
			return this.type1.equalsIgnoreCase("list"); //$NON-NLS-1$
		}

		boolean isCollection() {
			return isSet() || isList();
		}

		boolean isSimpleType() {
			return !isCollection() && !isArray();
		}

		boolean hasTypeAgreement(Value otherValue) {
			String otherType = (otherValue.isArray() || otherValue.isCollection()) ? otherValue.type2
					: otherValue.type1;
			return (isArray() || isCollection()) ? this.type2.equalsIgnoreCase(otherType)
					: this.type1.equalsIgnoreCase(otherType);
		}

		Value addPropertyValue(Value newValue) {
			if (!hasTypeAgreement(newValue)) {
				LogUtility.logError("addEDEFPropertyValue", DebugOptions.ENDPOINT_DESCRIPTION_LOCATOR, getClass(), //$NON-NLS-1$
						"type disagreement between property values for old type1=" + this.type1 + ",type2=" //$NON-NLS-1$ //$NON-NLS-2$
								+ this.type2 + ", and new type1=" + newValue.type1 + ",type2=" + newValue.type2); //$NON-NLS-1$ //$NON-NLS-2$
				return this;
			}
			// get old and new values
			Object ov = this.getValueObject();
			Object nv = newValue.getValueObject();
			// If we've got a collection already
			if (isCollection()) {
				Collection oldVs = (Collection) ov;
				// If newValue is also collection
				if (newValue.isCollection()) {
					// Then addAll
					oldVs.addAll((Collection) nv);
				} else if (newValue.isSimpleType()) {
					// Add single element
					oldVs.add(nv);
				}
			} else if (isArray()) {
				// Get old length
				int oldLength = Array.getLength(ov);
				Object newResultValue = null;
				if (newValue.isArray()) {
					// new value is also array...get length
					int newLength = Array.getLength(nv);
					// Check to make sure that the type of elements is same (type2 for arrays)
					newResultValue = Array.newInstance(ov.getClass().getComponentType(), oldLength + newLength);
					// copy ov contents to newResultValue
					System.arraycopy(ov, 0, newResultValue, 0, oldLength);
					// append nv contents to newResultValue after ov contents
					System.arraycopy(nv, 0, newResultValue, oldLength, newLength);
				} else if (newValue.isSimpleType()) {
					newResultValue = Array.newInstance(ov.getClass().getComponentType(), oldLength + 1);
					System.arraycopy(ov, 0, newResultValue, 0, oldLength);
					Array.set(newResultValue, oldLength, nv);
				}
				if (newResultValue != null) {
					this.valueObject = newResultValue;
				}
			}
			return this;
		}

		Value(String value) {
			// Split value with =. Two cases:
			// 1) the value has array=something, Long=somethingelse or array:long=1l,2l,3l.
			// This happens when the
			// properties parser parses ':' as the first separator, with name values like
			// this:
			// name1:array=something, or name2:Long=something
			// 2) The equals sign is somewhere in the value (as a string)
			String[] valueArr = value.split("="); //$NON-NLS-1$
			// To detect case 1, we split the first value by ':' and take the first array
			// element, which for case 1 will be (e.g.) array,list,long...or one of the
			// other supported types
			// Here we test: If case 1, the test below returns true and the block is
			// entered.
			if (TYPES.contains(valueArr[0].split(":")[0].toLowerCase())) { //$NON-NLS-1$
				// If the valueArray length > 1 then that means that
				if (valueArr.length > 1) {
					// split second element in valueArr by :
					String[] firstSplit = valueArr[0].split(":"); //$NON-NLS-1$
					// If more than one then type2 is second element in firstSplit
					if (firstSplit.length > 1) {
						this.type2 = firstSplit[1];
					}
					// In either case type1 is firstSplit[0]
					this.type1 = firstSplit[0];
				}
				// Now set value to the last elemtn in the valueArr
				this.valueString = valueArr[valueArr.length - 1];
			} else {
				// If case 2, we simply set the valueString equal to the given value.
				this.valueString = value;
			}
		}

		private void setType2(Collection coll) {
			Class<?> c = coll.iterator().next().getClass();
			if (!c.equals(String.class)) {
				this.type2 = c.getSimpleName().toLowerCase();
			}
		}

		Value(Object value) {
			Class<?> clazz = value.getClass();
			if (clazz.isArray()) {
				this.type1 = "array"; //$NON-NLS-1$
				Class<?> compType = clazz.getComponentType();
				if (!compType.equals(String.class)) {
					this.type2 = compType.getSimpleName().toLowerCase();
				}
			} else if (List.class.isInstance(value)) {
				this.type1 = "list"; //$NON-NLS-1$
				setType2((List) value);
			} else if (Set.class.isInstance(value)) {
				this.type1 = "set"; //$NON-NLS-1$
				setType2((Set) value);
			} else {
				String type = clazz.getSimpleName().toLowerCase();
				if (!type.equalsIgnoreCase("string")) { //$NON-NLS-1$
					this.type1 = type;
				}
			}
			this.valueObject = value;

		}

		private Object getSimpleValue(Class<?> simpleType, Object value) {
			try {
				return simpleType.getDeclaredMethod("valueOf", new Class[] { String.class }).invoke(null, value); //$NON-NLS-1$
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				LogUtility.logWarning("getSimpleValue", DebugOptions.ENDPOINT_DESCRIPTION_LOCATOR, this.getClass(), //$NON-NLS-1$
						"Cannot create instance of simpleType=" + simpleType + ", value=" + value); //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			}
		}

		boolean isUnique() {
			return "unique".equals(this.type2); //$NON-NLS-1$
		}

		Object readSimpleValue(String simpleType, String value) {
			switch (simpleType) {
			case "long": //$NON-NLS-1$
			case "Long": //$NON-NLS-1$
				if ("unique".equalsIgnoreCase(this.type2)) { //$NON-NLS-1$
					return getNextLong();
				} else if ("nanoTime".equalsIgnoreCase(this.type2)) { //$NON-NLS-1$
					return System.nanoTime();
				} else if ("milliTime".equalsIgnoreCase(this.type2)) { //$NON-NLS-1$
					return System.currentTimeMillis();
				}
				return getSimpleValue(Long.class, value);
			case "double": //$NON-NLS-1$
			case "Double": //$NON-NLS-1$
				return getSimpleValue(Double.class, value);
			case "float": //$NON-NLS-1$
			case "Float": //$NON-NLS-1$
				return getSimpleValue(Float.class, value);
			case "int": //$NON-NLS-1$
			case "integer": //$NON-NLS-1$
			case "Integer": //$NON-NLS-1$
				if ("unique".equals(this.type2)) { //$NON-NLS-1$
					return getNextInteger();
				}
				return getSimpleValue(Integer.class, value);
			case "Byte": //$NON-NLS-1$
			case "byte": //$NON-NLS-1$
				if ("unique".equals(this.type2)) { //$NON-NLS-1$
					return getNextByte();
				}
				return getSimpleValue(Byte.class, value);
			case "char": //$NON-NLS-1$
			case "Character": //$NON-NLS-1$
				return getSimpleValue(Character.class, value.toCharArray()[0]);
			case "boolean": //$NON-NLS-1$
			case "Boolean": //$NON-NLS-1$
				return getSimpleValue(Boolean.class, value);
			case "short": //$NON-NLS-1$
			case "Short": //$NON-NLS-1$
				if ("unique".equals(this.type2)) { //$NON-NLS-1$
					return getNextShort();
				}
				return getSimpleValue(Short.class, value);
			case "uuid": //$NON-NLS-1$
			case "Uuid": //$NON-NLS-1$
			case "UUID": //$NON-NLS-1$
				// we don't care whether 'unique' is given or not
				return UUID.randomUUID().toString();
			case "String": //$NON-NLS-1$
			case "string": //$NON-NLS-1$
				return value;
			default:
				return null;
			}
		}

		Object readArrayValues(String collectionValue) {
			String[] elements = this.valueString.split("\\s*,\\s*"); //$NON-NLS-1$
			Object result = null;
			for (int i = 0; i < elements.length; i++) {
				Object elementValue = readSimpleValue(this.type2, elements[i]);
				if (elementValue == null) {
					LogUtility.logWarning("getArrayValues", DebugOptions.ENDPOINT_DESCRIPTION_LOCATOR, getClass(), //$NON-NLS-1$
							"array element=" + elements[i] + " could not be created"); //$NON-NLS-1$//$NON-NLS-2$
					continue;
				} else {
					if (i == 0) {
						result = Array.newInstance(elementValue.getClass(), elements.length);
					}
					Array.set(result, i, elementValue);
				}
			}
			return result;
		}

		Collection<Object> readCollectionValues(Collection<Object> c, String collectionValue) {
			String[] elements = this.valueString.split("\\s*,\\s*"); //$NON-NLS-1$
			for (String element : elements) {
				Object elementValue = readSimpleValue(this.type2, element);
				if (elementValue == null) {
					LogUtility.logWarning("getCollectionValues", DebugOptions.ENDPOINT_DESCRIPTION_LOCATOR, //$NON-NLS-1$
							getClass(), "array element=" + element + " could not be created"); //$NON-NLS-1$//$NON-NLS-2$
					continue;
				} else {
					c.add(elementValue);
				}
			}
			return c;
		}

		public synchronized Object getValueObject() {
			if (this.valueObject == null) {
				switch (this.type1) {
				case "array": //$NON-NLS-1$
					this.valueObject = readArrayValues(this.valueString);
					break;
				case "list": //$NON-NLS-1$
					this.valueObject = readCollectionValues(new ArrayList(), this.valueString);
					break;
				case "set": //$NON-NLS-1$
					this.valueObject = readCollectionValues(new HashSet(), this.valueString);
					break;
				default:
					this.valueObject = readSimpleValue(this.type1, this.valueString);
					break;
				}
			}
			return this.valueObject;
		}

		public synchronized String getValueString() {
			if (this.valueString == null) {
				StringBuffer buf = new StringBuffer();
				if (isSimpleType()) {
					if (!this.type1.equalsIgnoreCase("string")) { //$NON-NLS-1$
						buf.append(":").append(this.type1); //$NON-NLS-1$
					}
					buf.append("=").append(this.valueObject.toString()); //$NON-NLS-1$
				} else {
					buf.append(":").append(this.type1); //$NON-NLS-1$
					if (!this.type2.equalsIgnoreCase("string")) { //$NON-NLS-1$
						buf.append(":").append(this.type2); //$NON-NLS-1$
					}
					buf.append("="); //$NON-NLS-1$
					Object[] arr = (Object[]) (isArray() ? valueObject : ((Collection) valueObject).toArray());
					for (int i = 0; i < arr.length; i++) {
						buf.append(arr[i].toString());
						if (i != (arr.length - 1)) {
							buf.append(","); //$NON-NLS-1$
						}
					}
				}
				this.valueString = buf.toString();
			}
			return valueString;
		}
	}

	synchronized static int getNextInteger() {
		if (nextInt == Integer.MAX_VALUE) {
			nextInt = 0;
		}
		return ++nextInt;
	}

	synchronized static long getNextLong() {
		if (nextLong == Long.MAX_VALUE) {
			nextLong = 0;
		}
		return ++nextLong;
	}

	synchronized static short getNextShort() {
		if (nextShort == Short.MAX_VALUE) {
			nextShort = 0;
		}
		return ++nextShort;
	}

	synchronized static byte getNextByte() {
		if (nextByte == Byte.MAX_VALUE) {
			nextByte = 0;
		}
		return ++nextByte;
	}

	/**
	 * Create empty EDEFProperties instance.
	 */
	public EDEFProperties() {
	}

	/**
	 * Create EDEFProperties instance initialized with all the given properties
	 * 
	 * @param properties must not be <code>null</code>
	 */
	public EDEFProperties(Map<String, Object> properties) {
		putEDEFProperties(properties);
	}

	@Override
	public Object put(Object key, Object value) {
		if (key instanceof String && value instanceof String) {
			Value oldValue = (Value) this.get(key);
			Value newValue = new Value((String) value);
			return super.put(key, (oldValue == null) ? newValue : oldValue.addPropertyValue(newValue));
		}
		return super.put(key, value);
	}

	/**
	 * Get EDEF Property Value given name
	 * 
	 * @param name the name/key of the EDEFProperty previously loaded or added. Must
	 *             not be <code>null</code>.
	 * @return Value the value found for given name/key. <code>Null</code> if not
	 *         found.
	 */
	public Value getValue(String name) {
		Object result = this.get(name);
		return (Value) ((result instanceof Value) ? result : null);
	}

	/**
	 * Put String->Object relation in as an EDEF property. Both the key and value
	 * must not be <code>null</code>. The value must be either a Set, List, Array
	 * type or a primitive type: Long/long, Byte/byte, Short/short, Int/Integer,
	 * char/Character, double/Double, float/Float. If array,set, or list, the
	 * elements must be one of the primitive types.
	 * 
	 * @param key   unique name/key for given value. Must not be <code>null</code>.
	 * @param value array,list,set of primitive type or instance of primitive type.
	 * @return existing Object with name/key. Null if no existing object exists.
	 */
	public Object putEDEFProperty(String name, Object value) {
		if (name != null && value != null) {
			return super.put(name, new Value(value));
		}
		return null;
	}

	/**
	 * Get EDEF properties as String -> Object map
	 * 
	 * @return Map<String,Object> containing all name->EDEFPropertiesValue contents
	 *         of this EDEFProperties
	 */
	public Map<String, Object> getEDEFPropertiesAsMap() {
		Map<String, Object> result = new TreeMap<String, Object>();
		this.forEach((k, v) -> {
			result.put((String) k, ((Value) v).getValueObject());
		});
		return result;
	}

	private static void writeComments0(BufferedWriter bw, String comments) throws IOException {
		bw.write("#"); //$NON-NLS-1$
		int len = comments.length();
		int current = 0;
		int last = 0;
		char[] uu = new char[6];
		uu[0] = '\\';
		uu[1] = 'u';
		while (current < len) {
			char c = comments.charAt(current);
			if (c > '\u00ff' || c == '\n' || c == '\r') {
				if (last != current)
					bw.write(comments.substring(last, current));
				if (c > '\u00ff') {
					uu[2] = toHex((c >> 12) & 0xf);
					uu[3] = toHex((c >> 8) & 0xf);
					uu[4] = toHex((c >> 4) & 0xf);
					uu[5] = toHex(c & 0xf);
					bw.write(new String(uu));
				} else {
					bw.newLine();
					if (c == '\r' && current != len - 1 && comments.charAt(current + 1) == '\n') {
						current++;
					}
					if (current == len - 1
							|| (comments.charAt(current + 1) != '#' && comments.charAt(current + 1) != '!'))
						bw.write("#"); //$NON-NLS-1$
				}
				last = current + 1;
			}
			current++;
		}
		if (last != current)
			bw.write(comments.substring(last, current));
		bw.newLine();
	}

	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	/**
	 * Load EDEF properties from the given input stream
	 * 
	 * @param ins InputStream to read the edef properties from. Must not be
	 *            <code>null</code>
	 * @throws IOException if properties cannot be read from given InputStream
	 */
	public synchronized void loadEDEFProperties(InputStream ins) throws IOException {
		load(ins);
	}

	/**
	 * Load EDEF properties from the given reader
	 * 
	 * @param reader Reader to read the edef properties from. Must not be
	 *               <code>null</code>
	 * @throws IOException if properties cannot be read from given Reader
	 */
	public void loadEDEFProperties(Reader reader) throws IOException {
		load(reader);
	}

	/**
	 * Store EDEF properties to given Writer
	 * 
	 * @param writer   the Writer to write output to. Must not be <code>null</code>.
	 * @param comments
	 * @throws IOException
	 */
	public void storeEDEFProperties(Writer writer, String comments) throws IOException {
		storeEDEFProperties((writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer),
				comments);
	}

	/**
	 * Store EDEF properties to the given buffered writer.
	 * 
	 * @param bufferedWriter the BufferedWriter to write to. Must not be
	 *                       <code>null</code>
	 * @param comments       Comment line prepended to properties file output. May
	 *                       be <code>null</code>.
	 * @throws IOException if properties cannot be written to bufferedWriter
	 */
	public void storeEDEFProperties(BufferedWriter bufferedWriter, String comments) throws IOException {
		if (comments != null) {
			writeComments0(bufferedWriter, comments);
		}
		bufferedWriter.write("#" + new Date().toString()); //$NON-NLS-1$
		bufferedWriter.newLine();
		synchronized (this) {
			for (Enumeration<?> e = keys(); e.hasMoreElements();) {
				Object k = e.nextElement();
				if (k instanceof String) {
					String key = (String) k;
					Object elemValue = get(key);
					if (elemValue instanceof Value) {
						bufferedWriter.write(key + ((Value) elemValue).getValueString());
						bufferedWriter.newLine();
					}
				}
			}
			bufferedWriter.flush();
		}
	}

	/**
	 * Put all the given properties into this map as EDEF properties, suitable for
	 * storing via {@link #storeEDEFProperties(Writer, String)}
	 * 
	 * @param properties the properties to put. May not be <code>null</code>
	 */
	public synchronized void putEDEFProperties(Map<String, Object> properties) {
		properties.forEach((k, v) -> {
			if (k instanceof String) {
				putEDEFProperty((String) k, v);
			}
		});
	}

}