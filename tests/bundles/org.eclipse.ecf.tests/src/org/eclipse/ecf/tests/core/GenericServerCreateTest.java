package org.eclipse.ecf.tests.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

public class GenericServerCreateTest extends ContainerAbstractTestCase {

	private IContainerFactory containerFactory;

	protected void setUp() throws Exception {
		super.setUp();
		containerFactory = ContainerFactory.getDefault();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		containerFactory = null;
	}

	public void testServerCreateParam1() throws Exception {
		String id = getServerIdentity();
		IContainer container = containerFactory.createContainer(
				getServerContainerName(), new Object[] { id });
		assertNotNull(container);
		assertEquals(id, container.getID().getName());
	}

	public void testServerCreateParam2() throws Exception {
		IContainer container = containerFactory
				.createContainer(getServerContainerName());
		assertNotNull(container);
	}

	public void testServerCreateMapParam1() throws Exception {
		String serverId = getServerIdentity();
		Map map = new HashMap();
		map.put("id", serverId);
		IContainer container = containerFactory.createContainer(
				getServerContainerName(), map);
		assertNotNull(container);
	}

	public void testServerCreateMapParam2() throws Exception {
		Map map = new HashMap();
		map.put("hostname", "localhost");
		map.put("port", "2222");
		map.put("path", "/foo");
		IContainer container = containerFactory.createContainer(
				getServerContainerName(), map);
		assertNotNull(container);
	}

	public void testServerCreateMapParam1Fail() throws Exception {
		Map map = new HashMap();
		// bogus port
		map.put("port", new Object());
		try {
			containerFactory.createContainer(getServerContainerName(), map);
			fail("create with map=" + map + " succeeded");
		} catch (ContainerCreateException e) {
			// succeed
		}
	}
}
