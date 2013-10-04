package org.eclipse.ecf.tests.core;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.IContainerFactory;
import org.eclipse.ecf.tests.ContainerAbstractTestCase;

public class GenericServerCreateTest extends ContainerAbstractTestCase {

	private IContainerFactory containerFactory;
	private IContainer container;
	
	protected void setUp() throws Exception {
		super.setUp();
		containerFactory = ContainerFactory.getDefault();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (container != null) {
			container.disconnect();
			container.dispose();
			removeFromContainerManager(container);
		}
		containerFactory = null;
	}

	public void testServerCreateParam1() throws Exception {
		String id = getServerIdentity();
		container = containerFactory.createContainer(
				getServerContainerName(), new Object[] { id });
		assertNotNull(container);
		assertEquals(id, container.getID().getName());
	}

	public void testServerCreateParam2() throws Exception {
		container = containerFactory
				.createContainer(getServerContainerName());
		assertNotNull(container);
	}

	public void testServerCreateMapParam1() throws Exception {
		String serverId = getServerIdentity();
		Map map = new HashMap();
		map.put("id", serverId);
		container = containerFactory.createContainer(
				getServerContainerName(), map);
		assertNotNull(container);
	}

	public void testServerCreateMapParam2() throws Exception {
		Map map = new HashMap();
		map.put("hostname", "localhost");
		map.put("port", ""+genericServerPort);
		map.put("path", "/foo");
		container = containerFactory.createContainer(
				getServerContainerName(), map);
		assertNotNull(container);
	}

	public void testServerCreateMapParam3() throws Exception {
		String serverId = getServerIdentity();
		Map map = new HashMap();
		map.put("id", serverId);
		map.put("bindAddress", new InetSocketAddress((InetAddress) null,0).getAddress());
		container = containerFactory.createContainer(
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
