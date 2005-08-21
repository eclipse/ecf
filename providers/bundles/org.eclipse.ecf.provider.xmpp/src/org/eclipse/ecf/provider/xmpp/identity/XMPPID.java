package org.eclipse.ecf.provider.xmpp.identity;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ecf.core.identity.BaseID;
import org.eclipse.ecf.core.identity.Namespace;

public class XMPPID extends BaseID {

	private static final long serialVersionUID = 3257289140701049140L;
	public static final char USER_HOST_DELIMITER = '@';
	
	URI uri;
	
	protected static String fixEscape(String src) {
		if (src == null) return null;
		return src.replaceAll("%","%25");
	}
	
	protected XMPPID(Namespace namespace, String username, String host, String query) throws URISyntaxException {
		super(namespace);
		username = fixEscape(username);
		uri = new URI(namespace.getScheme(),username,host,-1,"",query,null);
	}
	protected XMPPID(Namespace namespace, String username, String host) throws URISyntaxException {
		this(namespace,username,host,null);
	}
	protected XMPPID(Namespace namespace, String unamehost) throws URISyntaxException {
		super(namespace);
		unamehost = fixEscape(unamehost);
		if (unamehost == null) throw new URISyntaxException(unamehost,"username/host string cannot be null");
		int atIndex = unamehost.indexOf(USER_HOST_DELIMITER);
		if (atIndex == -1) throw new URISyntaxException(unamehost,"username/host string not valid.  Must be of form <username>@<hostname>");
		String username = unamehost.substring(0,atIndex);
		String host = unamehost.substring(atIndex+1);
		uri = new URI(namespace.getScheme(),username,host,-1,null,null,null);
	}
	
	protected int namespaceCompareTo(BaseID o) {
        return getName().compareTo(o.getName());
	}

	protected boolean namespaceEquals(BaseID o) {
		if (!(o instanceof XMPPID)) {
			return false;
		}
		XMPPID other = (XMPPID) o;
		return uri.equals(other.uri);
	}

	protected String namespaceGetName() {
		return getUsernameAtHost();
	}

	protected int namespaceHashCode() {
		return uri.hashCode();
	}

	protected URI namespaceToURI() throws URISyntaxException {
		return uri;
	}
	
	public String getUsername() {
		return uri.getUserInfo();
	}
	
	public String getHostname() {
		return uri.getHost();
	}
	public String getUsernameAtHost() {
		return getUsername()+"@"+getHostname();
	}
	public String toString() {
		StringBuffer sb = new StringBuffer("XMPPID[");
		sb.append(uri.toString()).append("]");
		return sb.toString();
	}

}
