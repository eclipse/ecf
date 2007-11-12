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

import org.eclipse.core.commands.*;
import org.eclipse.ecf.internal.mylyn.ui.CompoundContextActivationContributionItem.ActivateTaskAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

public class ActivateReceivedContextHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		if (shell != null) {
			open(shell);
		}
		return null;
	}

	static void open(Shell shell) {
		SelectTaskDialog dialog = new SelectTaskDialog(shell);
		dialog.setInput(CompoundContextActivationContributionItem.tasks);
		if (Window.OK == dialog.open()) {
			ActivateTaskAction action = new CompoundContextActivationContributionItem.ActivateTaskAction();
			action.setTask(dialog.getTask());
			action.run();
		}
	}
}
