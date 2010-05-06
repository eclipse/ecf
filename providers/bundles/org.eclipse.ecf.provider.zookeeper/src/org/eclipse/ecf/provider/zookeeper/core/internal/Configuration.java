package org.eclipse.ecf.provider.zookeeper.core.internal;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.provider.zookeeper.core.DefaultDiscoveryConfig;
import org.eclipse.ecf.provider.zookeeper.core.IDiscoveryConfig;
import org.eclipse.ecf.provider.zookeeper.core.ZooDiscoveryContainer.FLAVOR;
import org.eclipse.ecf.provider.zookeeper.util.Geo;
import org.eclipse.ecf.provider.zookeeper.util.Logger;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

public class Configuration extends DefaultDiscoveryConfig {

	private File zooConfFile;
	private File zookeeperData;
	private ServiceReference reference;
	private List<String> serverIps = new ArrayList<String>();
	private FLAVOR flavor;
	private static final String TEMP = System.getProperties().getProperty(
			"java.io.tmpdir"); //$NON-NLS-1$

	public Configuration(ServiceReference reference) {
		Assert.isNotNull(reference);
		Set<String> legalKeys = getConfigProperties().keySet();
		for (String key : reference.getPropertyKeys()) {
			if (legalKeys.contains(key) || key.startsWith("zoodiscovery"))
				getConfigProperties().put(key, reference.getProperty(key));
		}
	}

	public Configuration(ID targetId) {
		this(targetId.getName());
	}

	public Configuration(String propsAsString) {
		Assert.isNotNull(propsAsString);
		String ss[] = propsAsString.split(";");//$NON-NLS-1$
		for (String s : ss) {
			String key_value[] = s.split("=");//$NON-NLS-1$
			getConfigProperties().put(key_value[0], key_value[1]);
		}
	}

	public Configuration configure() {
		PrintWriter writer = null;
		boolean isNewZookeeperData = false;
		try {
			setZookeeperData(new File(TEMP + File.separator
					+ DefaultDiscoveryConfig.DATADIR_DEFAULT));
			isNewZookeeperData = getZookeeperData().mkdir();
			getZookeeperData().deleteOnExit();
			if (!isNewZookeeperData) {
				clean();		
			}
			this.zooConfFile = new File(getZookeeperData() + "/zoo.cfg");//$NON-NLS-1$
			this.zooConfFile.createNewFile();
			this.zooConfFile.deleteOnExit();
			if (getConfigProperties().containsKey(
					ZOODISCOVERY_FLAVOR_CENTRALIZED)) {
				this.setFlavor(FLAVOR.CENTRALIZED);
				this.serverIps = parseIps();
				if (this.serverIps.size() != 1) {
					String msg = "Industrial Discovery property "
							+ ZOODISCOVERY_FLAVOR_CENTRALIZED
							+ " must contain exactly one ip address";
					Logger.log(LogService.LOG_ERROR, msg, null);
					throw new ServiceException(msg);

				}

			} else if (getConfigProperties().containsKey(
					ZOODISCOVERY_FLAVOR_REPLICATED)) {
				this.setFlavor(FLAVOR.REPLICATED);
				this.serverIps = parseIps();
				this.serverIps.remove("localhost"); //$NON-NLS-1$	
				if (!this.serverIps.contains(Geo.getHost())) {
					this.serverIps.add(Geo.getHost());
				}
				if (this.serverIps.size() < 2) {
					String msg = "Industrial Discovery property "//$NON-NLS-1$
							+ IDiscoveryConfig.ZOODISCOVERY_FLAVOR_REPLICATED
							+ " must contain at least one IP address which is not localhost.";
					Logger.log(LogService.LOG_ERROR, msg, null);
					throw new ServiceException(msg);

				}

			} else if (getConfigProperties().containsKey(
					ZOODISCOVERY_FLAVOR_STANDALONE)) {
				this.setFlavor(FLAVOR.STANDALONE);
				this.serverIps = parseIps();
				this.serverIps.remove("localhost"); //$NON-NLS-1$						

			}

			getConfigProperties().put(ZOOKEEPER_DATADIR,
			/*
			 * zooKeeper seems not understanding Windows file backslash !!
			 */
			getZookeeperData().getAbsolutePath().replace("\\", "/"));//$NON-NLS-1$ //$NON-NLS-2$
			getConfigProperties().put(ZOOKEEPER_DATALOGDIR,
					getZookeeperData().getAbsolutePath().replace("\\", "/"));//$NON-NLS-1$ //$NON-NLS-2$
			Collections.sort(this.serverIps);
			if (this.isQuorum()) {
				String myip = Geo.getHost();
				int myId = this.serverIps.indexOf(myip);
				File myIdFile = new File(getZookeeperData() + "/myid");//$NON-NLS-1$
				myIdFile.createNewFile();
				myIdFile.deleteOnExit();
				writer = new PrintWriter(myIdFile);
				writer.print(myId);
				writer.flush();
				writer.close();
			}
			writer = new PrintWriter(this.zooConfFile);
			if (this.isQuorum()) {
				for (int i = 0; i < this.serverIps.size(); i++) {
					writer.println("server."//$NON-NLS-1$
							+ i
							+ "="//$NON-NLS-1$
							+ this.serverIps.get(i)
							+ ":"//$NON-NLS-1$
							+ getConfigProperties().get(ZOOKEEPER_SERVER_PORT)
							+ ":"//$NON-NLS-1$
							+ getConfigProperties()
									.get(ZOOKEEPER_ELECTION_PORT));

				}
			}
			for (String k : getConfigProperties().keySet()) {
				if (k.startsWith("zoodiscovery")) {//$NON-NLS-1$
					/*
					 * Ignore properties that are not intended for ZooKeeper
					 * internal configuration
					 */
					continue;
				}
				writer.println(k + "=" + getConfigProperties().get(k));//$NON-NLS-1$
			}
			writer.flush();
			writer.close();

		} catch (IOException e) {
			Logger.log(LogService.LOG_ERROR, e.getMessage(), e);
		} finally {
			if (writer != null)
				writer.close();
		}
		return this;
	}

	public String getConfFile() {
		return this.zooConfFile.toString();
	}

	public String getServerIps() {
		String ipsString = ""; //$NON-NLS-1$
		for (String i : this.serverIps) {
			ipsString += i
					+ ":" + DefaultDiscoveryConfig.CLIENT_PORT_DEFAULT + ",";//$NON-NLS-1$ //$NON-NLS-2$
		}
		return ipsString.substring(0, ipsString.lastIndexOf(","));//$NON-NLS-1$
	}

	public List<String> getServerIpsAsList() {
		return this.serverIps;
	}

	public void setZookeeperData(File zookeeperData) {
		this.zookeeperData = zookeeperData;
	}

	public File getZookeeperData() {
		return this.zookeeperData;
	}

	public void setFlavor(FLAVOR flavor) {
		this.flavor = flavor;
	}

	public FLAVOR getFlavor() {
		return this.flavor;
	}

	public boolean isQuorum() {
		return this.flavor == FLAVOR.REPLICATED;
	}

	public boolean isCentralized() {
		return this.flavor == FLAVOR.CENTRALIZED;
	}

	public boolean isStandAlone() {
		return this.flavor == FLAVOR.STANDALONE;
	}

	public ServiceReference getReference() {
		return this.reference;
	}

	private void clean() {
		for (File file : this.zookeeperData.listFiles()) {
			try {
				if (file.isDirectory()) {
					for (File f : file.listFiles())
						f.delete();
				}
				file.delete();
			} catch (Throwable t) {
				continue;
			}
		}
	}

	private List parseIps() {
		List l = Arrays.asList(((String) getConfigProperties().get(
				flavor.toString())).split(","));//$NON-NLS-1$
		Collections.sort(l);
		return l;
	}

	public String toString() {
		String s = flavor.name();
		for (Object o : parseIps())
			s += o;
		return s;
	}
}
