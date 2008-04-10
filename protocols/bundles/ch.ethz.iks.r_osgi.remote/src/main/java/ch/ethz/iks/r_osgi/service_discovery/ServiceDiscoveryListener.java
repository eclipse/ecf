package ch.ethz.iks.r_osgi.service_discovery;

import ch.ethz.iks.r_osgi.URI;

public interface ServiceDiscoveryListener {

	public static final String SERVICE_INTERFACES_PROPERTY = "service.interfaces";

	public static final String FILTER_PROPERTY = "filter";

	/**
	 * if this property is set (to anything), the service is automatically
	 * fetched before the listener is called.
	 * 
	 * @since 0.5
	 */
	public static final String AUTO_FETCH_PROPERTY = "listener.auto_fetch";

	void announceService(final String serviceInterface, final URI uri);

	void discardService(final String serviceInterface, final URI uri);

}
