package org.eclipse.ecf.provider.jmdns.identity;

import org.eclipse.ecf.core.identity.Namespace;
import org.eclipse.ecf.core.identity.ServiceID;

public class JMDNSServiceID extends ServiceID {

	private static final String DELIMITER = ".";
	private static final long serialVersionUID = 1L;

	public JMDNSServiceID(Namespace namespace, String type, String name) {
		super(namespace, type, name);
	}

	protected String getFullyQualifiedName() {
		if (name == null)
			return type;
		else
			return name+DELIMITER+type;
	}

}
