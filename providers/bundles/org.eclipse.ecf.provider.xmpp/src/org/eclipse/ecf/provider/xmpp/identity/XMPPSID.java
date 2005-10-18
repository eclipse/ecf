package org.eclipse.ecf.provider.xmpp.identity;

import java.net.URISyntaxException;
import org.eclipse.ecf.core.identity.Namespace;

public class XMPPSID extends XMPPID {

	private static final long serialVersionUID = -7665808387581704917L;

	public XMPPSID(Namespace namespace, String unamehost) throws URISyntaxException {
		super(namespace,unamehost);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("XMPPSID[");
		sb.append(uri.toString()).append("]");
		return sb.toString();
	}

}
