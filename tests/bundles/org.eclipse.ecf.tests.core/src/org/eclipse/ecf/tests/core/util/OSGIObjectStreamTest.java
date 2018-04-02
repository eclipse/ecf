package org.eclipse.ecf.tests.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.util.OSGIObjectInputStream;
import org.eclipse.ecf.core.util.OSGIObjectOutputStream;
import org.eclipse.ecf.internal.tests.core.Activator;
import org.osgi.dto.DTO;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import junit.framework.TestCase;

public class OSGIObjectStreamTest extends TestCase {

	public static class MyDTO extends DTO {
		public double d1;
		public String s;
		public MyDTO dto;
	}
	
	public static class MySerializable implements Serializable {
		private static final long serialVersionUID = 7671873163370195115L;
		private String first;
		private long second;
		private byte[] bytes;
		
		public MySerializable(String f, long s, byte[] b) {
			this.first = f;
			this.second = s;
			this.bytes = b;
		}
		
		public String getFirst() {
			return first;
		}
		
		public long getSecond() {
			return second;
		}
		
		public byte[] getBytes() {
			return bytes;
		}
	}

	MyDTO dto1;
	MyDTO dto2;
	Version v1;
	Version v2;
	String s;
	MySerializable ser;
	Bundle b;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MyDTO dto1 = new MyDTO();
		dto1.d1 = 1.0;
		dto1.s = "test";
		dto1.dto = null;

		MyDTO dto2 = new MyDTO();
		dto2.d1 = 2.0;
		dto2.s = "test2";
		dto2.dto = new MyDTO();
		dto2.dto.s = "test3";

		v1 = Version.valueOf("1.0.1.myfirstversion");
		v2 = Version.valueOf("2.0.2.mysecondversion");
		
		s = "my string is as it is";
		
		ser = new MySerializable("first string", 100, new byte[] { 1, 2, 3 });
		
		b = Activator.getContext().getBundle();
	}
	
	public void testOSGIObjectOutputStream() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OSGIObjectOutputStream oos = new OSGIObjectOutputStream(bos);
		oos.writeObject(ser);
		oos.close();

		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		OSGIObjectInputStream ois = new OSGIObjectInputStream(Activator.getContext().getBundle(),bis);
		Object result = ois.readObject();
		ois.close();

	}

	public void testObjectOutputStream() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(ser);
		oos.close();

		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object result = ois.readObject();
		ois.close();

	}
	

	public void testDTOMap() throws Exception {
		Map<String, DTO> dtos = new HashMap<String, DTO>();
		dtos.put("one", dto1);
		dtos.put("two", dto2);
		assertTrue(dtos.equals(serializeDeserialize(dtos)));
	}
	
	public void testVersionArray() throws Exception {
		Version[] versions = new Version[2];
		versions[0] = v1;
		versions[1] = v2;
		assertTrue(Arrays.equals(versions, (Version[]) serializeDeserialize(versions)));
	}
	
	public void testString() throws Exception {
		assertEquals(s, (String) serializeDeserialize(s));
	}
	
	private Object serializeDeserialize(Object o) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OSGIObjectOutputStream oos = new OSGIObjectOutputStream(bos);
		oos.writeObject(o);
		oos.close();

		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		OSGIObjectInputStream ois = new OSGIObjectInputStream(b,bis);
		Object result = ois.readObject();
		ois.close();
		return result;
	}
	
	public void testSerializable() throws Exception {
		MySerializable r = (MySerializable) serializeDeserialize(ser);
		assertEquals(r.getFirst(),ser.getFirst());
		assertEquals(r.getSecond(),ser.getSecond());
		assertTrue(Arrays.equals(r.getBytes(),ser.getBytes()));
	}


}
