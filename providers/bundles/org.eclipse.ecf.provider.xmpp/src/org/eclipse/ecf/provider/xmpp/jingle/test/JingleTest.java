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
			XMPPClientSOContainer client2= new XMPPClientSOContainer();
			
			ICallContainer jingle1= (ICallContainer)client1.getAdapter( ICallContainer.class );
			ICallContainer jingle2= (ICallContainer)client2.getAdapter( ICallContainer.class );
			
			jingle1.createCallSession( client1.getID() );
			jingle2.createCallSession( client2.getID() );
			
			// client1 make a call to client2
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
