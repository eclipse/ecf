package org.eclipse.ecf.tests.remoteservice.rest;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.remoteservice.IRemoteService;
import org.eclipse.ecf.remoteservice.IRemoteServiceRegistration;
import org.eclipse.ecf.remoteservice.client.IRemoteCallParameter;
import org.eclipse.ecf.remoteservice.client.IRemoteCallable;
import org.eclipse.ecf.remoteservice.client.IRemoteServiceClientContainerAdapter;
import org.eclipse.ecf.remoteservice.client.RemoteCallParameter;
import org.eclipse.ecf.remoteservice.rest.RestCallFactory;
import org.eclipse.ecf.remoteservice.rest.RestCallableFactory;
import org.eclipse.ecf.remoteservice.rest.client.HttpPutRequestType;

@SuppressWarnings("unused")
public class RestPutServiceTest extends AbstractRestTestCase {

	private String username = System.getProperty("rest.test.username","p126371rw");
	private String password = System.getProperty("rest.test.password","demo");
	private String uri = System.getProperty("rest.test.uri","http://phprestsql.sourceforge.net");
	private String resourcePath = System.getProperty("rest.test.resourcePath","/tutorial/user/1.xml");
	private String method = System.getProperty("rest.test.method","doPut");
	
	private IContainer container;
	private IRemoteServiceRegistration registration;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		container = createRestContainer(uri);
		IRemoteCallable callable = RestCallableFactory.createCallable(method,resourcePath,
				new IRemoteCallParameter[] { new RemoteCallParameter("body")} ,
				new HttpPutRequestType(HttpPutRequestType.STRING_REQUEST_ENTITY,"application/xml",-1,"UTF-8"));
		// Get adapter
		IRemoteServiceClientContainerAdapter adapter = (IRemoteServiceClientContainerAdapter) getRemoteServiceClientContainerAdapter(container);
		// Setup authentication info
		adapter.setConnectContextForAuthentication(ConnectContextFactory.createUsernamePasswordConnectContext(username, password));
		registration = adapter.registerCallables(new IRemoteCallable[] { callable } , null);
	}
	
	/*
	public void testPutCallSync() throws Exception {
		IRemoteService restClientService = getRemoteServiceClientContainerAdapter(container).getRemoteService(registration.getReference());
		Object result = restClientService.callSync(RestCallFactory.createRestCall(method, new String[] { getPutBody() }));
		System.out.println("result="+result);
	}
	*/
	
	private String getPutBody() {
		return "<row xmlns:xlink=\"http://www.w3.org/1999/xlink\"><uid>1</uid><firstname>Scott</firstname><surname>Example</surname><email>jim@example.org</email><company_uid xlink:href=\"/tutorial/company/1.xml\">1</company_uid></row>";
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		registration.unregister();
		container.disconnect();
	}
}
