/****************************************************************************
 * Copyright (c) 2007 Remy Suen, Composent, Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.presence.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.presence.ui.Messages;
import org.eclipse.ecf.presence.im.IChatID;
import org.eclipse.ecf.presence.im.IChatMessage;
import org.eclipse.ecf.presence.im.IChatMessageSender;
import org.eclipse.ecf.presence.im.ITypingMessageEvent;
import org.eclipse.ecf.presence.im.ITypingMessageSender;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public class MessagesView extends ViewPart {

	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(
			"(hh:mm:ss a)"); //$NON-NLS-1$

	public static final String VIEW_ID = "org.eclipse.ecf.presence.ui.MessagesView"; //$NON-NLS-1$

	private static final int[] WEIGHTS = { 75, 25 };

	private CTabFolder tabFolder;

	private Color redColor;

	private Color blueColor;

	private Map tabs;

	private boolean showTimestamps = true;

	private static final String getUserName(ID id) {
		IChatID chatID = (IChatID) id.getAdapter(IChatID.class);
		return chatID == null ? id.getName() : chatID.getUsername();
	}

	public MessagesView() {
		tabs = new HashMap();
	}

	public void createPartControl(Composite parent) {
		tabFolder = new CTabFolder(parent, SWT.CLOSE);
		tabFolder.setTabPosition(SWT.BOTTOM);

		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Iterator it = tabs.values().iterator();
				while (it.hasNext()) {
					ChatTab tab = (ChatTab) it.next();
					if (tab.item == e.item) {
						tab.inputText.setFocus();
						break;
					}
				}
			}
		});

		tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent e) {
				Iterator it = tabs.keySet().iterator();
				while (it.hasNext()) {
					Object key = it.next();
					ChatTab tab = (ChatTab) tabs.get(key);
					if (tab.item == e.item) {
						tabs.remove(key);
						break;
					}
				}
			}
		});

		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		IAction timestampAction = new Action(
				Messages.MessagesView_ShowTimestamps, IAction.AS_CHECK_BOX) {
			public void run() {
				showTimestamps = !showTimestamps;
			}
		};
		timestampAction.setChecked(true);
		IAction clearChatLogAction = new Action(
				Messages.MessagesView_ClearChatLog) {
			public void run() {
				CTabItem item = tabFolder.getSelection();
				if (item != null) {
					Iterator iterator = tabs.values().iterator();
					while (iterator.hasNext()) {
						ChatTab tab = (ChatTab) iterator.next();
						if (tab.item == item) {
							if (MessageDialog
									.openConfirm(
											tabFolder.getShell(),
											Messages.MessagesView_ClearChatLogDialogTitle,
											NLS
													.bind(
															Messages.MessagesView_ClearChatLogDialogMessage,
															getUserName(tab.remoteID)))) {
								synchronized (tab) {
									tab.chatText.setText(""); //$NON-NLS-1$
								}
							}
							return;
						}
					}
				}
			}
		};
		manager.add(clearChatLogAction);
		manager.add(timestampAction);

		redColor = new Color(parent.getDisplay(), 255, 0, 0);
		blueColor = new Color(parent.getDisplay(), 0, 0, 255);
	}

	public void dispose() {
		redColor.dispose();
		blueColor.dispose();
		super.dispose();
	}

	private ChatTab getTab(IChatMessageSender messageSender,
			ITypingMessageSender typingSender, ID localID, ID userID) {
		ChatTab tab = (ChatTab) tabs.get(userID);
		if (tab == null) {
			tab = new ChatTab(messageSender, typingSender, localID, userID);
			tabs.put(userID, tab);
		}
		return tab;
	}

	/**
	 * Display a message to notify the current user that a typing event has
	 * occurred.
	 * 
	 * @param event
	 *            the typing message event
	 */
	public void displayTypingNotification(ITypingMessageEvent event) {
		ChatTab tab = null;
		synchronized (tabs) {
			tab = (ChatTab) tabs.get(event.getFromID());
		}
		if (tab != null) {
			tab.showIsTyping(event.getTypingMessage().isTyping());
		}
	}

	/**
	 * Opens a new tab for conversing with a user.
	 * 
	 * @param messageSender
	 *            the <tt>IChatMessageSender</tt> interface that can be used
	 *            to send messages to the other user
	 * @param typingSender
	 *            the <tt>ITypingMessageSender</tt> interface to notify the
	 *            other user that the current user is typing a message,
	 *            <tt>null</tt> if unsupported
	 * @param localID
	 *            the ID of the local user
	 * @param remoteID
	 *            the ID of the remote user
	 */
	public synchronized void openTab(IChatMessageSender messageSender,
			ITypingMessageSender typingSender, ID localID, ID remoteID) {
		Assert.isNotNull(messageSender);
		Assert.isNotNull(localID);
		Assert.isNotNull(remoteID);
		ChatTab tab = getTab(messageSender, typingSender, localID, remoteID);
		// if there is only one tab, select this tab
		if (tabs.size() == 1) {
			tabFolder.setSelection(tab.item);
		}
	}

	public synchronized void selectTab(IChatMessageSender messageSender,
			ITypingMessageSender typingSender, ID localID, ID userID) {
		ChatTab tab = getTab(messageSender, typingSender, localID, userID);
		tabFolder.setSelection(tab.item);
		tab.inputText.setFocus();
	}

	/**
	 * Display a chat message from a remote user in their designated chat box.
	 * 
	 * @param message
	 *            a chat message that has been sent to the local user
	 */
	public synchronized void showMessage(IChatMessage message) {
		Assert.isNotNull(message);
		ID remoteID = message.getFromID();
		ChatTab tab = (ChatTab) tabs.get(remoteID);
		if (tab != null) {
			tab.append(remoteID, message.getBody());
		}
	}

	public void setFocus() {
		CTabItem item = tabFolder.getSelection();
		if (item != null) {
			for (Iterator it = tabs.values().iterator(); it.hasNext();) {
				ChatTab tab = (ChatTab) it.next();
				if (tab.item == item) {
					tab.inputText.setFocus();
					break;
				}
			}
		}
	}

	private class ChatTab {

		private CTabItem item;

		private StyledText chatText;

		private Text inputText;

		private Label notificationLabel;

		private IChatMessageSender icms;

		private ITypingMessageSender itms;

		private ID localID;
		private ID remoteID;

		private boolean sendTyping = false;

		private boolean isFirstMessage = true;

		private ChatTab(IChatMessageSender icms, ITypingMessageSender itms,
				ID localID, ID remoteID) {
			this.icms = icms;
			this.itms = itms;
			this.localID = localID;
			this.remoteID = remoteID;
			constructWidgets();
			addListeners();
		}

		private void addListeners() {
			inputText.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					switch (e.keyCode) {
					case SWT.CR:
					case SWT.KEYPAD_CR:
						if (e.stateMask == 0) {
							String text = inputText.getText();
							inputText.setText(""); //$NON-NLS-1$
							try {
								if (!text.equals("")) { //$NON-NLS-1$
									icms.sendChatMessage(remoteID, text);
								}
								append(localID, text);
							} catch (ECFException ex) {
								notificationLabel
										.setText(Messages.MessagesView_CouldNotSendMessage);
							}
							e.doit = false;
							sendTyping = false;
						}
						break;
					}
				}
			});

			inputText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (!sendTyping && itms != null) {
						sendTyping = true;
						try {
							itms.sendTypingMessage(remoteID, true, null);
						} catch (ECFException ex) {
							// ignored since this is not really that important
							return;
						}
					}
				}
			});
		}

		private void append(ID fromID, String body) {
			if (!isFirstMessage) {
				chatText.append(Text.DELIMITER);
			}
			int length = chatText.getCharCount();
			String name = getUserName(fromID);
			if (fromID.equals(remoteID)) {
				if (showTimestamps) {
					chatText.append(FORMATTER.format(new Date(System
							.currentTimeMillis())) + ' ');
					chatText.setStyleRange(new StyleRange(length, 13, redColor,
							null));
					length = chatText.getCharCount();
				}
				chatText.append(name + ": " + body); //$NON-NLS-1$
				chatText.setStyleRange(new StyleRange(length,
						name.length() + 1, redColor, null, SWT.BOLD));
				notificationLabel.setText(""); //$NON-NLS-1$
				if (isFirstMessage) {
					final MessageNotificationPopup popup = new MessageNotificationPopup(
							getSite().getWorkbenchWindow(), tabFolder
									.getShell(), remoteID);
					popup.setContent(name, body);
					popup.open();

					new UIJob(tabFolder.getDisplay(), "Close Popup Job") { //$NON-NLS-1$
						public IStatus runInUIThread(IProgressMonitor monitor) {
							Shell shell = popup.getShell();
							if (shell != null && !shell.isDisposed()) {
								popup.close();
							}
							return Status.OK_STATUS;
						}
					}.schedule(5000);
				}
			} else {
				if (showTimestamps) {
					chatText.append(FORMATTER.format(new Date(System
							.currentTimeMillis())) + ' ');
					chatText.setStyleRange(new StyleRange(length, 13,
							blueColor, null));
					length = chatText.getCharCount();
				}
				chatText.append(name + ": " + body); //$NON-NLS-1$
				chatText.setStyleRange(new StyleRange(length,
						name.length() + 1, blueColor, null, SWT.BOLD));
			}
			isFirstMessage = false;
			chatText.invokeAction(ST.PAGE_DOWN);
		}

		private void constructWidgets() {
			item = new CTabItem(tabFolder, SWT.NONE);
			Composite parent = new Composite(tabFolder, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			parent.setLayout(layout);

			notificationLabel = new Label(parent, SWT.BEGINNING);
			notificationLabel.setLayoutData(new GridData(SWT.FILL,
					SWT.BEGINNING, true, false));

			SashForm sash = new SashForm(parent, SWT.VERTICAL);
			sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			SourceViewer result = new SourceViewer(sash, null, null, true,
					SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI
							| SWT.READ_ONLY);
			result.configure(new TextSourceViewerConfiguration(EditorsUI
					.getPreferenceStore()));
			result.setDocument(new Document());

			chatText = result.getTextWidget();

			inputText = new Text(sash, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);

			sash.setWeights(WEIGHTS);

			Menu menu = new Menu(chatText);
			MenuItem mi = new MenuItem(menu, SWT.PUSH);
			mi.setText(Messages.MessagesView_Copy);
			mi.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(
					org.eclipse.ui.ISharedImages.IMG_TOOL_COPY));
			mi.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					String text = chatText.getSelectionText();
					if (!text.equals("")) { //$NON-NLS-1$
						chatText.copy();
					}
				}
			});
			mi = new MenuItem(menu, SWT.PUSH);
			mi.setText(Messages.MessagesView_SelectAll);
			mi.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					chatText.selectAll();
				}
			});
			chatText.setMenu(menu);

			item.setControl(parent);
			item.setText(getUserName(remoteID));
		}

		private void showIsTyping(boolean isTyping) {
			notificationLabel.setText(isTyping ? NLS.bind(
					Messages.MessagesView_TypingNotification,
					getUserName(remoteID)) : ""); //$NON-NLS-1$
		}
	}

}
