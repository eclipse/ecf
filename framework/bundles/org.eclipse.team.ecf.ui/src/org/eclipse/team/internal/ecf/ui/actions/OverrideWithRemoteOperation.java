/****************************************************************************
 * Copyright (c) 2008 Versant Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Remy Chi Jian Suen (Versant Corporation) - initial API and implementation
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.team.internal.ecf.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.ecf.ui.Messages;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;

class OverrideWithRemoteOperation extends SynchronizeModelOperation {

	protected OverrideWithRemoteOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
		super(configuration, elements);
	}

	private ISchedulingRule createSchedulingRule(Collection rules) {
		if (rules.size() == 1) {
			return (ISchedulingRule) rules.iterator().next();
		}
		return new MultiRule((ISchedulingRule[]) rules.toArray(new ISchedulingRule[rules.size()]));
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException {
		SyncInfoSet syncInfoSet = getSyncInfoSet();
		SyncInfo[] syncInfos = syncInfoSet.getSyncInfos();

		Set projects = new HashSet();
		for (int i = 0; i < syncInfos.length; i++) {
			projects.add(syncInfos[i].getLocal().getProject());
		}

		try {
			ResourcesPlugin.getWorkspace().run(new OverrideWithRemoteRunnable(syncInfos), createSchedulingRule(projects), IWorkspace.AVOID_UPDATE, monitor);
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	class OverrideWithRemoteRunnable implements IWorkspaceRunnable {

		private SyncInfo[] syncInfos;

		public OverrideWithRemoteRunnable(SyncInfo[] syncInfos) {
			this.syncInfos = syncInfos;
		}

		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.beginTask("", syncInfos.length); //$NON-NLS-1$
			monitor.subTask(Messages.OverrideWithRemoteOperation_SubTaskName);

			for (int i = 0; i < syncInfos.length; i++) {
				if (monitor.isCanceled()) {
					return;
				}

				IResourceVariant remoteVariant = syncInfos[i].getRemote();
				IResource resource = syncInfos[i].getLocal();

				switch (syncInfos[i].getKind() & SyncInfo.CHANGE_MASK) {
					case SyncInfo.ADDITION :
						monitor.subTask(NLS.bind(Messages.OverrideWithRemoteOperation_CreatingResource, resource.getName()));
						switch (resource.getType()) {
							case IResource.FILE :
								IStorage storage = remoteVariant.getStorage(null);
								// create parent folders of the resource if applicable
								createParents(resource);
								((IFile) resource).create(storage.getContents(), true, new SubProgressMonitor(monitor, 1));
								break;
							case IResource.FOLDER :
								// technically, the folder shouldn't exist if we're supposed
								// to be adding the resource, however, we precreate parents
								// of files when creating files and the parent folder may be
								// created as a side effect of that, so we add this check
								// here, note, not having this call was causing problems in
								// RemoteSyncInfo's calculateKind() method
								if (!resource.exists()) {
									((IFolder) resource).create(true, true, new SubProgressMonitor(monitor, 1));
								}
								break;
							default :
								monitor.worked(1);
								break;
						}
						break;
					case SyncInfo.CHANGE :
						switch (resource.getType()) {
							case IResource.FILE :
								monitor.subTask(NLS.bind(Messages.OverrideWithRemoteOperation_ReplacingResource, resource.getName()));
								IStorage storage = remoteVariant.getStorage(null);
								((IFile) resource).setContents(storage.getContents(), true, true, new SubProgressMonitor(monitor, 1));
								break;
							default :
								monitor.worked(1);
								break;
						}
						break;
					case SyncInfo.DELETION :
						if (resource.exists()) {
							monitor.subTask(NLS.bind(Messages.OverrideWithRemoteOperation_DeletingResource, resource.getName()));
						}
						resource.delete(true, new SubProgressMonitor(monitor, 1));
						break;
				}
			}
		}

		private void createParents(IResource resource) throws CoreException {
			IContainer container = resource.getParent();
			while (!container.exists() && container.getType() == IResource.FOLDER) {
				IFolder folder = (IFolder) container;
				folder.create(true, true, null);
				container = folder.getParent();
			}
		}

	}
}
