/*******************************************************************************
 * Copyright (c) 2018 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.osgi.services.remoteserviceadmin.console;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Converter;
import org.apache.felix.service.command.Parameter;
import org.eclipse.ecf.console.AbstractCommand;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IIDFactory;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.DebugRemoteServiceAdminListener;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionReader;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionWriter;
import org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteServiceAdmin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

@Component(immediate = true, property = { "osgi.command.scope=ecf", "osgi.command.function=listexports",
		"osgi.command.function=lex", "osgi.command.function=listimports", "osgi.command.function=lim",
		"osgi.command.function=unexport", "osgi.command.function=une", "osgi.command.function=unimport",
		"osgi.command.function=uni",
		"osgi.command.function=rsadebug",
		"osgi.command.function=rsexport",
		"osgi.command.function=rsimport" }, service = { RSACommand.class, Converter.class })
public class RSACommand extends AbstractCommand implements Converter {

	private static final boolean DEBUGON = Boolean
			.parseBoolean(System.getProperty("org.eclipse.ecf.osgi.services.remoteserviceadmin.debug", "true"));

	private IContainerManager containerManager;
	private IIDFactory idFactory;
	private RemoteServiceAdmin rsa;

	@Reference
	void bindContainerManager(IContainerManager cm) {
		this.containerManager = cm;
	}

	void unbindContainerManager(IContainerManager cm) {
		this.containerManager = null;
	}

	@Reference
	void bindIDFactory(IIDFactory idf) {
		this.idFactory = idf;
	}

	void unbindIDFactory(IIDFactory idf) {
		this.idFactory = null;
	}

	@Reference
	void bindRSA(org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa) {
		this.rsa = (RemoteServiceAdmin) rsa;
	}

	void unbindRSA(org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa) {
		this.rsa = null;
	}

	@Override
	protected IContainerManager getContainerManager() {
		return this.containerManager;
	}

	@Override
	protected IIDFactory getIDFactory() {
		return this.idFactory;
	}

	private RemoteServiceAdmin getRSA() {
		return this.rsa;
	}

	private RemoteServiceAdmin.ExportReference getExportReferenceForIdOrContainerId(String exportRefId) {
		for (RemoteServiceAdmin.ExportReference r : getExports()) {
			EndpointDescription ed = (EndpointDescription) r.getExportedEndpoint();
			if (ed != null && (ed.getId().equals(exportRefId) || ed.getContainerID().getName().equals(exportRefId)))
				return r;
		}
		return null;
	}

	private RemoteServiceAdmin.ExportRegistration getExportRegistrationForId(String id) {
		for (RemoteServiceAdmin.ExportRegistration r : getRSA().getExportedRegistrations()) {
			RemoteServiceAdmin.ExportReference er = (RemoteServiceAdmin.ExportReference) r.getExportReference();
			if (er != null) {
				EndpointDescription ed = (EndpointDescription) er.getExportedEndpoint();
				if (ed != null && ed.getId().equals(id))
					return r;
			}
		}
		return null;
	}

	private RemoteServiceAdmin.ImportRegistration getImportRegistrationForId(String id) {
		for (RemoteServiceAdmin.ImportRegistration r : getRSA().getImportedRegistrations()) {
			RemoteServiceAdmin.ImportReference er = (RemoteServiceAdmin.ImportReference) r.getImportReference();
			if (er != null) {
				EndpointDescription ed = (EndpointDescription) er.getImportedEndpoint();
				if (ed != null && ed.getId().equals(id))
					return r;
			}
		}
		return null;
	}

	private RemoteServiceAdmin.ExportReference getExportReferenceForServiceId(Long serviceId) {
		for (RemoteServiceAdmin.ExportReference r : getExports()) {
			EndpointDescription ed = (EndpointDescription) r.getExportedEndpoint();
			if (ed != null && ed.getServiceId() == (long) serviceId)
				return r;
		}
		return null;
	}

	private RemoteServiceAdmin.ImportReference getImportReferenceForServiceId(Long serviceId) {
		for (RemoteServiceAdmin.ImportReference r : getImports()) {
			EndpointDescription ed = (EndpointDescription) r.getImportedEndpoint();
			if (ed != null && ed.getServiceId() == (long) serviceId)
				return r;
		}
		return null;
	}

	private RemoteServiceAdmin.ImportReference getImportReferenceForIdOrContainerId(String importRefId) {
		for (RemoteServiceAdmin.ImportReference r : getImports()) {
			EndpointDescription ed = (EndpointDescription) r.getImportedEndpoint();
			if (ed != null && (ed.getId().equals(importRefId) || ed.getContainerID().getName().equals(importRefId)))
				return r;
		}
		return null;
	}

	public static final String EXPORT_LINE_FORMAT = "%1$-37s|%2$-45s|%3$s";
	public static final String IMPORT_LINE_FORMAT = "%1$-37s|%2$-45s|%3$s";

	public List<RemoteServiceAdmin.ExportReference> listexports(CommandSession cs) {
		consoleLine(cs, EXPORT_LINE_FORMAT, "Endpoint Id", "Exporting Container ID", "Exported Service Id\n");
		return getExports();
	}

	public List<RemoteServiceAdmin.ExportReference> lex(CommandSession cs) {
		return listexports(cs);
	}

	public RemoteServiceAdmin.ExportReference listexports(RemoteServiceAdmin.ExportReference r) {
		return r;
	}

	public RemoteServiceAdmin.ExportReference lex(RemoteServiceAdmin.ExportReference r) {
		return r;
	}

	public List<RemoteServiceAdmin.ImportReference> listimports(CommandSession cs) {
		consoleLine(cs, IMPORT_LINE_FORMAT, "Endpoint Id", "Importing Container ID", "Imported Service Id\n");
		return getImports();
	}

	public List<RemoteServiceAdmin.ImportReference> lim(CommandSession cs) {
		return listimports(cs);
	}

	public RemoteServiceAdmin.ImportReference listimports(RemoteServiceAdmin.ImportReference r) {
		return r;
	}

	public RemoteServiceAdmin.ImportReference lim(RemoteServiceAdmin.ImportReference r) {
		return r;
	}

	public String unexport(String endpointId) {
		RemoteServiceAdmin.ExportRegistration reg = getExportRegistrationForId(endpointId);
		if (reg != null) {
			reg.close();
			return endpointId + " unexported";
		}
		return endpointId + " not found";
	}

	public String une(String endpointId) {
		return unexport(endpointId);
	}

	public String unimport(String endpointId) {
		RemoteServiceAdmin.ImportRegistration reg = getImportRegistrationForId(endpointId);
		if (reg != null) {
			reg.close();
			return endpointId + " unimported";
		}
		return endpointId + " not found";
	}

	public String uni(String endpointId) {
		return unimport(endpointId);
	}

	private List<RemoteServiceAdmin.ExportReference> getExports() {
		List<RemoteServiceAdmin.ExportReference> results = new ArrayList<RemoteServiceAdmin.ExportReference>();
		for (org.osgi.service.remoteserviceadmin.ExportReference er : getRSA().getExportedServices())
			results.add((RemoteServiceAdmin.ExportReference) er);
		return results;
	}

	public List<RemoteServiceAdmin.ImportReference> getImports() {
		List<RemoteServiceAdmin.ImportReference> results = new ArrayList<RemoteServiceAdmin.ImportReference>();
		for (org.osgi.service.remoteserviceadmin.ImportReference er : getRSA().getImportedEndpoints())
			results.add((RemoteServiceAdmin.ImportReference) er);
		return results;
	}

	public Object convert(Class<?> desiredType, Object in) throws Exception {
		if (desiredType == RemoteServiceAdmin.ExportReference.class && in instanceof String)
			return getExportReferenceForIdOrContainerId((String) in);
		else if (desiredType == RemoteServiceAdmin.ExportReference.class && in instanceof Long)
			return getExportReferenceForServiceId((Long) in);
		else if (desiredType == RemoteServiceAdmin.ImportReference.class && in instanceof String)
			return getImportReferenceForIdOrContainerId((String) in);
		else if (desiredType == RemoteServiceAdmin.ExportReference.class && in instanceof Long)
			return getImportReferenceForServiceId((Long) in);

		return null;
	}

	private String formatExportReference(RemoteServiceAdmin.ExportReference er, int level, Converter escape) {
		EndpointDescription ed = (EndpointDescription) er.getExportedEndpoint();
		if (ed == null)
			return null;
		return formatReference(EXPORT_LINE_FORMAT, ed, ed.getContainerID().getName(), ed.getServiceId(), level, escape);
	}

	private String formatReference(String lineFormat, EndpointDescription ed, String containerId, long serviceId,
			int level, Converter escape) {
		if (ed == null)
			return null;
		switch (level) {
		case Converter.PART:
			return null;
		case Converter.LINE:
			return formatLine(lineFormat, ed.getId(), containerId, serviceId);
		case Converter.INSPECT:
			return formatEndpoint(ed);
		default:
			return null;
		}
	}

	private String formatImportReference(RemoteServiceAdmin.ImportReference ir, int level, Converter escape) {
		ID localContainerID = ir.getLocalContainerID();
		if (localContainerID == null)
			return null;
		@SuppressWarnings("rawtypes")
		ServiceReference ref = ir.getImportedService();
		if (ref == null)
			return null;
		return formatReference(IMPORT_LINE_FORMAT, (EndpointDescription) ir.getImportedEndpoint(),
				localContainerID.getName(), (Long) ref.getProperty(Constants.SERVICE_ID), level, escape);
	}

	private String formatEndpoint(EndpointDescription ed) {
		EndpointDescriptionWriter edw = new EndpointDescriptionWriter();
		StringWriter sw = new StringWriter();
		try {
			edw.writeEndpointDescription(sw, ed);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return formatLine("%s", sw.toString());
	}

	public String format(Object target, int level, Converter escape) {
		if (target instanceof RemoteServiceAdmin.ExportReference)
			return formatExportReference((RemoteServiceAdmin.ExportReference) target, level, escape);
		else if (target instanceof RemoteServiceAdmin.ImportReference)
			return formatImportReference((RemoteServiceAdmin.ImportReference) target, level, escape);
		return null;
	}

	private BundleContext context;
	private ServiceRegistration<?> debugReg;

	@Activate
	void activate(BundleContext context) {
		this.context = context;
		if (DEBUGON)
			debugOn();
	}

	@Deactivate
	void deactivate() {
		debugOff();
	}

	synchronized void debugOff() {
		if (debugReg != null) {
			debugReg.unregister();
			debugReg = null;
		}
	}

	synchronized void debugOn() {
		if (debugReg == null)
			debugReg = this.context.registerService(
					org.osgi.service.remoteserviceadmin.RemoteServiceAdminListener.class,
					new DebugRemoteServiceAdminListener(), null);
	}

	public String rsadebug(boolean on) {
		String msg = null;
		synchronized (this) {
			if (debugReg == null) {
				if (on) {
					debugOn();
					msg = "RSA debugging ON";
				} else 
					msg = "RSA debugging already on";
			} else {
				if (debugReg != null) {
					debugOff();
					msg = "RSA debugging OFF";
				} else
					msg = "RSA debugging already off";
			}
		}
		return msg;
	}

	public RemoteServiceAdmin.ExportReference rsexport(CommandSession cs, @Parameter(names = { "-s", "--serviceid" }, absentValue = "") long serviceid,
			@Parameter(names = { "--properties", "-p" }, absentValue = "") Map<String,?> map) {
		ServiceReference<?> ref = null;
		try {
			ServiceReference<?>[] refs = context.getAllServiceReferences(null, "("+Constants.SERVICE_ID+"="+String.valueOf(serviceid)+")");
			if (refs == null || refs.length < 1)
				cs.getConsole().println("Cannot find service with id="+String.valueOf(serviceid));
			ref = refs[0];
		} catch (InvalidSyntaxException e) {
			e.printStackTrace(cs.getConsole());
			return null;
		}
		Map<String,Object> op = new HashMap<String,Object>(map);
		if (!op.containsKey(RemoteConstants.SERVICE_EXPORTED_INTERFACES))
			op.put(RemoteConstants.SERVICE_EXPORTED_INTERFACES, "*");
		if (!op.containsKey(RemoteConstants.SERVICE_EXPORTED_CONFIGS))
			op.put(RemoteConstants.SERVICE_EXPORTED_CONFIGS, "ecf.generic.server");
		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> regs = getRSA().exportService(ref, op);
		for(org.osgi.service.remoteserviceadmin.ExportRegistration reg: regs) {
			Throwable t = reg.getException();
			if (t != null) 
				t.printStackTrace(cs.getConsole());
			else {
				RemoteServiceAdmin.ExportReference er = (RemoteServiceAdmin.ExportReference) reg.getExportReference();
				if (er != null)
					return er;
			}
		}
		return null;
	}
	
	public RemoteServiceAdmin.ImportReference rsimport(CommandSession cs, @Parameter(names = { "-e", "--endpointdescriptionurl" }, absentValue="") String endpointurl) {
		InputStream ins = null;
		URL url = null;
		if ("".equals(endpointurl)) {
			ins = cs.getKeyboard();
			cs.getConsole().println("Waiting for console input.   To complete enter an empty line...");
		} else {
			try {
				url = new URL(endpointurl);
				ins = url.openStream();
			} catch (IOException e) {
				e.printStackTrace(cs.getConsole());
			}
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(ins));
		StringBuffer buf = new StringBuffer();
		// read from the input stream until an empty line is encountered
		while (true) {
			try {
				String line = br.readLine();
				if (line != null && line.length() > 0)
					buf.append(line).append("\n");
				else
					break;
			} catch (IOException e) {
				e.printStackTrace(cs.getConsole());
				return null;
			}
		}
		// Close the input stream if this was from a url
		if (url != null) 
			try {
				ins.close();
			} catch (IOException e) {
				e.printStackTrace(cs.getConsole());
			}
		
		ByteArrayInputStream bins = new ByteArrayInputStream(buf.toString().getBytes());
		EndpointDescriptionReader r = new EndpointDescriptionReader();
		org.osgi.service.remoteserviceadmin.EndpointDescription[] eds = null;
		try {
			 eds = r.readEndpointDescriptions(bins);
		} catch (IOException e) {
			e.printStackTrace(cs.getConsole());
			return null;
		}
		// should be only one
		org.osgi.service.remoteserviceadmin.ImportRegistration reg = getRSA().importService(eds[0]);
		if (reg == null)
			return null;
		else {
			Throwable t = reg.getException();
			if (t != null) {
				t.printStackTrace(cs.getConsole());
				return null;
			} else {
				RemoteServiceAdmin.ImportReference ir = (RemoteServiceAdmin.ImportReference) reg.getImportReference();
				if (ir != null)
					return ir;
				else
					return null;
			}
		}
	}
}
