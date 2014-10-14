/*******************************************************************************
 * Copyright (c) 2014 CohesionForce Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     CohesionForce Inc - initial API and implementation
 *******************************************************************************/

package org.eclipse.ecf.internal.provider.filetransfer.scp;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.IConnectContext;
import org.eclipse.ecf.core.util.Proxy;
import org.eclipse.ecf.filetransfer.IRemoteFile;
import org.eclipse.ecf.filetransfer.IRemoteFileSystemListener;
import org.eclipse.ecf.filetransfer.identity.*;
import org.eclipse.ecf.provider.filetransfer.browse.AbstractFileSystemBrowser;
import org.eclipse.ecf.provider.filetransfer.browse.URLRemoteFile;
import org.eclipse.ecf.provider.filetransfer.identity.FileTransferNamespace;

/**
 * The ScpFileSystemBrowser uses the JCraft JSch package to run remote commands
 * using SSH to list files in a directory.
 *
 */
public class ScpFileSystemBrowser extends AbstractFileSystemBrowser implements
		IScpFileTransfer {

	protected InputStream inputStream;

	protected OutputStream outputStream;

	protected ScpUtil scpUtil;

	protected ChannelExec channel;
	protected String username;

	protected static final String SCP_EXEC = System.getProperty(
			"org.eclipse.ecf.filetransfer.scp.filebrowse.exec", "exec"); //$NON-NLS-1$
	protected static final String LS_START_COMMAND = System.getProperty(
			"org.eclipse.ecf.filetransfer.scp.filebrowse.lscommand.start",
			"for file in "); //$NON-NLS-1$; //$NON-NLS-1$
	protected static final String LS_END_COMMAND = System.getProperty(
			"org.eclipse.ecf.filetransfer.scp.filebrowse.lscommand.end",
			"/*; do stat --format='%F|%s|%Y|%n' $file; done "); //$NON-NLS-1$; //$NON-NLS-1$

	/**
	 * Constructor for creating a ScpFileSystemBrowser.
	 * 
	 * @param directoryOrFileID
	 *            - ID of the remote location to browse
	 * @param listener
	 *            - will be called asynchronously with events resulting from
	 *            file browsing.
	 * @param url
	 *            - URL of the parent directory to browse.
	 * @param connectContext
	 *            - contains username/password to use for the ssh connection
	 * @param proxy
	 *            - proxy to be used if set.
	 */
	public ScpFileSystemBrowser(IFileID directoryOrFileID,
			IRemoteFileSystemListener listener, URL url,
			IConnectContext connectContext, Proxy proxy) {
		super(directoryOrFileID, listener, url, connectContext, proxy);
		username = directoryOrFile.getUserInfo();
	}

	/**
	 * Method called from super class to build the list of remote files.
	 */
	protected void runRequest() throws Exception {
		try {

			scpUtil = new ScpUtil(this);
			final Session s = scpUtil.getSession();
			s.connect();
			if (s.isConnected()) {
				final String targetFileName = scpUtil
						.trimTargetFile(directoryOrFile.getPath());
				final String command = LS_START_COMMAND + targetFileName
						+ LS_END_COMMAND;
				channel = (ChannelExec) s.openChannel(SCP_EXEC);
				channel.setCommand(command);
				final OutputStream outs = channel.getOutputStream();
				inputStream = channel.getInputStream();
				channel.connect();

				setOutputStream(outs);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream));
				String line = reader.readLine();
				ArrayList strings = new ArrayList();
				while (line != null) {
					strings.add(line);
					line = reader.readLine();
				}
				remoteFiles = new IRemoteFile[strings.size()];
				for (int i = 0; i < strings.size(); i++) {
					remoteFiles[i] = createRemoteFile((String) strings.get(i));
				}
			}
		} catch (final Exception e) {
			Activator.getDefault().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							IStatus.ERROR, "runRequest", e)); //$NON-NLS-1$
		}
	}

	private IRemoteFile createRemoteFile(String string)
			throws FileCreateException, SecurityException {
		URLRemoteFile file = null;
		IFileID id = null;
		String[] parts = string.split("\\|");

		// Check to see if this string can be parsed
		if (parts.length < 4) {
			id = FileIDFactory.getDefault().createFileID(
					IDFactory.getDefault().getNamespaceByName(
							FileTransferNamespace.PROTOCOL), "scp://unknown");
			file = new URLRemoteFile(0, 0, id);
		} else {

			// Build the filename back up, since the filename may also contain
			// "|"
			// characters
			StringBuilder builder = new StringBuilder("scp://");
			for (int i = 3; i < parts.length; i++) {
				builder.append(parts[i]);

				// Put the | back into the name
				if (i > 3 && i < parts.length - 1) {
					builder.append("|");
				}
			}

			// If it's a directory, then make sure it ends with /
			if (parts[0].equals("directory")
					&& !builder.toString().endsWith("/")) {
				builder.append("/");
			} else if (!parts[0].equals("directory")
					&& builder.toString().endsWith("/")) {
				builder.deleteCharAt(builder.length() - 1);
			}

			// Create the FileID
			id = FileIDFactory.getDefault()
					.createFileID(
							IDFactory.getDefault().getNamespaceByName(
									FileTransferNamespace.PROTOCOL),
							builder.toString());
			long size = Long.parseLong(parts[1]);
			long modification = Long.parseLong(parts[2]);
			file = new URLRemoteFile(modification, size, id);
		}
		return file;
	}

	protected void cleanUp() {
		super.cleanUp();
		// FIXME - This code is from ScpOutgoingFileTransfer, but it throws
		// exceptions
		// if (scpUtil != null) {
		// scpUtil.sendZeroToStream(outputStream);
		// scpUtil.checkAck(inputStream);
		// }
		if (channel != null) {
			channel.disconnect();
			channel = null;
		}
		if (scpUtil != null) {
			scpUtil.dispose();
			scpUtil = null;
		}
		try {
			if (inputStream != null)
				inputStream.close();
		} catch (final IOException e) {
			Activator.getDefault().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							IStatus.ERROR, "cleanup", e)); //$NON-NLS-1$
		}
		try {
			if (outputStream != null)
				outputStream.close();
		} catch (final IOException e) {
			Activator.getDefault().log(
					new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							IStatus.ERROR, "cleanup", e)); //$NON-NLS-1$
		}
		inputStream = null;
		outputStream = null;
	}

	protected void setupProxy(Proxy proxy) {
		this.proxy = proxy;
		this.setupProxies();
	}

	protected void setInputStream(InputStream ins) {
		inputStream = ins;
	}

	protected void setOutputStream(OutputStream outs) {
		outputStream = outs;
	}

	public IConnectContext getConnectContext() {
		return connectContext;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public URL getTargetURL() {
		return directoryOrFile;
	}

	public Map getOptions() {
		return null;
	}

}
