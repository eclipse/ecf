package org.eclipse.ecf.internal.provider.xmpp.ui;

import java.io.File;

import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.filetransfer.IFileTransferListener;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransferContainerAdapter;
import org.eclipse.ecf.filetransfer.OutgoingFileTransferException;
import org.eclipse.ecf.filetransfer.events.IFileTransferEvent;
import org.eclipse.ecf.filetransfer.events.IOutgoingFileTransferResponseEvent;
import org.eclipse.ecf.internal.provider.xmpp.XMPPContainer;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.roster.IRosterEntry;
import org.eclipse.ecf.presence.ui.roster.AbstractRosterEntryContributionItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class XMPPCompoundContributionItem extends
		AbstractRosterEntryContributionItem {

	private static final IContributionItem[] EMPTY_ARRAY = new IContributionItem[0];

	private Shell shell;

	protected IContributionItem[] getContributionItems() {
		Object selection = getSelection();
		if (!(selection instanceof IRosterEntry)) {
			return EMPTY_ARRAY;
		}
		final IRosterEntry entry = (IRosterEntry) selection;
		IContainer container = getContainerForRosterEntry(entry);
		if (container instanceof XMPPContainer) {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();
			IContributionItem[] contributions = new IContributionItem[2];
			final IOutgoingFileTransferContainerAdapter ioftca = (IOutgoingFileTransferContainerAdapter) container
					.getAdapter(IOutgoingFileTransferContainerAdapter.class);
			IPresence presence = entry.getPresence();
			boolean type = (presence == null) ? false : presence.getType()
					.equals(IPresence.Type.AVAILABLE);
			boolean mode = (presence == null) ? false : presence.getMode()
					.equals(IPresence.Mode.AVAILABLE);
			if (!(ioftca != null && type && mode)) {
				return EMPTY_ARRAY;
			}
			IAction fileSendAction = new Action() {
				public void run() {
					sendFileToTarget(ioftca, entry.getUser().getID());
				}
			};
			fileSendAction.setText(Messages.getString("XMPPCompoundContributionItem.SEND_FILE")); //$NON-NLS-1$
			fileSendAction.setImageDescriptor(PlatformUI.getWorkbench()
					.getSharedImages().getImageDescriptor(
							ISharedImages.IMG_OBJ_FILE));
			contributions[0] = new ActionContributionItem(fileSendAction);
			contributions[1] = new Separator();
			return contributions;
		} else {
			return EMPTY_ARRAY;
		}
	}

	private void sendFileToTarget(
			IOutgoingFileTransferContainerAdapter fileTransfer,
			final ID targetID) {
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		// XXX this should be some default path set by preferences
		fd.setFilterPath(System.getProperty("user.home")); //$NON-NLS-1$
		fd.setText(NLS.bind(Messages.getString("XMPPCompoundContributionItem.CHOOSE_FILE"), targetID //$NON-NLS-1$
				.getName()));
		final String res = fd.open();
		if (res != null) {
			File aFile = new File(res);
			try {
				fileTransfer.sendOutgoingRequest(targetID, aFile,
						new IFileTransferListener() {
							public void handleTransferEvent(
									final IFileTransferEvent event) {
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										// XXX This should be handled more
										// gracefully/with better UI (progress
										// bar?)
										if (event instanceof IOutgoingFileTransferResponseEvent) {
											if (!((IOutgoingFileTransferResponseEvent) event)
													.requestAccepted())
												MessageDialog
														.openInformation(
																shell,
																Messages.getString("XMPPCompoundContributionItem.FILE_SEND_REFUSED_TITLE"), //$NON-NLS-1$
																NLS
																		.bind(
																				Messages.getString("XMPPCompoundContributionItem.FILE_SEND_REFUSED_MESSAGE"), //$NON-NLS-1$
																				res,
																				targetID
																						.getName()));
										}
									}
								});
							}
						}, null);
			} catch (OutgoingFileTransferException e) {
				MessageDialog
						.openError(
								shell,
								Messages.getString("XMPPCompoundContributionItem.SEND_ERROR_TITLE"), //$NON-NLS-1$
								NLS
										.bind(
												Messages.getString("XMPPCompoundContributionItem.SEND_ERROR_MESSAGE"), //$NON-NLS-1$
												new Object[] { res,
														e.getLocalizedMessage() }));
			}
		}
	}

}
