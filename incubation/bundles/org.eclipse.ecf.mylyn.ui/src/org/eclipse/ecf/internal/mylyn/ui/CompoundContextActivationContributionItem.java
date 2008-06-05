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

import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.context.core.IInteractionContext;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.context.core.InteractionContext;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.TaskList;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.progress.UIJob;

public class CompoundContextActivationContributionItem extends CompoundContributionItem {

	static LinkedList tasks = new LinkedList();

	static Map contexts = new HashMap();

	private static ActivateTaskAction[] actions = new ActivateTaskAction[5];

	private static Shell shell;

	static {
		for (int i = 0; i < 5; i++) {
			actions[i] = new ActivateTaskAction();
		}
	}

	public void fill(Menu menu, int index) {
		super.fill(menu, index);
		shell = menu.getShell();
	}

	protected IContributionItem[] getContributionItems() {
		int count = 0;
		for (Iterator it = tasks.iterator(); it.hasNext() && count != 5;) {
			AbstractTask task = (AbstractTask) it.next();
			actions[count].setTask(task);
			count++;
		}

		IContributionItem[] array = null;

		if (count == 5 && tasks.size() != 5) {
			array = new IContributionItem[7];
			array[5] = new Separator();
			array[6] = new ActionContributionItem(new Action("Activate received task...") {
				public void run() {
					ActivateReceivedContextHandler.open(shell);
				}
			});
		} else {
			array = new IContributionItem[count];
		}

		for (int i = 0; i < count; i++) {
			array[i] = new ActionContributionItem(actions[i]);
		}

		return array;
	}

	static void enqueue(ITask task, IInteractionContext context) {
		tasks.add(task);
		contexts.put(task, context);
	}

	static class ActivateTaskAction extends Action {
		private ITask task;

		void setTask(ITask task) {
			this.task = task;
			setText(task.getSummary());
		}

		public void run() {
			final InteractionContext context = (InteractionContext) contexts.get(task);

			final TaskList taskList = TasksUiPlugin.getTaskList();
			if (taskList.getTask(task.getHandleIdentifier()) != null) {
				boolean confirmed = MessageDialog.openConfirm(shell, ITasksUiConstants.TITLE_DIALOG, "The task '" + task.getSummary() + "' already exists. Do you want to override its context with the source?");
				if (confirmed) {
					Job job = new Job("Import context") {
						protected IStatus run(IProgressMonitor monitor) {
							ContextCorePlugin.getContextManager().importContext(context);
							scheduleTaskActivationJob();
							return Status.OK_STATUS;
						}
					};
					job.schedule();
				} else {
					return;
				}
			} else {
				Job job = new Job("Import task") {
					protected IStatus run(IProgressMonitor monitor) {
						ContextCorePlugin.getContextManager().importContext(context);
						taskList.insertTask(task, null, null);
						scheduleTaskActivationJob();
						return Status.OK_STATUS;
					}
				};
				job.schedule();
			}
			tasks.remove(task);
			contexts.remove(task);
		}

		private void scheduleTaskActivationJob() {
			UIJob job = new UIJob(shell.getDisplay(), "Activate imported task") {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					TasksUiPlugin.getTaskListManager().activateTask((AbstractTask) task);
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
	}

}
