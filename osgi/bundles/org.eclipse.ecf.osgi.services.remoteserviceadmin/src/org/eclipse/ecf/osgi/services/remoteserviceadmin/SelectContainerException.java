package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.core.ContainerTypeDescription;

public class SelectContainerException extends Exception {

	private static final long serialVersionUID = -5507248105370677422L;

	private ContainerTypeDescription containerTypeDescription;

	public SelectContainerException(String message, Throwable cause,
			ContainerTypeDescription containerTypeDescription) {
		super(message, cause);
		this.containerTypeDescription = containerTypeDescription;
	}

	public ContainerTypeDescription getContainerTypeDescription() {
		return containerTypeDescription;
	}
}
