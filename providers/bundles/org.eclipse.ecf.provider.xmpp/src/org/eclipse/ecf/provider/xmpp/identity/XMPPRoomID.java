package org.eclipse.ecf.provider.xmpp.identity;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.ecf.core.identity.BaseID;
import org.eclipse.ecf.core.identity.Namespace;

public class XMPPRoomID extends BaseID {
	
	private static final long serialVersionUID = -4843967090539640622L;
	public static final String DOMAIN_DEFAULT = "conference";
	public static final String NICKNAME = "nickname";
	
	URI uri = null;
	
	protected String fixHostname(String host, String domain) {
		String subHost = host;
		int dotIndex = host.indexOf('.');
		if (dotIndex > 0) subHost = host.substring(0,dotIndex);
		if (domain == null) domain = DOMAIN_DEFAULT;
		return domain+"."+subHost;
	}
	public XMPPRoomID(Namespace namespace, String username, String host, String domain, String groupname, String nickname) throws URISyntaxException {
		String hostname = fixHostname(host,domain);
		String query = NICKNAME+"="+((nickname==null)?username:nickname);
		uri = new URI(namespace.getScheme(),username,hostname,-1,"/"+groupname,query,null);
	}
	public XMPPRoomID(Namespace namespace, XMPPID userid, String domain, String groupname, String nickname) throws URISyntaxException {
		this(namespace,userid.getUsername(),userid.getHostname(),domain,groupname,nickname);
	}
	protected int namespaceCompareTo(BaseID o) {
        return getName().compareTo(o.getName());
	}
	protected boolean namespaceEquals(BaseID o) {
		if (!(o instanceof XMPPRoomID)) {
			return false;
		}
		XMPPRoomID other = (XMPPRoomID) o;
		return uri.equals(other.uri);
	}
	protected String fixPath(String path) {
		while (path.startsWith("/")) {
			path = path.substring(1);
		}
		return path;
	}
	protected String namespaceGetName() {
		String path = uri.getPath();
		return fixPath(path);
	}
	protected int namespaceHashCode() {
		return uri.hashCode();
	}
	protected URI namespaceToURI() throws URISyntaxException {
		return uri;
	}
	public String getMucString() {
		String host = uri.getHost();
		String group = fixPath(uri.getPath());
		String res = group + "@" + host;
		return res;
	}
	public String getNickname() {
		String query = uri.getQuery();
		if (query == null) {
			return uri.getUserInfo();
		} else {
			return query.substring(query.indexOf('=')+1);
		}
	}
	public String toString() {
		StringBuffer sb = new StringBuffer("XMPPRoomID[");
		sb.append(uri).append("]");
		return sb.toString();
	}
}
