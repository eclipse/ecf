/*******************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.core.util;

/**
 * @since 3.9
 */
public interface OSGIObjectStreamConstants {

	public static final byte C_NULL = 0; // null
	public static final byte C_SER = 1; // Serializable
	public static final byte C_VER = 2; // Version
	public static final byte C_ARRAY = 3; // array
	public static final byte C_DTO = 4; // DTO
	public static final byte C_MAP = 5; // Map
	public static final byte C_LIST = 6; // List
	public static final byte C_SET = 7; // Set
	public static final byte C_COLL = 8; // Collection
	public static final byte C_ITER = 9; // Iterable
	public static final byte C_EXTER = 10; // Externalizable
	public static final byte C_STRING = 11; // String
	public static final byte C_ENUM = 12; // Enum
	public static final byte C_OBJECT = 13; // Everything else
	public static final byte C_DICT = 14; // Dictionary

	// primitive types
	public static final byte C_LONG = 20; // primitive long
	public static final byte C_INT = 21; // primitive int
	public static final byte C_SHORT = 22; // prim short
	public static final byte C_BOOL = 23; // prim boolean
	public static final byte C_BYTE = 24; // prim byte
	public static final byte C_CHAR = 25; // primp character
	public static final byte C_DOUBLE = 26; // prim double
	public static final byte C_FLOAT = 27; // prim float

	// simple object types
	public static final byte C_OLONG = 30; // Long
	public static final byte C_OINT = 31; // Integer
	public static final byte C_OSHORT = 32; // Short
	public static final byte C_OBOOL = 33; // Boolean
	public static final byte C_OBYTE = 34; // Byte
	public static final byte C_OCHAR = 35; // Character
	public static final byte C_ODOUBLE = 36; // Double
	public static final byte C_OFLOAT = 37; // Float

}
