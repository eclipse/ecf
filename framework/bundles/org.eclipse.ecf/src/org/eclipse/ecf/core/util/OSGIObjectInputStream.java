/****************************************************************************
 * Copyright (c) 2018, 2020 Composent, Inc. and others.
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
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.osgi.service.log.LogService;

/**
 * @since 3.9
 */
public class OSGIObjectInputStream extends ObjectInputStream implements OSGIObjectStreamConstants {

	protected ObjectInputStream in;
	protected final Bundle b;
	protected LogService logger;

	/**
	 * @since 3.10
	 */
	ClassLoader classLoader;

	class ReplaceableObjectInputStream extends ObjectInputStream {
		public ReplaceableObjectInputStream(InputStream ins) throws IOException {
			super(ins);
			enableResolveObject(true);
		}

		@Override
		protected Object resolveObject(Object obj) throws IOException {
			if (obj instanceof SerVersion) {
				return ((SerVersion) obj).toVersion();
			}
			if (obj instanceof SerDTO) {
				SerDTO serDTO = (SerDTO) obj;
				String className = serDTO.getClassname();
				Class<?> clazz = null;
				try {
					clazz = loadClass(className);
				} catch (Exception e) {
					throw new IOException("Could not load class for instance of SerDTO with className=" + className); //$NON-NLS-1$
				}
				return serDTO.readObject(clazz);
			}
			return super.resolveObject(obj);
		}

		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
			try {
				return loadClass(desc.getName());
			} catch (Exception e) {
				return super.resolveClass(desc);
			}
		}
	}

	public OSGIObjectInputStream(Bundle b, InputStream in, LogService logger) throws IOException {
		super();
		this.b = b;
		this.in = new ReplaceableObjectInputStream(in);
		this.logger = logger;
	}

	public OSGIObjectInputStream(Bundle b, InputStream in) throws IOException {
		this(b, in, null);
	}

	/**
	 * @since 3.10
	 */
	public OSGIObjectInputStream(Bundle b) throws IOException {
		super();
		this.b = b;
	}

	/**
	 * @since 3.10
	 */
	public synchronized void setInputStream(InputStream in) throws IOException {
		if (this.in != null) {
			this.in.close();
		}
		this.in = new ReplaceableObjectInputStream(in);
	}

	/**
	 * @since 3.10
	 */
	public void setClassLoader(ClassLoader cl) {
		this.classLoader = cl;
	}

	public void setLogService(LogService log) {
		this.logger = log;
	}

	@SuppressWarnings("deprecation")
	protected void trace(String message) {
		LogService ls = this.logger;
		if (ls != null) {
			ls.log(LogService.LOG_DEBUG, message);
		}
	}

	protected Class loadClass(String classname) throws ClassNotFoundException {
		ClassLoader cl = this.classLoader;
		Bundle bundle = b;
		if (cl != null) {
			try {
				return Class.forName(classname, false, cl);
			} catch (Exception e) {
				// Try bundle loading
			}
		}
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
				return null;
			case C_SER : // Serializable
				return readSerializedObject();
			case C_VER : // Version
				return Version.parseVersion(in.readUTF());
			case C_ARRAY : // Object[]
				// read array length
				int ol = in.readInt();
				// read component type and create array for that component type
				Class<?> clazz = getClassForType(in.readUTF());
				Object oresult = Array.newInstance(clazz, ol);
				for (int i = 0; i < ol; i++)
					Array.set(oresult, i, readObjectOverride());
				return oresult;
			case C_DICT : // Dictionary
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
				Map mr = new HashMap();
				for (int i = 0; i < ms; i++) {
					Object key = readObjectOverride();
					Object val = readObjectOverride();
					mr.put(key, val);
				}
				return mr;
			case C_LIST : // List
				int lsize = in.readInt();
				List l = new ArrayList(lsize);
				for (int i = 0; i < lsize; i++)
					l.add(readObjectOverride());
				return l;
			case C_SET : // Set
				int ssize = in.readInt();
				Set s = new HashSet(ssize);
				for (int i = 0; i < ssize; i++)
					s.add(readObjectOverride());
				return s;
			case C_COLL : // Collection
				int csize = in.readInt();
				Collection c = new ArrayList(csize);
				for (int i = 0; i < csize; i++)
					c.add(readObjectOverride());
				return c;
			case C_ITER : // Iterable
				int isize = in.readInt();
				List itr = new ArrayList(isize);
				for (int i = 0; i < isize; i++)
					itr.add(readObjectOverride());
				return itr;
			case C_EXTER : // Externalizable
				return in.readObject();
			case C_STRING : // String
				return in.readUTF();
			case C_LONG :
			case C_OLONG :
				return in.readLong();
			case C_INT :
			case C_OINT :
				return in.readInt();
			case C_SHORT :
			case C_OSHORT :
				return in.readShort();
			case C_BOOL :
			case C_OBOOL :
				return in.readBoolean();
			case C_BYTE :
			case C_OBYTE :
				return in.readByte();
			case C_CHAR :
			case C_OCHAR :
				return in.readChar();
			case C_DOUBLE :
			case C_ODOUBLE :
				return in.readDouble();
			case C_FLOAT :
			case C_OFLOAT :
				return in.readFloat();
			case C_ENUM :
				return Enum.valueOf(loadClass(in.readUTF()), in.readUTF());
			case C_OBJECT :
				return readNonSerializedObject();
			default :
				throw new IOException("Cannot deserialize object with type=" + type); //$NON-NLS-1$
		}
	}

	protected Object readExternalizable() throws ClassNotFoundException, IOException {
		return in.readObject();
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
			return clazz.getDeclaredConstructor().newInstance();
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
		return in.readObject();
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
