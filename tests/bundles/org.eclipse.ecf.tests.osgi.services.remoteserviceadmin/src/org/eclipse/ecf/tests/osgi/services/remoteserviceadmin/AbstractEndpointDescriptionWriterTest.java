package org.eclipse.ecf.tests.osgi.services.remoteserviceadmin;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionWriter;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionAdvertiser;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

public abstract class AbstractEndpointDescriptionWriterTest extends
		AbstractDistributionTest {

	protected static final int REGISTER_WAIT = 2000;
	private ServiceRegistration registration;

	private ServiceRegistration writerEndpointDescriptionAdvertiser;
	private EndpointDescriptionWriter writer;
	
	protected void setUp() throws Exception {
		super.setUp();
		writer = new EndpointDescriptionWriter();
		writerEndpointDescriptionAdvertiser = getContext().registerService(IEndpointDescriptionAdvertiser.class.getName(), createStandardOutputWriterServiceInfoFactory(), null);
	}

	private IEndpointDescriptionAdvertiser createStandardOutputWriterServiceInfoFactory() {
		return new IEndpointDescriptionAdvertiser() {

			@Override
			public IStatus advertise(EndpointDescription endpointDescription) {
				// TODO Auto-generated method stub
				try {
					StringWriter sr = new StringWriter();
					sr.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
					writer.writeEndpointDescriptions(sr, new EndpointDescription[] { (EndpointDescription) endpointDescription });
					System.out.print(sr.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return Status.OK_STATUS;
			}

			@Override
			public IStatus unadvertise(EndpointDescription endpointDescription) {
				// TODO Auto-generated method stub
				return Status.OK_STATUS;
			}
		};
	}

	protected void tearDown() throws Exception {
		if (registration != null) {
			registration.unregister();
			registration = null;
		}
		if (writerEndpointDescriptionAdvertiser != null) {
			writerEndpointDescriptionAdvertiser.unregister();
			writerEndpointDescriptionAdvertiser = null;
		}
		super.tearDown();
	}

	public void testRegisterOnCreatedServer() throws Exception {
		Properties props = getServiceProperties();
		// Actually register with default service (IConcatService)
		registration = registerDefaultService(props);
		// Wait a while
		Thread.sleep(REGISTER_WAIT);
	}

	protected abstract String getServerContainerTypeName();
		
	private Properties getServiceProperties() {
		Properties props = new Properties();
		// Set config to the server container name/provider config name (e.g.
		// ecf.generic.server)
		props.put(RemoteConstants.SERVICE_EXPORTED_CONFIGS,
				getServerContainerTypeName());
		// Set the service exported interfaces to all
		props.put(RemoteConstants.SERVICE_EXPORTED_INTERFACES, "*");
		return props;
	}

}
