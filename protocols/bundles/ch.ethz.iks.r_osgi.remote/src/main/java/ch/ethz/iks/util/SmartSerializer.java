/* Copyright (c) 2006-2008 Jan S. Rellermeyer
 * Information and Communication Systems Research Group (IKS),
 * Department of Computer Science, ETH Zurich.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of ETH Zurich nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ch.ethz.iks.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import ch.ethz.iks.r_osgi.types.BoxedPrimitive;

/**
 * <p>
 * SmartSerializer can serialize objects in a smarter way than ordinary java
 * serialization does it. String serializable objects will be handled separatly,
 * in arrays, every element is checked if it is string serializable.
 * </p>
 * <p>
 * String-serializable means, that the object has a constructor that creates a
 * equal object from the output of an object's <code>toString</code> method.
 * This is a more optimized way of transporting values of complex objects
 * through the network.
 * </p>
 * 
 * @author Jan S. Rellermeyer, ETH Zurich
 * @since 0.1
 */
public final class SmartSerializer {

	/**
	 * hidden default constructor.
	 */
	private SmartSerializer() {

	}

	/**
	 * the positive list contains class names of classes that are
	 * string-serializable.
	 */
	private static List positiveList = new ArrayList(Arrays.asList(new Object[] {"java.lang.Integer", "java.lang.Boolean", "java.lang.Long", "java.lang.Short", "java.lang.Byte"})); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 

	private static final HashMap idToClass = new HashMap();

	private static final HashMap classToId = new HashMap();
	static {
		idToClass.put("I", Integer.class); //$NON-NLS-1$
		classToId.put(Integer.class.getName(), "I"); //$NON-NLS-1$
		idToClass.put("Z", Boolean.class); //$NON-NLS-1$
		classToId.put(Boolean.class.getName(), "Z"); //$NON-NLS-1$
		idToClass.put("J", Long.class); //$NON-NLS-1$
		classToId.put(Long.class.getName(), "J"); //$NON-NLS-1$
		idToClass.put("S", Short.class); //$NON-NLS-1$
		classToId.put(Short.class.getName(), "S"); //$NON-NLS-1$
		idToClass.put("B", Byte.class); //$NON-NLS-1$
		classToId.put(Byte.class.getName(), "B"); //$NON-NLS-1$
		idToClass.put("C", Character.class); //$NON-NLS-1$
		classToId.put(Character.class.getName(), "C"); //$NON-NLS-1$
		idToClass.put("D", Double.class); //$NON-NLS-1$
		classToId.put(Double.class.getName(), "D"); //$NON-NLS-1$
		idToClass.put("F", Float.class); //$NON-NLS-1$
		classToId.put(Float.class.getName(), "F"); //$NON-NLS-1$
	}

	/**
	 * serialize Hashtables. Useful to serialize properties of services etc.
	 * 
	 * @param htable
	 *            the <code>Hashtable</code>.
	 * @param out
	 *            the output stream.
	 * @throws IOException
	 *             in case of errors.
	 */
	private static void serialize(final Hashtable htable, final ObjectOutputStream out) throws IOException {
		if (htable == null) {
			out.writeUTF(""); //$NON-NLS-1$
			return;
		}
		out.writeUTF("java.util.Hashtable"); //$NON-NLS-1$
		out.write(htable.size());
		for (final Enumeration keys = htable.keys(); keys.hasMoreElements();) {
			final Object key = keys.nextElement();
			final Object value = htable.get(key);
			serialize(key, out);
			serialize(value, out);
		}
	}

	/**
	 * serialize an array of objects. Checks every element for
	 * string-serializability.
	 * 
	 * @param obj
	 *            the object array.
	 * @param out
	 *            the output stream.
	 * @throws IOException
	 *             in case of errors.
	 */
	private static void serialize(final Object[] obj, final ObjectOutputStream out) throws IOException {
		if (obj == null) {
			out.writeUTF(""); //$NON-NLS-1$
			return;
		}

		out.writeUTF("A"); //$NON-NLS-1$
		out.writeUTF(obj.getClass().getComponentType().getName());
		out.write(obj.length);
		for (int i = 0; i < obj.length; i++) {
			serialize(obj[i], out);
		}
	}

	/**
	 * serialize an object.
	 * 
	 * @param object
	 *            the object.
	 * @param out
	 *            the output stream.
	 * @throws IOException
	 *             in case of errors.
	 */
	public static void serialize(final Object object, final ObjectOutputStream out) throws IOException {
		final Object obj = object instanceof BoxedPrimitive ? ((BoxedPrimitive) object).getBoxed() : object;

		if (obj == null) {
			out.writeUTF(""); //$NON-NLS-1$
			return;
		} else if (obj instanceof Hashtable) {
			serialize((Hashtable) obj, out);
			return;
		} else if (obj instanceof String) {
			out.writeUTF("S"); //$NON-NLS-1$
			out.writeUTF((String) obj);
			return;
		} else if (obj.getClass().isArray() && !obj.getClass().getComponentType().isPrimitive()) {
			serialize((Object[]) obj, out);
			return;
		}

		final String clazzName = obj.getClass().getName();
		if (positiveList.contains(clazzName)) {
			final String id = (String) classToId.get(clazzName);
			out.writeUTF(id != null ? id : clazzName);
			out.writeUTF(obj.toString());
			return;
		} else {
			// fallback: Java Serialization
			if (!(obj instanceof Serializable)) {
				throw new RuntimeException(obj.getClass().getName() + " is not serializable"); //$NON-NLS-1$
			}
			out.writeUTF("R"); //$NON-NLS-1$
			out.writeObject(obj);
		}
	}

	/**
	 * deserialize from an input stream.
	 * 
	 * @param in
	 *            the input stream.
	 * @return the deserialized object.
	 * @throws IOException
	 *             if the deserialization fails.
	 */
	public static Object deserialize(final ObjectInputStream in) throws IOException {
		final String type = in.readUTF().intern();
		if (type == "") { //$NON-NLS-1$
			return null;
		} else if (type == "A") { //$NON-NLS-1$
			final String componentType = in.readUTF();
			final int length = in.read();

			try {
				final Class arrayClass = Class.forName(componentType);

				final Object[] objects = (Object[]) Array.newInstance(arrayClass, length);
				for (int i = 0; i < length; i++) {
					objects[i] = deserialize(in);
				}
				return objects;
			} catch (final Exception e) {
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
		} else if (type == "S") { //$NON-NLS-1$
			// String object
			return in.readUTF();
		} else if (type == "java.util.Hashtable") { //$NON-NLS-1$
			final Hashtable htable = new Hashtable();

			final int size = in.read();

			for (int i = 0; i < size; i++) {
				final Object key = deserialize(in);
				final Object value = deserialize(in);
				htable.put(key, value);
			}
			return htable;
		} else if (type == "R") { //$NON-NLS-1$
			// JAVA SERIALIZED OBJECT
			try {
				return in.readObject();
			} catch (final ClassNotFoundException c) {
				c.printStackTrace();
				return null;
			}
		} else {
			// String serialized object
			try {
				final Class test = (Class) idToClass.get(type);
				final Class clazz = test != null ? test : Class.forName(type);
				final Constructor constr = clazz.getConstructor(new Class[] {String.class});
				return constr.newInstance(new Object[] {in.readUTF()});
			} catch (final Exception e) {
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
		}
	}

}
