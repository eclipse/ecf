/*******************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.util;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.service.log.LogService;

/**
 * @since 3.9
 */
public class OSGIObjectInputStream extends ObjectInputStream implements OSGIObjectStreamConstants {

	protected final ObjectInputStream in;
	protected final Bundle b;
	protected LogService logger;

	public OSGIObjectInputStream(Bundle b, InputStream in, LogService logger) throws IOException {
		super();
		this.b = b;
		this.in = new ObjectInputStream(in);
		this.logger = logger;
	}

	public OSGIObjectInputStream(Bundle b, InputStream in) throws IOException {
		this(b, in, null);
	}

	public void setLogService(LogService log) {
		this.logger = log;
	}

	protected void trace(String message) {
		LogService ls = this.logger;
		if (ls != null) {
			ls.log(LogService.LOG_DEBUG, message);
		}
	}

	protected Class loadClass(String classname) throws ClassNotFoundException {
		Bundle bundle = b;
		return (bundle == null) ? Class.forName(classname) : bundle.loadClass(classname);
	}

	protected Class<?> getClassForType(String type) throws ClassNotFoundException {
		if (type.equals(byte.class.getName()))
			return byte.class;
		else if (type.equals(long.class.getName()))
			return long.class;
		else if (type.equals(int.class.getName()))
			return int.class;
		else if (type.equals(short.class.getName()))
			return short.class;
		else if (type.equals(char.class.getName()))
			return char.class;
		else if (type.equals(boolean.class.getName()))
			return boolean.class;
		else if (type.equals(float.class.getName()))
			return float.class;
		else if (type.equals(double.class.getName()))
			return double.class;
		else
			return loadClass(type);
	}

	protected final Object readObjectOverride() throws IOException, ClassNotFoundException {
		final byte type = in.readByte();
		switch (type) {
			case C_NULL : // null
				trace("null"); //$NON-NLS-1$
				return null;
			case C_SER : // Serializable
				trace("readSerializedObject"); //$NON-NLS-1$
				return readSerializedObject();
			case C_VER : // Version
				trace("readVersion"); //$NON-NLS-1$
				return Version.parseVersion(in.readUTF());
			case C_ARRAY : // Object[]
				// read array length
				trace("readArray"); //$NON-NLS-1$
				int ol = in.readInt();
				// read component type and create array for that component type
				Class<?> clazz = getClassForType(in.readUTF());
				Object oresult = Array.newInstance(clazz, ol);
				for (int i = 0; i < ol; i++)
					Array.set(oresult, i, readObjectOverride());
				return oresult;
			case C_DTO : // DTO
				trace("readDTO"); //$NON-NLS-1$
				return readDTO();
			case C_DICT : // Dictionary
				trace("readDictionary"); //$NON-NLS-1$
				Class<?> dictClazz = loadClass(in.readUTF());
				Dictionary dict = null;
				Constructor cons;
				try {
					cons = dictClazz.getDeclaredConstructor((Class[]) null);
					cons.setAccessible(true);
					dict = (Dictionary) cons.newInstance((Object[]) null);
				} catch (Exception e) {
					throw new IOException("Could not create dictionary instance of clazz=" + dictClazz.getName()); //$NON-NLS-1$
				}
				int dsize = in.readInt();
				for (int i = 0; i < dsize; i++) {
					Object key = readObjectOverride();
					Object val = readObjectOverride();
					dict.put(key, val);
				}
				return dict;
			case C_MAP : // Map
				// read map length
				int ms = in.readInt();
				trace("readMap=" + ms); //$NON-NLS-1$
				Map mr = new HashMap();
				for (int i = 0; i < ms; i++) {
					Object key = readObjectOverride();
					Object val = readObjectOverride();
					mr.put(key, val);
				}
				return mr;
			case C_LIST : // List
				int lsize = in.readInt();
				trace("readList=" + lsize); //$NON-NLS-1$
				List l = new ArrayList(lsize);
				for (int i = 0; i < lsize; i++)
					l.add(readObjectOverride());
				return l;
			case C_SET : // Set
				int ssize = in.readInt();
				trace("readSet=" + ssize); //$NON-NLS-1$
				Set s = new HashSet(ssize);
				for (int i = 0; i < ssize; i++)
					s.add(readObjectOverride());
				return s;
			case C_COLL : // Collection
				int csize = in.readInt();
				trace("readCol=" + csize); //$NON-NLS-1$
				Collection c = new ArrayList(csize);
				for (int i = 0; i < csize; i++)
					c.add(readObjectOverride());
				return c;
			case C_ITER : // Iterable
				int isize = in.readInt();
				trace("readIter=" + isize); //$NON-NLS-1$
				List itr = new ArrayList(isize);
				for (int i = 0; i < isize; i++)
					itr.add(readObjectOverride());
				return itr;
			case C_EXTER : // Externalizable
				return readExternalizable();
			case C_STRING : // String
				trace("readString"); //$NON-NLS-1$
				return in.readUTF();
			case C_LONG :
			case C_OLONG :
				trace("readLong"); //$NON-NLS-1$
				return in.readLong();
			case C_INT :
			case C_OINT :
				trace("readInt"); //$NON-NLS-1$
				return in.readInt();
			case C_SHORT :
			case C_OSHORT :
				trace("readShort"); //$NON-NLS-1$
				return in.readShort();
			case C_BOOL :
			case C_OBOOL :
				trace("readBool"); //$NON-NLS-1$
				return in.readBoolean();
			case C_BYTE :
			case C_OBYTE :
				trace("readByte"); //$NON-NLS-1$
				return in.readByte();
			case C_CHAR :
			case C_OCHAR :
				trace("readChar"); //$NON-NLS-1$
				return in.readChar();
			case C_DOUBLE :
			case C_ODOUBLE :
				trace("readDouble"); //$NON-NLS-1$
				return in.readDouble();
			case C_FLOAT :
			case C_OFLOAT :
				trace("readFloat"); //$NON-NLS-1$
				return in.readFloat();
			case C_ENUM :
				trace("readEnum"); //$NON-NLS-1$
				return Enum.valueOf(loadClass(in.readUTF()), in.readUTF());
			case C_OBJECT :
				return readNonSerializedObject();
			default :
				throw new IOException("Cannot deserialize object with type=" + type); //$NON-NLS-1$
		}
	}

	private Object readDTO() throws IOException, ClassNotFoundException {
		Class<?> clazz = loadClass(in.readUTF());
		Object result = null;
		try {
			result = clazz.newInstance();
			for (Field f : clazz.getFields()) {
				final int mod = f.getModifiers();
				// If it's static or transient then ignore
				if (Modifier.isStatic(mod) || Modifier.isTransient(mod))
					continue;
				// Else read and set value of field
				f.set(result, readObjectOverride());
			}
		} catch (Exception e) {
			throw new IOException("Cannot deserialize DTO because of exception: " + e.getMessage()); //$NON-NLS-1$
		}
		return result;
	}

	private Object createInstance(ObjectStreamClass osc) throws IOException {
		try {
			Method m = osc.getClass().getDeclaredMethod("newInstance", (Class<?>[]) null); //$NON-NLS-1$
			m.setAccessible(true);
			return m.invoke(osc, (Object[]) null);
		} catch (Exception e) {
			IOException t = new IOException("Exception creating newInstance of class=" + osc.getName()); //$NON-NLS-1$
			t.setStackTrace(e.getStackTrace());
			throw t;
		}
	}

	protected Object readExternalizable() throws ClassNotFoundException, IOException {
		final String clazzName = in.readUTF();
		trace("readExternalizable " + clazzName); //$NON-NLS-1$
		Class<?> clazz = loadClass(clazzName);
		Object inst = createInstance(clazz);
		Externalizable ex = (Externalizable) inst;
		ex.readExternal(this);
		return inst;
	}

	protected Object readFields(Class<?> clazz, Object inst) throws IOException {
		try {
			int fieldCount = in.readInt();
			while (fieldCount > -1) {
				for (int i = 0; i < fieldCount; i++) {
					final String fieldName = in.readUTF();
					final Field field = clazz.getDeclaredField(fieldName);
					final int mod = field.getModifiers();
					if (!Modifier.isPublic(mod))
						field.setAccessible(true);

					//
					final Object value = readObjectOverride();
					field.set(inst, value);
				}
				clazz = clazz.getSuperclass();
				fieldCount = in.readInt();
			}
			return inst;
		} catch (final Exception e) {
			IOException t = new IOException("Error while deserializing class=" + clazz.getName() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			t.setStackTrace(e.getStackTrace());
			throw t;
		}
	}

	protected Object createInstance(Class<?> clazz) throws IOException {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new IOException("Could create new instance of class=" + clazz.getName() + ".  Class must have public no-arg constructor"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected Object readNonSerializedObject() throws IOException, ClassNotFoundException {
		// read object stream class
		String className = in.readUTF();
		trace("readNonSerializedObject " + className); //$NON-NLS-1$
		Class<?> clazz = loadClass(className);
		// create instance
		Object instance = createInstance(clazz);
		return readFields(clazz, instance);
	}

	protected Object readSerializedObject() throws IOException, ClassNotFoundException {
		// read object stream class
		ObjectStreamClass osc = (ObjectStreamClass) in.readObject();
		trace("readSerializedObject " + osc.getName()); //$NON-NLS-1$
		Class<?> clazz = osc.forClass();
		// create instance
		final Object instance = createInstance(osc);
		return readFields(clazz, instance);
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#read()
	 */
	public final int read() throws IOException {
		return in.read();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#read(byte[], int, int)
	 */
	public final int read(final byte[] buf, final int off, final int len) throws IOException {
		return in.read(buf, off, len);
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#available()
	 */
	public final int available() throws IOException {
		return in.available();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#close()
	 */
	public final void close() throws IOException {
		in.close();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readBoolean()
	 */
	public final boolean readBoolean() throws IOException {
		return in.readBoolean();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readByte()
	 */
	public final byte readByte() throws IOException {
		return in.readByte();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readUnsignedByte()
	 */
	public final int readUnsignedByte() throws IOException {
		return in.readUnsignedByte();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readChar()
	 */
	public final char readChar() throws IOException {
		return in.readChar();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readShort()
	 */
	public final short readShort() throws IOException {
		return in.readShort();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readUnsignedShort()
	 */
	public final int readUnsignedShort() throws IOException {
		return in.readUnsignedShort();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readInt()
	 */
	public final int readInt() throws IOException {
		return in.readInt();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readLong()
	 */
	public final long readLong() throws IOException {
		return in.readLong();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readFloat()
	 */
	public final float readFloat() throws IOException {
		return in.readFloat();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readDouble()
	 */
	public final double readDouble() throws IOException {
		return in.readDouble();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readFully(byte[])
	 */
	public final void readFully(final byte[] buf) throws IOException {
		in.readFully(buf);
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readFully(byte[], int, int)
	 */
	public final void readFully(final byte[] buf, final int off, final int len) throws IOException {
		in.readFully(buf, off, len);
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#skipBytes(int)
	 */
	public final int skipBytes(final int len) throws IOException {
		return in.skipBytes(len);
	}

	/**
	 * @return String
	 * @throws IOException 
	 * @deprecated
	 */
	public final String readLine() throws IOException {
		return in.readLine();
	}

	/**
	 * 
	 * @see java.io.ObjectInputStream#readUTF()
	 */
	public final String readUTF() throws IOException {
		return in.readUTF();
	}

}
