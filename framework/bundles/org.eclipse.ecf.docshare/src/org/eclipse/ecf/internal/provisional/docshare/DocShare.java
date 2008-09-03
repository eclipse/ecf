/****************************************************************************
 * Copyright (c) 2007, 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Mustafa K. Isik - conflict resolution via operational transformations
 *****************************************************************************/

package org.eclipse.ecf.internal.provisional.docshare;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.datashare.AbstractShare;
import org.eclipse.ecf.datashare.IChannelContainerAdapter;
import org.eclipse.ecf.datashare.events.IChannelDisconnectEvent;
import org.eclipse.ecf.internal.docshare.*;
import org.eclipse.ecf.internal.provisional.docshare.cola.ColaSynchronizer;
import org.eclipse.ecf.internal.provisional.docshare.cola.ColaUpdateMessage;
import org.eclipse.ecf.internal.provisional.docshare.messages.*;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.roster.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Represents a document sharing session between two participants.
 */
/**
 * 
 */
public class DocShare extends AbstractShare {

	/**
	 * The ID of the initiator
	 */
	ID initiatorID;
	/**
	 * The ID of the receiver.
	 */
	ID receiverID;
	/**
	 * Our ID
	 */
	ID ourID;
	/**
	 * Text editor
	 */
	ITextEditor editor;

	IRosterManager rosterManager;

	IRosterListener rosterListener = new IRosterListener() {
		public void handleRosterEntryAdd(IRosterEntry entry) {
			// XXX nothing to do
		}

		public void handleRosterEntryRemove(IRosterEntry entry) {
			// XXX nothing to do
		}

		public void handleRosterUpdate(IRoster roster, IRosterItem changedValue) {
			if (changedValue instanceof IRosterEntry) {
				ID changedID = ((IRosterEntry) changedValue).getUser().getID();
				ID oID = null;
				ID otherID = null;
				Shell shell = null;
				synchronized (stateLock) {
					oID = getOurID();
					otherID = getOtherID();
					shell = editor.getSite().getShell();
				}
				if (oID != null && changedID.equals(oID)) {
					localStopShare();
					showStopShareMessage(shell, Messages.DocShare_STOP_SHARED_EDITOR_US);
				} else if (otherID != null && changedID.equals(otherID)) {
					localStopShare();
					showStopShareMessage(shell, Messages.DocShare_STOP_SHARED_EDITOR_REMOTE);
				}
			}
		}

	};

	/**
	 * Content that we have received via start message, before user has
	 * responded to question about whether or not to display in editor. Should
	 * be null at all other times.
	 */
	String startContent = null;
	/**
	 * Object to use as lock for changing connected state of this docshare
	 * instance
	 */
	Object stateLock = new Object();

	/**
	 * Strategy for maintaining consistency among session participants'
	 * documents.
	 */
	// TODO provide for a user-interactive selection mechanism
	SynchronizationStrategy sync;

	/**
	 * The document listener is the listener for changes to the *local* copy of
	 * the IDocument. This listener is responsible for sending document update
	 * messages when notified.
	 */
	IDocumentListener documentListener = new IDocumentListener() {

		public void documentAboutToBeChanged(DocumentEvent event) {
			// nothing to do
		}

		// handling of LOCAL OPERATION application
		public void documentChanged(DocumentEvent event) {
			// If the channel is gone, then no reason to handle this.
			if (getChannel() == null || !Activator.getDefault().isListenerActive()) {
				return;
			}
			// If the listener is not active, ignore input
			if (!Activator.getDefault().isListenerActive()) {
				// The local editor is being updated by a remote peer, so we do
				// not
				// wish to echo this change.
				return;
			}
			Trace.trace(Activator.PLUGIN_ID, NLS.bind("{0}.documentChanged[{1}]", DocShare.this, event)); //$NON-NLS-1$
			UpdateMessage msg = new UpdateMessage(event.getOffset(), event.getLength(), event.getText());
			UpdateMessage colaMsg = sync.registerOutgoingMessage(msg);

			//TODO breaking DocShare & strategy independence, bad design, fix when refactoring for API 
			if (((ColaUpdateMessage) colaMsg).isReplacement()) {
				//this necessitates splitting up the replacement op into two distinct ops, del and ins
				UpdateMessage delMsg = new UpdateMessage(event.getOffset(), event.getLength(), ""); //$NON-NLS-1$
				UpdateMessage colaDelMsg = sync.registerOutgoingMessage(delMsg);
				sendUpdateMsg(colaDelMsg);

				int lengthOfNoDeletion = 0;
				UpdateMessage insMsg = new UpdateMessage(event.getOffset(), lengthOfNoDeletion, event.getText());
				UpdateMessage colaInsMsg = sync.registerOutgoingMessage(insMsg);
				sendUpdateMsg(colaInsMsg);

				Assert.isTrue(((ColaUpdateMessage) colaDelMsg).getRemoteOperationsCount() == ((ColaUpdateMessage) colaInsMsg).getRemoteOperationsCount(), "remote counters diverge - i.e. document has been modified during separation of replacement"); //$NON-NLS-1$
			} else {
				//standard case
				sendUpdateMsg(colaMsg);
			}
		}
	};

	/**
	 * Create a document sharing session instance.
	 * 
	 * @param adapter
	 *            the {@link IChannelContainerAdapter} to use to create this
	 *            document sharing session.
	 * @throws ECFException
	 *             if the channel cannot be created.
	 */
	public DocShare(IChannelContainerAdapter adapter) throws ECFException {
		super(adapter);
	}

	public ID getInitiatorID() {
		return initiatorID;
	}

	public ID getReceiverID() {
		return receiverID;
	}

	public ID getOurID() {
		return ourID;
	}

	public ITextEditor getTextEditor() {
		return this.editor;
	}

	public boolean isSharing() {
		synchronized (stateLock) {
			return (this.editor != null);
		}
	}

	public ID getOtherID() {
		synchronized (stateLock) {
			if (isInitiator())
				return receiverID;
			return initiatorID;
		}
	}

	public boolean isInitiator() {
		synchronized (stateLock) {
			if (ourID == null || initiatorID == null || receiverID == null)
				return false;
			return ourID.equals(initiatorID);
		}
	}

	/**
	 * Start sharing an editor's contents between two participants. This will
	 * send a request to start sharing with the target identified by the
	 * <code>toID</code> parameter. The remote receiver will be displayed a
	 * message dialog, and given the option to start editor sharing, or not.
	 * 
	 * @param our
	 *            the ID associated with the initiator. Must not be
	 *            <code>null</code>.
	 * @param fromName
	 *            a name to present to the receiver. If
	 *            <code>null, our.getName() will be used.
	 * @param toID the ID of the intended receiver.  Must not be <code>null</code>.
	 * @param fileName the file name of the file to be shared (with suffix type extension).  Must not be <code>null</code>.
	 * @param editorPart the text editor currently showing the contents of this editor.  Must not be <code>null</code>.
	 */
	public void startShare(final ID our, final String fromName, final ID toID, final String fileName, final ITextEditor editorPart) {
		Trace.entering(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_ENTERING, DocShare.class, "startShare", new Object[] {our, fromName, toID, fileName, editorPart}); //$NON-NLS-1$
		Assert.isNotNull(our);
		final String fName = (fromName == null) ? our.getName() : fromName;
		Assert.isNotNull(toID);
		Assert.isNotNull(fName);
		Assert.isNotNull(editorPart);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					// Get content from local document
					final String content = editorPart.getDocumentProvider().getDocument(editorPart.getEditorInput()).get();
					// send start message
					send(toID, new StartMessage(our, fName, toID, content, fileName));
					// Set local sharing start (to setup doc listener)
					localStartShare(getLocalRosterManager(), our, our, toID, editorPart);
				} catch (final Exception e) {
					logError(Messages.DocShare_ERROR_STARTING_EDITOR_TITLE, e);
					showErrorToUser(Messages.DocShare_ERROR_STARTING_EDITOR_TITLE, NLS.bind(Messages.DocShare_ERROR_STARTING_EDITOR_MESSAGE, e.getLocalizedMessage()));
				}
			}
		});
		Trace.exiting(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_ENTERING, DocShare.class, "startShare"); //$NON-NLS-1$
	}

	/**
	 * Stop editor sharing. Message only sent if we are currently engaged in an
	 * editor sharing session ({@link #isSharing()} returns <code>true</code>.
	 */
	public void stopShare() {
		Trace.entering(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_ENTERING, this.getClass(), "stopShare"); //$NON-NLS-1$
		if (isSharing()) {
			// send stop message to other
			sendStopMessage();
		}
		localStopShare();
		Trace.exiting(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_EXITING, this.getClass(), "stopShare"); //$NON-NLS-1$
	}

	void send(ID toID, Message message) throws Exception {
		super.sendMessage(toID, message.serialize());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.AbstractShare#handleMessage(org.eclipse.ecf.core.identity.ID,
	 *      byte[])
	 */
	protected void handleMessage(ID fromContainerID, byte[] data) {
		try {
			final Message message = Message.deserialize(data);
			Assert.isNotNull(message);
			if (message instanceof StartMessage) {
				handleStartMessage((StartMessage) message);
			} else if (message instanceof UpdateMessage) {
				handleUpdateMessage((UpdateMessage) message);
			} else if (message instanceof StopMessage) {
				handleStopMessage((StopMessage) message);
			} else {
				throw new InvalidObjectException(NLS.bind(Messages.DocShare_EXCEPTION_INVALID_MESSAGE, message.getClass().getName()));
			}
		} catch (final Exception e) {
			logError(Messages.DocShare_EXCEPTION_HANDLE_MESSAGE, e);
		}
	}

	/**
	 * This method called by the {@link #handleMessage(ID, byte[])} method if
	 * the type of the message received is a start message (sent by remote party
	 * via {@link #startShare(ID, String, ID, String, ITextEditor)}.
	 * 
	 * @param message
	 *            the UpdateMessage received.
	 */
	protected void handleStartMessage(final StartMessage message) {
		final ID senderID = message.getSenderID();
		Assert.isNotNull(senderID);
		final String senderUsername = message.getSenderUsername();
		Assert.isNotNull(senderUsername);
		final ID our = message.getReceiverID();
		Assert.isNotNull(our);
		final String filename = message.getFilename();
		Assert.isNotNull(filename);
		final String documentContent = message.getDocumentContent();
		Assert.isNotNull(documentContent);
		// First synchronize on any state changes by getting stateLock
		synchronized (stateLock) {
			// If we are already sharing, or have non-null start content
			if (isSharing() || startContent != null) {
				sendStopMessage(senderID);
				// And we're done
				return;
			}
			// Otherwise set start content to the message-provided
			// documentContent
			startContent = documentContent;
		}
		// Then open UI and show text editor if appropriate
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					// First, ask user if they want to receive the doc
					if (openReceiverDialog(senderID, senderUsername, filename)) {
						// If so, then we create a new DocShareEditorInput
						final DocShareEditorInput dsei = new DocShareEditorInput(getTempFileStore(senderUsername, filename, startContent), senderUsername, filename);
						// Then open up text editor
						final ITextEditor ep = (ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(dsei, getEditorIdForFileName(filename));
						// Then change our local state
						localStartShare(getLocalRosterManager(), our, senderID, our, ep);
					} else {
						// Send stop message to initiator
						sendStopMessage();
						// Then we stop the local share
						localStopShare();
					}
				} catch (final Exception e) {
					logError(Messages.DocShare_EXCEPTION_RECEIVING_MESSAGE_TITLE, e);
					showErrorToUser(Messages.DocShare_EXCEPTION_RECEIVING_MESSAGE_TITLE, NLS.bind(Messages.DocShare_EXCEPTION_RECEIVING_MESSAGE_MESSAGE, e.getLocalizedMessage()));
				}
			}
		});
	}

	IRosterManager getLocalRosterManager() {
		IContainer container = (IContainer) this.adapter.getAdapter(IContainer.class);
		if (container != null) {
			IPresenceContainerAdapter presenceContainerAdapter = (IPresenceContainerAdapter) container.getAdapter(IPresenceContainerAdapter.class);
			if (presenceContainerAdapter != null) {
				return presenceContainerAdapter.getRosterManager();
			}
		}
		return null;
	}

	void modifyStartContent(int offset, int length, String text) {
		final StringBuffer result = new StringBuffer(startContent.substring(0, offset));
		result.append(text);
		result.append(startContent.substring(offset + length));
		startContent = result.toString();
	}

	/**
	 * This method called by the {@link #handleMessage(ID, byte[])} method if
	 * the type of the message received is an update message.
	 * 
	 * @param remoteMsg
	 *            the UpdateMessage received.
	 */
	protected void handleUpdateMessage(final UpdateMessage remoteMsg) {
		synchronized (stateLock) {
			// If we're waiting on user to start then change the
			// startContent
			// directly
			if (startContent != null) {
				modifyStartContent(remoteMsg.getOffset(), remoteMsg.getLengthOfReplacedText(), remoteMsg.getText());
				// And we're done
				return;
			}
		}
		// Else replace in document directly
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					Trace.entering(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_ENTERING, this.getClass(), "handleUpdateMessage", remoteMsg); //$NON-NLS-1$
					final IDocument document = getDocumentFromEditor();

					if (document != null) {
						// transparent concurrency/sync'ing algorithm delegation
						// The idea here is to be transparent with the sync'ing
						// strategy.
						Trace.trace(Activator.PLUGIN_ID, NLS.bind("{0}.handleUpdateMessage calling transformIncomingMessage", DocShare.this)); //$NON-NLS-1$
						List messagesForLocalApplication = sync.transformIncomingMessage(remoteMsg);

						// if (localState.equalsIgnoreCase(remoteState)) {
						// We setup editor to not take input while we are
						// changing document
						setEditorToRefuseInput();
						UpdateMessage currentMsg;
						for (Iterator it = messagesForLocalApplication.iterator(); it.hasNext();) {
							currentMsg = (UpdateMessage) it.next();
							document.replace(currentMsg.getOffset(), currentMsg.getLengthOfReplacedText(), currentMsg.getText());
						}
					}
				} catch (final Exception e) {
					logError(Messages.DocShare_EXCEPTION_RECEIVING_MESSAGE_TITLE, e);
					showErrorToUser(Messages.DocShare_EXCEPTION_RECEIVING_MESSAGE_TITLE, NLS.bind(Messages.DocShare_EXCEPTION_RECEIVING_MESSAGE_MESSAGE, e.getLocalizedMessage()));
				} finally {
					// Have editor accept input
					setEditorToAcceptInput();
					Trace.exiting(Activator.PLUGIN_ID, DocshareDebugOptions.METHODS_EXITING, this.getClass(), "handleUpdateMessage"); //$NON-NLS-1$
				}
			}
		});
	}

	/**
	 * @param message
	 */
	protected void handleStopMessage(StopMessage message) {
		if (isSharing()) {
			localStopShare();
		}
	}

	void setEditorToRefuseInput() {
		setEditorEditable(false);
		Activator.getDefault().setListenerActive(false);
	}

	void setEditorToAcceptInput() {
		setEditorEditable(true);
		Activator.getDefault().setListenerActive(true);
	}

	IEditorInput getEditorInput() {
		synchronized (stateLock) {
			if (editor == null)
				return null;
			return editor.getEditorInput();
		}
	}

	boolean openReceiverDialog(ID fromID, String fromUsername, String fileName) {
		return MessageDialog.openQuestion(null, Messages.DocShare_EDITOR_SHARE_POPUP_TITLE, NLS.bind(Messages.DocShare_EDITOR_SHARE_POPUP_MESSAGE, fromUsername, fileName));
	}

	protected void handleDisconnectEvent(IChannelDisconnectEvent cde) {
		boolean weDisconnected = (ourID != null && ourID.equals(cde.getTargetID()));
		Shell shell = null;
		if (isSharing()) {
			shell = editor.getSite().getShell();
		}
		// Stop things and *then* notify user
		localStopShare();
		if (shell != null) {
			if (weDisconnected)
				showStopShareMessage(shell, Messages.DocShare_STOP_SHARED_EDITOR_US);
			else
				showStopShareMessage(shell, Messages.DocShare_STOP_SHARED_EDITOR_REMOTE);
		}
	}

	/**
	 * @param shell must not be <code>null</code>
	 * @param message message content for message dialog
	 */
	void showStopShareMessage(final Shell shell, final String message) {
		Display display = shell.getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(shell, Messages.DocShare_STOP_SHARED_EDITOR_TITLE, message);
			}
		});
	}

	IFileStore getTempFileStore(String fromUsername, String fileName, String content) throws IOException, CoreException {
		final IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(File.createTempFile(fromUsername, fileName));
		final OutputStream outs = fileStore.openOutputStream(EFS.OVERWRITE, null);
		outs.write(content.getBytes());
		outs.close();
		return fileStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.datashare.AbstractShare#dispose()
	 */
	public synchronized void dispose() {
		localStopShare();
		super.dispose();
	}

	void logError(IStatus status) {
		Activator.getDefault().getLog().log(status);
	}

	void showErrorToUser(String title, String message) {
		MessageDialog.openError(null, title, message);
	}

	void logError(String exceptionString, Throwable e) {
		Trace.catching(Activator.PLUGIN_ID, DocshareDebugOptions.EXCEPTIONS_CATCHING, this.getClass(), exceptionString, e);
		Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.ERROR, exceptionString, e));
	}

	StyledText getTextControl() {
		synchronized (stateLock) {
			if (editor == null)
				return null;
			return (StyledText) editor.getAdapter(Control.class);
		}
	}

	void setEditorEditable(final boolean editable) {
		final StyledText textControl = getTextControl();
		if (textControl != null && !textControl.isDisposed()) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					textControl.setEditable(editable);
				}
			});
		}
	}

	String getEditorIdForFileName(String fileName) {
		final IWorkbench wb = PlatformUI.getWorkbench();
		final IEditorRegistry er = wb.getEditorRegistry();
		final IEditorDescriptor desc = er.getDefaultEditor(fileName);
		if (desc != null)
			return desc.getId();
		return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
	}

	IDocument getDocumentFromEditor() {
		synchronized (stateLock) {
			if (editor == null)
				return null;
			final IDocumentProvider documentProvider = editor.getDocumentProvider();
			if (documentProvider == null)
				return null;
			return documentProvider.getDocument(editor.getEditorInput());
		}
	}

	void localStartShare(final IRosterManager rm, ID our, ID initiator, ID receiver, ITextEditor edt) {
		synchronized (stateLock) {
			localStopShare();
			this.rosterManager = rm;
			if (this.rosterManager != null) {
				this.rosterManager.addRosterListener(rosterListener);
			}
			this.ourID = our;
			this.initiatorID = initiator;
			this.receiverID = receiver;
			this.editor = edt;
			final IDocument doc = getDocumentFromEditor();
			if (doc != null)
				doc.addDocumentListener(documentListener);
		}
		// used to have the ColaSynchronizer.getInstanceFor(...) call here ...
		// TODO needs to be moved to a more appropriate spot, where ColaSynch'er
		// does not blow up
		// sync = IdentityMapping.getInstance();
		sync = ColaSynchronizer.getInstanceFor(this);
	}

	void localStopShare() {
		synchronized (stateLock) {
			if (rosterManager != null)
				rosterManager.removeRosterListener(rosterListener);
			this.rosterManager = null;
			this.ourID = null;
			this.initiatorID = null;
			this.receiverID = null;
			this.startContent = null;
			final IDocument doc = getDocumentFromEditor();
			if (doc != null)
				doc.removeDocumentListener(documentListener);
			this.editor = null;
		}
		// clean up if necessary
		// TODO abstract this to work for SynchronizationStrategy
		ColaSynchronizer.cleanUpFor(this);
		sync = null;
	}

	void sendUpdateMsg(UpdateMessage msg) {
		if (isSharing()) {
			try {
				send(getOtherID(), msg);
			} catch (final Exception e) {
				logError(Messages.DocShare_EXCEPTION_SEND_MESSAGE, e);
			}
		}
	}

	void sendStopMessage() {
		sendStopMessage(getOtherID());
	}

	void sendStopMessage(ID other) {
		if (isSharing()) {
			try {
				send(other, new StopMessage());
			} catch (final Exception e) {
				logError(Messages.DocShare_EXCEPTION_SEND_MESSAGE, e);
			}
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer("DocShare["); //$NON-NLS-1$
		buf.append("ourID=").append(ourID).append(";initiatorID=").append(initiatorID).append(";receiverID=").append(receiverID); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append(";strategy=").append(sync).append("]"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

}
