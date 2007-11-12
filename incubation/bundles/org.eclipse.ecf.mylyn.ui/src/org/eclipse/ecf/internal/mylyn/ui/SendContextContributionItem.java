/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ecf.internal.mylyn.ui;

import java.io.*;
import java.net.URLEncoder;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.datashare.IChannel;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuContributionItem;
import org.eclipse.ecf.presence.ui.menu.AbstractRosterMenuHandler;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.*;
import org.eclipse.mylyn.internal.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.core.AbstractTask;
import org.eclipse.mylyn.tasks.ui.TasksUiPlugin;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.handlers.HandlerUtil;

public class SendContextContributionItem extends AbstractRosterMenuContributionItem {

	public SendContextContributionItem() {
		setTopMenuName("Send Context");
		setTopMenuImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("IMG_SHARED_TASK"));
	}

	private String encodeName(AbstractTask task) {
		try {
			return URLEncoder.encode(task.getHandleIdentifier(), ITasksUiConstants.FILENAME_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	protected IContributionItem[] createContributionItemsForPresenceContainer(IPresenceContainerAdapter presenceContainerAdapter) {
		// if this IPCA doesn't support the datashare APIs, we should not create any contribution items 
		IChannelContainerAdapter channelAdapter = (IChannelContainerAdapter) presenceContainerAdapter.getAdapter(IChannelContainerAdapter.class);
		if (channelAdapter == null) {
			return new IContributionItem[0];
		}
		return super.createContributionItemsForPresenceContainer(presenceContainerAdapter);
	}

	protected AbstractRosterMenuHandler createRosterEntryHandler(final IRosterEntry rosterEntry) {
		return new AbstractRosterMenuHandler(rosterEntry) {
			public Object execute(ExecutionEvent event) throws ExecutionException {
				IWorkbenchPart part = HandlerUtil.getActivePart(event);
				if (part == null) {
					return null;
				}
				IWorkbenchSite site = part.getSite();
				if (site == null) {
					return null;
				}
				ISelectionProvider provider = site.getSelectionProvider();
				if (provider == null) {
					return null;
				}
				ISelection selection = provider.getSelection();
				if (selection instanceof IStructuredSelection) {
					IChannelContainerAdapter icca = (IChannelContainerAdapter) rosterEntry.getRoster().getPresenceContainerAdapter().getAdapter(IChannelContainerAdapter.class);
					ID channelID;
					try {
						channelID = icca.getChannelNamespace().createInstance(new Object[] {Activator.PLUGIN_ID});
					} catch (IDCreateException e1) {
						return null;
					}
					final IChannel channel = icca.getChannel(channelID);
					if (channel == null) {
						return null;
					}
					Object element = ((IStructuredSelection) selection).getFirstElement();
					if (element instanceof AbstractTask) {
						final AbstractTask task = (AbstractTask) element;
						Job job = new Job("Send Task") {
							protected IStatus run(IProgressMonitor monitor) {
								monitor.beginTask("Sending task...", 100);
								File tmpDir = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
								File outputFile = new File(tmpDir, encodeName(task));
								TasksUiPlugin.getTaskListManager().getTaskListWriter().writeTask(task, outputFile);
								monitor.worked(30);
								FileInputStream stream = null;
								try {
									byte[] data = new byte[(int) outputFile.length()];
									stream = new FileInputStream(outputFile);
									stream.read(data);
									monitor.worked(30);
									channel.sendMessage(getRosterEntry().getUser().getID(), data);
									monitor.worked(40);
								} catch (Exception e) {
									return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "An error occurred while sending the task.", e);
								} finally {
									try {
										if (stream != null) {
											stream.close();
										}
									} catch (Exception e) {
										// ignored
									}
									outputFile.delete();
									monitor.done();
								}
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
				}
				return null;
			}

		};
	}
}
