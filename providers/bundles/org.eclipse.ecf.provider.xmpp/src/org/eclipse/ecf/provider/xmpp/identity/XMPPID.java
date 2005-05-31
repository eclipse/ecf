package org.eclipse.ecf.provider.xmpp.identity;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.ecf.core.identity.BaseID;
import org.eclipse.ecf.core.identity.Namespace;

public class XMPPID extends BaseID {

	private static final long serialVersionUID = 3257289140701049140L;
	
    public static final String ADDRESS_SEPARATOR = "@";
    public static final String PROTOCOL = "xmpp";
    
	URI uri;
	
	protected static String fixEscape(String src) {
		if (src == null) return null;
		return src.replaceAll("%","%25");
	}
	
	protected XMPPID(Namespace namespace, String username, String host, String query) throws URISyntaxException {
		super(namespace);
		username = fixEscape(username);
		uri = new URI(PROTOCOL+":"+username+ADDRESS_SEPARATOR+host+((query==null)?"":("?"+query)));
	}
	protected XMPPID(Namespace namespace, String username, String host) throws URISyntaxException {
		this(namespace,username,host,null);
	}
	protected XMPPID(Namespace namespace, String unamehost) throws URISyntaxException {
		super(namespace);
		unamehost = fixEscape(unamehost);
		uri = new URI(PROTOCOL+":"+unamehost);
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
		return uri.getSchemeSpecificPart();
	}

	protected int namespaceHashCode() {
		return uri.hashCode();
	}

	protected URI namespaceToURI() throws URISyntaxException {
		return uri;
	}
	
	public String getUsername() {
		String name = getName();
		if (name == null) return null;
		return name.substring(0,name.indexOf(ADDRESS_SEPARATOR));
	}
	
	public String getHostname() {
		String name = getName();
		if (name == null) return null;
		return name.substring(name.indexOf(ADDRESS_SEPARATOR)+1,name.length());
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("XMPPID[");
		sb.append(uri.toString()).append("]");
		return sb.toString();
	}

}
