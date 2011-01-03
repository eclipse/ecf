package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.core.identity.ID;
import org.osgi.framework.Bundle;
import org.osgi.service.remoteserviceadmin.ExportReference;
import org.osgi.service.remoteserviceadmin.ImportReference;

public class RemoteServiceAdminEvent extends
		org.osgi.service.remoteserviceadmin.RemoteServiceAdminEvent {

	private ID containerID;

	public RemoteServiceAdminEvent(ID containerID, int type, Bundle source,
			ExportReference exportReference, Throwable exception) {
		super(type, source, exportReference, exception);
		this.containerID = containerID;
	}

	public RemoteServiceAdminEvent(ID containerID, int type, Bundle source,
			ImportReference importReference, Throwable exception) {
		super(type, source, importReference, exception);
		this.containerID = containerID;
	}

	public ID getContainerID() {
		return containerID;
	}

}
