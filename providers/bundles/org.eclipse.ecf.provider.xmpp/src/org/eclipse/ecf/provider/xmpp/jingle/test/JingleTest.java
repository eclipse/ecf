package org.eclipse.ecf.provider.xmpp.jingle.test;

import org.eclipse.ecf.call.ICallContainer;
import org.eclipse.ecf.provider.xmpp.container.XMPPClientSOContainer;

public class JingleTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			XMPPClientSOContainer client1= new XMPPClientSOContainer();

			// client1 make a call to client2
			// this installs request listener for incoming sessions
			ICallContainer jingle1= (ICallContainer)client1.getAdapter( ICallContainer.class );
			// create a call session
			jingle1.createCallSession( client1.getID() );


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
