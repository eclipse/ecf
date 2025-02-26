package org.eclipse.ecf.examples.sslcontextfactory;

import org.eclipse.ecf.core.security.SSLContextFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component(immediate=true)
public class ContextFactoryConsumer {
	
	private static SSLContextFactory factory;
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	void bindSSLContextFactory(SSLContextFactory sslContextFactory) {
		factory = sslContextFactory;
	}
	
	void unbindSSLContextFactory(SSLContextFactory sslContextFactory) {
		factory = null;
	}
	
	public static SSLContextFactory getSSLContextFactory() {
		return factory;
	}
}
