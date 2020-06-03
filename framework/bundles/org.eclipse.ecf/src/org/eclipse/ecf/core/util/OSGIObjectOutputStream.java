/****************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors: Composent, Inc. - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.core.util;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.osgi.dto.DTO;
import org.osgi.framework.Version;
import org.osgi.service.log.LogService;

/**
 * @since 3.9
 */
public class OSGIObjectOutputStream extends ObjectOutputStream implements OSGIObjectStreamConstants {

	protected final ObjectOutputStream out;
	protected LogService logger;
	protected boolean allowNonSerializable = false;

	public OSGIObjectOutputStream(OutputStream out, boolean allowNonSerializable, LogService log) throws IOException {
		super();
		this.out = new ObjectOutputStream(out);
		this.allowNonSerializable = allowNonSerializable;
		this.logger = log;
	}

	public OSGIObjectOutputStream(OutputStream out, boolean allowNonSerializable) throws IOException {
		this(out, allowNonSerializable, null);
	}

	public OSGIObjectOutputStream(OutputStream out) throws IOException {
		this(out, false, null);
	}

	public void setAllowNonSerializable(boolean value) {
		this.allowNonSerializable = value;
	}

	public void setLogService(LogService log) {
		this.logger = log;
	}

	protected void writeExternalizable(Externalizable obj, Class<?> clazz) throws IOException {
		trace("writeExternalizable " + clazz.getName()); //$NON-NLS-1$
		out.writeObject(obj);
	}

	@SuppressWarnings("deprecation")
	protected void trace(String message) {
		LogService ls = this.logger;
		if (ls != null) {
			ls.log(LogService.LOG_DEBUG, message);
		}
	}

	protected void writeFields(Object obj, Class<?> clazz) throws IOException {
		while (clazz != Object.class) {
			try {
				final Field[] allFields = clazz.getDeclaredFields();
				final int allFieldCount = allFields.length;
				int actualFieldCount = 0;
				for (int i = 0; i < allFieldCount; i++) {
					final int mod = allFields[i].getModifiers();
					if (!(Modifier.isStatic(mod) || Modifier.isTransient(mod)))
						actualFieldCount++;
				}
				// write field count
				out.writeInt(actualFieldCount);
				for (int i = 0; i < allFieldCount; i++) {
					final int mod = allFields[i].getModifiers();
					if (Modifier.isStatic(mod) || Modifier.isTransient(mod))
						continue;
					else if (!Modifier.isPublic(mod))
						allFields[i].setAccessible(true);
					Object val = allFields[i].get(obj);
					// Check to see it's not a circular ref
					if (val != obj) {
						// write field name
						out.writeUTF(allFields[i].getName());
						// field value
						writeObjectOverride(val);
					}
				}
			} catch (final Exception e) {
				throw new NotSerializableException("Exception while serializing " + obj.toString() //$NON-NLS-1$
						+ ":\n" + e.getMessage()); //$NON-NLS-1$ 
			}
			clazz = clazz.getSuperclass();
		}
		// Write out a terminator so reader can detect end of object
		out.writeInt(-1);
	}

	protected void writeNonSerializable(Object obj, Class<?> clazz) throws IOException {
		// lookup object stream class
		trace("writeNonSerializable " + clazz.getName()); //$NON-NLS-1$
		// write class name
		out.writeObject(clazz.getName());
		writeFields(obj, clazz);
	}

	protected void writeSerializable(Object obj, Class<?> clazz) throws IOException {
		out.writeObject(obj);
	}

	@Override
	protected void writeObjectOverride(Object obj) throws IOException {
		if (obj == null) {
			out.writeByte(C_NULL);
			return;
		}
		Class<?> clazz = obj.getClass();
		if (clazz.isArray()) {
			trace("writing array"); //$NON-NLS-1$
			out.writeByte(C_ARRAY);
			int len = Array.getLength(obj);
			// write length
			out.writeInt(len);
			// write component type
			out.writeUTF(clazz.getComponentType().getName());
			// write out each array entry
			for (int i = 0; i < len; i++)
				writeObjectOverride(Array.get(obj, i));
			return;
		} else if (obj instanceof Long) {
			trace("writing Long"); //$NON-NLS-1$
			if (clazz.isPrimitive()) {
				trace("writing long"); //$NON-NLS-1$
				out.writeByte(C_LONG);
			} else {
				trace("writing Long"); //$NON-NLS-1$
				out.writeByte(C_OLONG);
			}
			out.writeLong((Long) obj);
			return;
		} else if (obj instanceof Integer) {
			if (clazz.isPrimitive()) {
				trace("writing int"); //$NON-NLS-1$
				out.writeByte(C_INT);
			} else {
				trace("writing Integer"); //$NON-NLS-1$
				out.writeByte(C_OINT);
			}
			out.writeInt((Integer) obj);
			return;
		} else if (obj instanceof Short) {
			if (clazz.isPrimitive()) {
				trace("writing short"); //$NON-NLS-1$
				out.writeByte(C_SHORT);
			} else {
				trace("writing Short"); //$NON-NLS-1$
				out.writeByte(C_OSHORT);
			}
			out.writeShort((Short) obj);
			return;
		} else if (obj instanceof Boolean) {
			if (clazz.isPrimitive()) {
				trace("writing bool"); //$NON-NLS-1$
				out.writeByte(C_BOOL);
			} else {
				trace("writing Boolean"); //$NON-NLS-1$
				out.writeByte(C_OBOOL);
			}
			out.writeBoolean((Boolean) obj);
			return;
		} else if (obj instanceof Byte) {
			if (clazz.isPrimitive()) {
				trace("writing byte"); //$NON-NLS-1$
				out.writeByte(C_BYTE);
			} else {
				trace("writing Byte"); //$NON-NLS-1$
				out.writeByte(C_OBYTE);
			}
			out.writeByte((Byte) obj);
			return;
		} else if (obj instanceof Character) {
			if (clazz.isPrimitive()) {
				trace("writing char"); //$NON-NLS-1$
				out.writeByte(C_CHAR);
			} else {
				trace("writing Character"); //$NON-NLS-1$
				out.writeByte(C_OCHAR);
			}
			out.writeChar((Character) obj);
			return;
		} else if (obj instanceof Float) {
			if (clazz.isPrimitive()) {
				trace("writing float"); //$NON-NLS-1$
				out.writeByte(C_FLOAT);
			} else {
				trace("writing Float"); //$NON-NLS-1$
				out.writeByte(C_OFLOAT);
			}
			out.writeFloat((Float) obj);
			return;
		} else if (obj instanceof Double) {
			if (clazz.isPrimitive()) {
				trace("writing double"); //$NON-NLS-1$
				out.writeByte(C_DOUBLE);
			} else {
				trace("writing Double"); //$NON-NLS-1$
				out.writeByte(C_ODOUBLE);
			}
			out.writeDouble((Double) obj);
			return;
		} else if (obj instanceof String) {
			trace("writing String"); //$NON-NLS-1$
			out.writeByte(C_STRING);
			out.writeUTF((String) obj);
			return;
		} else if (obj instanceof Dictionary) {
			trace("writing dictionary"); //$NON-NLS-1$
			out.writeByte(C_DICT);
			out.writeUTF(clazz.getName());
			Dictionary dict = (Dictionary) obj;
			// write size
			int ds = dict.size();
			trace("writing Dictionary=" + ds); //$NON-NLS-1$
			out.writeInt(ds);
			// for each element in Map
			for (Enumeration e = dict.keys(); e.hasMoreElements();) {
				Object key = e.nextElement();
				writeObjectOverride(key);
				writeObjectOverride(dict.get(key));
			}
			return;
		} else if (obj instanceof Map) {
			out.writeByte(C_MAP);
			Map map = (Map) obj;
			// write size
			int size = map.size();
			trace("writing Map=" + size); //$NON-NLS-1$
			out.writeInt(size);
			// for each element in Map
			for (Object key : map.keySet()) {
				// Write key
				writeObjectOverride(key);
				// Write value
				writeObjectOverride(map.get(key));
			}
			return;
		} else if (obj instanceof List) {
			out.writeByte(C_LIST);
			List list = (List) obj;
			// write size
			int size = list.size();
			trace("writing List=" + size); //$NON-NLS-1$
			out.writeInt(size);
			// write each element
			for (Object item : list)
				writeObjectOverride(item);
			return;
		} else if (obj instanceof Set) {
			out.writeByte(C_SET);
			Set set = (Set) obj;
			// write size
			int size = set.size();
			trace("writing Set=" + size); //$NON-NLS-1$
			out.writeInt(size);
			// then elements
			for (Object item : set)
				writeObjectOverride(item);
			return;
		} else if (obj instanceof Collection) {
			out.writeByte(C_COLL);
			Collection col = (Collection) obj;
			// write size
			int size = col.size();
			trace("writing col=" + size); //$NON-NLS-1$
			out.writeInt(size);
			// then elements
			for (Object item : col)
				writeObjectOverride(item);
			return;
		} else if (obj instanceof Iterable) {
			out.writeByte(C_ITER);
			Iterable itr = (Iterable) obj;
			int size = 0;
			// Get size
			for (@SuppressWarnings("unused")
			Object v : itr)
				size++;
			// write size
			trace("writing Iterable=" + size); //$NON-NLS-1$
			out.writeInt(size);
			// write elements
			for (Object item : itr)
				writeObjectOverride(item);
			return;
		} else if (obj instanceof Enum) {
			out.writeByte(C_ENUM);
			out.writeUTF(obj.getClass().getName());
			out.writeUTF(((Enum) obj).name());
			return;
		}
		if (obj instanceof Externalizable) {
			out.writeByte(C_EXTER);
			writeExternalizable((Externalizable) obj, clazz);
		} else if (obj instanceof Serializable) {
			out.writeByte(C_SER);
			writeSerializable(obj, clazz);
			return;
		} else if (obj instanceof Version) {
			trace("writing Version"); //$NON-NLS-1$
			out.writeByte(C_VER);
			out.writeUTF(((Version) obj).toString());
			return;
		} else if (obj instanceof DTO) {
			out.writeByte(C_DTO);
			writeDTO(obj, clazz);
			return;
		} else {
			if (allowNonSerializable) {
				out.writeByte(C_OBJECT);
				writeNonSerializable(obj, clazz);
				return;
			}
			throw new NotSerializableException("Cannot serialize instance of class=" + clazz.getName()); //$NON-NLS-1$
		}
	}

	private void writeDTO(Object obj, Class<?> clazz) throws IOException {
		trace("writing DTO"); //$NON-NLS-1$
		// Write out class name
		out.writeUTF(clazz.getName());
		for (Field f : clazz.getFields()) {
			final int mod = f.getModifiers();
			if (Modifier.isStatic(mod) || Modifier.isTransient(mod))
				continue;
			try {
				writeObjectOverride(f.get(obj));
			} catch (Exception e) {
				// Should not happen for DTO
				throw new NotSerializableException(clazz.getName());
			}
		}
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#write(int)
	 */
	public final void write(final int val) throws IOException {
		out.write(val);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#write(byte[])
	 */
	public final void write(final byte[] buf) throws IOException {
		out.write(buf);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#write(byte[], int, int)
	 */
	public final void write(final byte[] buf, final int off, final int len) throws IOException {
		out.write(buf, off, len);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#flush()
	 */
	public final void flush() throws IOException {
		out.flush();
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#reset()
	 */
	public final void reset() throws IOException {
		out.reset();
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#close()
	 */
	public final void close() throws IOException {
		out.close();
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeBoolean(boolean)
	 */
	public final void writeBoolean(final boolean val) throws IOException {
		out.writeBoolean(val);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeByte(int)
	 */
	public final void writeByte(final int val) throws IOException {
		out.writeByte(val);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeShort(int)
	 */
	public final void writeShort(final int val) throws IOException {
		out.writeShort(val);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeChar(int)
	 */
	public final void writeChar(final int val) throws IOException {
		out.writeChar(val);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeInt(int)
	 */
	public final void writeInt(final int val) throws IOException {
		out.writeInt(val);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeLong(long)
	 */
	public final void writeLong(final long val) throws IOException {
		out.writeLong(val);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeFloat(float)
	 */
	public final void writeFloat(final float val) throws IOException {
		out.writeFloat(val);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeDouble(double)
	 */
	public final void writeDouble(final double val) throws IOException {
		out.writeDouble(val);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeBytes(java.lang.String)
	 */
	public final void writeBytes(final String str) throws IOException {
		out.writeBytes(str);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeChars(java.lang.String)
	 */
	public final void writeChars(final String str) throws IOException {
		out.writeChars(str);
	}

	/**
	 * 
	 * @see java.io.ObjectOutputStream#writeUTF(java.lang.String)
	 */
	public final void writeUTF(final String str) throws IOException {
		out.writeUTF(str);
	}

}
