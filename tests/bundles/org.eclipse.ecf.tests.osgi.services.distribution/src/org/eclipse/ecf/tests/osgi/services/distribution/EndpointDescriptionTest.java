package org.eclipse.ecf.tests.osgi.services.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.ecf.tests.internal.osgi.services.distribution.Activator;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.remoteserviceadmin.EndpointDescription;
import org.osgi.service.remoteserviceadmin.EndpointListener;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

public class EndpointDescriptionTest extends AbstractDistributionTest {

	private List<EndpointDescription> endpointsReceived = new ArrayList<EndpointDescription>();
	final String endpointListenerFilter = "(&("+org.osgi.service.remoteserviceadmin.RemoteConstants.ENDPOINT_ID+"=*)("+org.eclipse.ecf.remoteservice.Constants.SERVICE_ID+"=0))";

	private EndpointListener endpointListener = new EndpointListener() {

		public void endpointAdded(EndpointDescription paramEndpointDescription,
				String paramString) {
			endpointsReceived.add(paramEndpointDescription);
		}

		public void endpointRemoved(
				EndpointDescription paramEndpointDescription, String paramString) {
			endpointsReceived.remove(paramEndpointDescription);
		}
		
	};
	
	@Override
	protected String getClientContainerName() {
		return null;
	}

	@Override
	protected int getClientCount() {
		return 0;
	}
	
	private ServiceRegistration endpointListenerReg;
	private ServiceRegistration testServiceRegistration;
	private TestService1 testService1;
	@Override
	protected void setUp() throws Exception {
		endpointsReceived.clear();
		Hashtable props = new Hashtable();
		props.put(EndpointListener.ENDPOINT_LISTENER_SCOPE, new String[]{endpointListenerFilter});
		endpointListenerReg = Activator.getDefault().getContext().registerService(EndpointListener.class, endpointListener, props);
	
		testService1 = new TestService1();
		testServiceRegistration = Activator.getDefault().getContext().registerService(new String[]{TestService1.class.getName()}, testService1, null);
	}
	
	@Override
	protected void tearDown() throws Exception {
		if (endpointListenerReg != null) {
			endpointListenerReg.unregister();
			endpointListenerReg = null;
		}
	}
	
	private String isInterested(Object scopeobj, EndpointDescription description) {
		if (scopeobj instanceof List<?>) {
			List<String> scope = (List<String>) scopeobj;
			for (Iterator<String> it = scope.iterator(); it.hasNext();) {
				String filter = it.next();

				if (description.matches(filter)) {
					return filter;
				}
			}
		} else if (scopeobj instanceof String[]) {
			String[] scope = (String[]) scopeobj;
			for (String filter : scope) {
				if (description.matches(filter)) {
					return filter;
				}
			}
		} else if (scopeobj instanceof String) {
			StringTokenizer st = new StringTokenizer((String)scopeobj, " ");
			for (; st.hasMoreTokens();) {
				String filter = st.nextToken();
				if (description.matches(filter)) {
					return filter;
				}
			}
		}
		return null;
	}

	public void testEndpointDescription() throws Exception {
		Long endpointID = new Long(12345);
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("mykey", "has been overridden");
		properties.put(RemoteConstants.SERVICE_IMPORTED, TestService1.class.getName());
		properties.put(RemoteConstants.ENDPOINT_SERVICE_ID, endpointID);
		properties.put(RemoteConstants.ENDPOINT_FRAMEWORK_UUID, Activator.getDefault().getContext().getProperty("org.osgi.framework.uuid"));
		properties.put(RemoteConstants.ENDPOINT_ID, "someURI"); // mandatory
		properties.put(RemoteConstants.SERVICE_IMPORTED_CONFIGS, "A"); // mandatory
		properties.put("service.exported.interfaces", new String[] { TestServiceInterface1.class.getName() });
		EndpointDescription endpoint = new EndpointDescription(testServiceRegistration.getReference(), properties);
		
		String filter = "(" + EndpointListener.ENDPOINT_LISTENER_SCOPE + "=*)"; // see 122.6.1

		ServiceReference[] refs = Activator.getDefault().getContext().getServiceReferences(EndpointListener.class.getName(),filter);
		
		for (ServiceReference sr : refs) {
			EndpointListener listener = (EndpointListener) Activator.getDefault().getContext().getService(sr);
			Object scope = sr.getProperty(EndpointListener.ENDPOINT_LISTENER_SCOPE);
			
			String matchedFilter = isInterested(scope, endpoint);
			
			if (matchedFilter != null) 
				listener.endpointAdded(endpoint, matchedFilter);
		}
		Thread.sleep(10000);
		assertTrue(endpointsReceived.size()==1);
	}
}
