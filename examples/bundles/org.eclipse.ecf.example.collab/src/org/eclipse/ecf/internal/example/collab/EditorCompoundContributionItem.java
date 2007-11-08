package org.eclipse.ecf.internal.example.collab;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ecf.example.collab.share.EclipseCollabSharedObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public class EditorCompoundContributionItem extends CompoundContributionItem {

	public EditorCompoundContributionItem() {
	}

	public EditorCompoundContributionItem(String id) {
		super(id);
	}

	protected IFile getFileForPart(ITextEditor editor) {
		final IEditorInput input = editor.getEditorInput();
		if (input instanceof FileEditorInput) {
			final FileEditorInput fei = (FileEditorInput) input;
			return fei.getFile();
		}
		return null;
	}

	protected IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	protected ClientEntry isConnected(IResource res) {
		if (res == null)
			return null;
		final CollabClient client = CollabClient.getDefault();
		final ClientEntry entry = client.isConnected(res, CollabClient.GENERIC_CONTAINER_CLIENT_NAME);
		return entry;
	}

	protected IContributionItem[] getContributionItems() {
		final ITextEditor editor = (ITextEditor) getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (editor == null)
			return null;
		final ISelection s = editor.getSelectionProvider().getSelection();
		final ITextSelection textSelection = (s instanceof ITextSelection) ? ((ITextSelection) s) : null;
		if (textSelection == null)
			return null;
		final IFile file = getFileForPart(editor);
		if (file == null)
			return null;
		final IProject project = file.getProject();
		if (isConnected(project.getWorkspace().getRoot()) == null)
			return null;

		final IAction action = new Action() {
			public void run() {
				final ClientEntry entry = isConnected(project.getWorkspace().getRoot());
				if (entry == null) {
					MessageDialog.openInformation(getWorkbench().getDisplay().getActiveShell(), Messages.EditorCompoundContributionItem_EXCEPTION_NOT_CONNECTED_TITLE, Messages.EditorCompoundContributionItem_EXCEPTION_NOT_CONNECTED_MESSAGE);
					return;
				}
				final EclipseCollabSharedObject collabsharedobject = entry.getSharedObject();
				if (collabsharedobject != null) {
					collabsharedobject.sendOpenAndSelectForFile(null, project.getName() + "/" + file.getProjectRelativePath().toString(), textSelection.getOffset(), textSelection.getLength()); //$NON-NLS-1$
				}
			}
		};

		action.setText(Messages.EditorCompoundContributionItem_SHARE_SELECTION_MENU_ITEM_NAME);
		//action.setAccelerator(SWT.CTRL | SWT.SHIFT | '1');
		return new IContributionItem[] {new Separator(), new ActionContributionItem(action)};
	}
}
