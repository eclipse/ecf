package org.eclipse.ecf.osgi.services.remoteserviceadmin;

import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
import org.osgi.framework.ServiceReference;

public class ImportRegistration implements
		org.osgi.service.remoteserviceadmin.ImportRegistration {

	private IRemoteServiceContainerAdapter containerAdapter;
	private IRemoteServiceReference rsReference;
	private ImportReference importReference;
	private Throwable throwable;
	private final Object closeLock = new Object();

	protected ImportRegistration(
			IRemoteServiceContainerAdapter containerAdapter,
			IRemoteServiceReference rsReference,
			ServiceReference serviceReference,
			EndpointDescription endpointDescription) {
		this.containerAdapter = containerAdapter;
		this.rsReference = rsReference;
		this.importReference = new ImportReference(serviceReference,
				endpointDescription);
	}

	public IRemoteServiceReference getRemoteServiceReference() {
		return rsReference;
	}

	public IRemoteServiceContainerAdapter getContainerAdapter() {
		return containerAdapter;
	}

	public ImportReference getImportReference() {
		synchronized (closeLock) {
			Throwable t = getException();
			if (t != null)
				throw new IllegalStateException(
						"Cannot get import reference as registration not properly initialized",
						t);
			return importReference;
		}
	}

	public void close() {
		synchronized (closeLock) {
			if (rsReference != null) {
				containerAdapter.ungetRemoteService(rsReference);
				rsReference = null;
				containerAdapter = null;
			}
			if (importReference != null) {
				importReference.close();
				importReference = null;
			}
			throwable = null;
		}
	}

	public Throwable getException() {
		synchronized (closeLock) {
			return throwable;
		}
	}

	@Override
	public String toString() {
		return "ImportRegistration [rsReference=" + rsReference
				+ ", importReference=" + importReference + ", throwable="
				+ throwable + ", closeLock=" + closeLock + "]";
	}

}
