package org.eclipse.ecf.examples.sslcontextfactory;

import java.security.Provider;

import org.osgi.service.component.annotations.Component;

@Component(service=Provider.class,immediate=true)
public class ExampleProvider extends Provider {

	private static final long serialVersionUID = 4387195962639458953L;

	public ExampleProvider() {
		super("PKIJoe", "1.0", null);
	}

}
