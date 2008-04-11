/*******************************************************************************
 * Copyright (c) 2004, 2007 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 ******************************************************************************/
package org.eclipse.ecf.internal.ui.deprecated.views;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.events.*;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.ecf.presence.*;
import org.eclipse.ecf.presence.chatroom.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public class ChatRoomManagerView extends ViewPart implements IChatRoomInvitationListener {

	public static final String VIEW_ID = "org.eclipse.ecf.presence.ui.chatroom.ChatRoomManagerView";

	private static final String COMMAND_PREFIX = "/";

	private static final String COMMAND_DELIM = " ";

	private static final String USERNAME_HOST_DELIMETER = "@";

	private static final int RATIO_WRITE_PANE = 1;

	private static final int RATIO_READ_PANE = 7;

	private static final int RATIO_READ_WRITE_PANE = 85;

	private static final int RATIO_PRESENCE_PANE = 15;

	protected static final String DEFAULT_ME_COLOR = "0,255,0";

	protected static final String DEFAULT_OTHER_COLOR = "0,0,0";

	protected static final String DEFAULT_SYSTEM_COLOR = "0,0,255";

	/**
	 * The default color used to highlight the string of text when the user's
	 * name is referred to in the chatroom. The default color is red.
	 */
	protected static final String DEFAULT_HIGHLIGHT_COLOR = "255,0,0";

	protected static final String DEFAULT_DATE_COLOR = "0,0,0";

	protected static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

	protected static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	protected static final int DEFAULT_INPUT_HEIGHT = 25;

	protected static final int DEFAULT_INPUT_SEPARATOR = 5;

	private CTabFolder rootTabFolder = null;

	private ChatRoomTab rootChannelTab = null;

	private IChatRoomViewCloseListener rootCloseListener = null;

	private IChatRoomMessageSender rootMessageSender = null;

	private IChatRoomManager rootChatRoomManager = null;

	private Color otherColor = null;

	private Color systemColor = null;

	private Color dateColor = null;

	private Color highlightColor = null;

	Action outputClear = null;

	Action outputCopy = null;

	Action outputPaste = null;

	Action outputSelectAll = null;

	boolean rootDisposed = false;

	private ID rootTargetID;

	private String userName = "<user>";

	private String hostName = "<host>";

	private boolean rootEnabled = false;

	private Hashtable chatRooms = new Hashtable();

	class ChatRoomTab {
		private SashForm fullChat;

		private CTabItem tabItem;

		private SashForm rightSash;

		private StyledText outputText;

		private Text inputText;

		private ListViewer listViewer;

		private Action outputSelectAll;
		private Action outputCopy;
		private Action outputClear;

		ChatRoomTab(CTabFolder parent, String name) {
			this(true, parent, name, null);
		}

		ChatRoomTab(boolean withParticipantsList, CTabFolder parent, String name, KeyListener keyListener) {
			tabItem = new CTabItem(parent, SWT.NULL);
			tabItem.setText(name);
			if (withParticipantsList) {
				fullChat = new SashForm(parent, SWT.HORIZONTAL);
				fullChat.setLayout(new FillLayout());
				Composite memberComp = new Composite(fullChat, SWT.NONE);
				memberComp.setLayout(new FillLayout());
				listViewer = new ListViewer(memberComp, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
				listViewer.setSorter(new ViewerSorter());
				Composite rightComp = new Composite(fullChat, SWT.NONE);
				rightComp.setLayout(new FillLayout());
				rightSash = new SashForm(rightComp, SWT.VERTICAL);
			} else
				rightSash = new SashForm(parent, SWT.VERTICAL);
			Composite readInlayComp = new Composite(rightSash, SWT.FILL);
			readInlayComp.setLayout(new GridLayout());
			readInlayComp.setLayoutData(new GridData(GridData.FILL_BOTH));

			outputText = createStyledTextWidget(readInlayComp);
			outputText.setEditable(false);
			outputText.setLayoutData(new GridData(GridData.FILL_BOTH));

			Composite writeComp = new Composite(rightSash, SWT.NONE);
			writeComp.setLayout(new FillLayout());
			inputText = new Text(writeComp, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
			if (keyListener != null)
				inputText.addKeyListener(keyListener);
			rightSash.setWeights(new int[] {RATIO_READ_PANE, RATIO_WRITE_PANE});
			if (withParticipantsList) {
				fullChat.setWeights(new int[] {RATIO_PRESENCE_PANE, RATIO_READ_WRITE_PANE});
				tabItem.setControl(fullChat);
			} else
				tabItem.setControl(rightSash);

			parent.setSelection(tabItem);

			makeActions();
			hookContextMenu();
		}

		private StyledText createStyledTextWidget(Composite parent) {
			try {
				SourceViewer result = new SourceViewer(parent, null, null, true, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
				result.configure(new TextSourceViewerConfiguration(EditorsUI.getPreferenceStore()));
				result.setDocument(new Document());
				return result.getTextWidget();
			} catch (Exception e) {
				Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, IStatus.WARNING, "Source viewer not available.  Hyperlinking will be disabled.", e));
				return new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
			} catch (NoClassDefFoundError e) {
				Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, IStatus.WARNING, "Source viewer not available.  Hyperlinking will be disabled.", e));
				return new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
			}
		}

		protected void outputClear() {
			if (MessageDialog.openConfirm(null, "Confirm Clear Text Output", "Are you sure you want to clear output?")) {
				outputText.setText(""); //$NON-NLS-1$
			}
		}

		protected void outputCopy() {
			String t = outputText.getSelectionText();
			if (t == null || t.length() == 0) {
				outputText.selectAll();
			}
			outputText.copy();
			outputText.setSelection(outputText.getText().length());
		}

		private void fillContextMenu(IMenuManager manager) {
			manager.add(outputCopy);
			manager.add(outputClear);
			manager.add(new Separator());
			manager.add(outputSelectAll);
			manager.add(new Separator("Additions"));
		}

		private void hookContextMenu() {
			MenuManager menuMgr = new MenuManager("#PopupMenu");
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					fillContextMenu(manager);
				}
			});
			Menu menu = menuMgr.createContextMenu(outputText);
			outputText.setMenu(menu);
			ISelectionProvider selectionProvider = new ISelectionProvider() {

				public void addSelectionChangedListener(ISelectionChangedListener listener) {
				}

				public ISelection getSelection() {
					ISelection selection = new TextSelection(outputText.getSelectionRange().x, outputText.getSelectionRange().y);

					return selection;
				}

				public void removeSelectionChangedListener(ISelectionChangedListener listener) {
				}

				public void setSelection(ISelection selection) {
					if (selection instanceof ITextSelection) {
						ITextSelection textSelection = (ITextSelection) selection;
						outputText.setSelection(textSelection.getOffset(), textSelection.getOffset() + textSelection.getLength());
					}
				}

			};
			getSite().registerContextMenu(menuMgr, selectionProvider);
		}

		private void makeActions() {
			outputSelectAll = new Action() {
				public void run() {
					outputText.selectAll();
				}
			};
			outputSelectAll.setText("Select All");
			outputSelectAll.setToolTipText("Select All");
			outputSelectAll.setAccelerator(SWT.CTRL | 'A');
			outputCopy = new Action() {
				public void run() {
					outputCopy();
				}
			};
			outputCopy.setText("Copy");
			outputCopy.setToolTipText("Copy Selected");
			outputCopy.setAccelerator(SWT.CTRL | 'C');
			outputCopy.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
			outputClear = new Action() {
				public void run() {
					outputClear();
				}
			};
			outputClear.setText("Clear");
			outputClear.setToolTipText("Clear output window");
			outputPaste = new Action() {
				public void run() {
					getRootTextInput().paste();
				}
			};

		}

		protected Text getInputText() {
			return inputText;
		}

		protected void setKeyListener(KeyListener listener) {
			if (listener != null)
				inputText.addKeyListener(listener);
		}

		protected ListViewer getListViewer() {
			return listViewer;
		}

		/**
		 * @return the <tt>StyledText</tt> widget that is displaying the output of the chatroom
		 */
		public StyledText getOutputText() {
			return outputText;
		}
	}

	public void createPartControl(Composite parent) {
		otherColor = colorFromRGBString(DEFAULT_OTHER_COLOR);
		systemColor = colorFromRGBString(DEFAULT_SYSTEM_COLOR);
		highlightColor = colorFromRGBString(DEFAULT_HIGHLIGHT_COLOR);
		dateColor = colorFromRGBString(DEFAULT_DATE_COLOR);
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(new FillLayout());
		boolean useTraditionalTabFolder = PlatformUI.getPreferenceStore().getBoolean(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS);
		rootTabFolder = new CTabFolder(rootComposite, SWT.NORMAL | SWT.CLOSE);
		rootTabFolder.setUnselectedCloseVisible(false);
		rootTabFolder.setSimple(useTraditionalTabFolder);
		PlatformUI.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS) && !rootTabFolder.isDisposed()) {
					rootTabFolder.setSimple(((Boolean) event.getNewValue()).booleanValue());
					rootTabFolder.redraw();
				}
			}
		});

		rootTabFolder.addCTabFolder2Listener(new CTabFolder2Listener() {
			public void close(CTabFolderEvent event) {
				event.doit = closeTabItem((CTabItem) event.item);
			}

			public void maximize(CTabFolderEvent event) {
			}

			public void minimize(CTabFolderEvent event) {
			}

			public void restore(CTabFolderEvent event) {
			}

			public void showList(CTabFolderEvent event) {
			}
		});
		rootChannelTab = new ChatRoomTab(false, rootTabFolder, hostName, new KeyListener() {
			public void keyPressed(KeyEvent evt) {
				handleKeyPressed(evt);
			}

			public void keyReleased(KeyEvent evt) {
				handleKeyReleased(evt);
			}
		});
		setEnabled(false);
		makeActions();
		hookContextMenu();
	}

	private boolean closeTabItem(CTabItem tabItem) {
		ChatRoom chatRoom = findChatRoomForTabItem(tabItem);
		if (chatRoom == null) {
			return false;
		} else {
			if (MessageDialog.openQuestion(getSite().getShell(), "Close Chat Room", NLS.bind("Close {0}?", tabItem.getText()))) {
				chatRoom.disconnect();
				return true;
			} else
				return false;
		}
	}

	private ChatRoom findChatRoomForTabItem(CTabItem tabItem) {
		for (Iterator i = chatRooms.values().iterator(); i.hasNext();) {
			ChatRoom cr = (ChatRoom) i.next();
			if (tabItem == cr.chatRoomTab.tabItem)
				return cr;
		}
		return null;
	}

	private Text getRootTextInput() {
		return rootChannelTab.getInputText();
	}

	private StyledText getRootTextOutput() {
		return rootChannelTab.getOutputText();
	}

	public void initialize(final IChatRoomViewCloseListener parent, final IChatRoomContainer chatRoomContainer, final IChatRoomManager chatRoomManager, final ID targetID) {
		Assert.isNotNull(parent);
		Assert.isNotNull(chatRoomContainer);
		Assert.isNotNull(chatRoomManager);
		Assert.isNotNull(targetID);
		ChatRoomManagerView.this.rootChatRoomManager = chatRoomManager;
		ChatRoomManagerView.this.rootCloseListener = parent;
		ChatRoomManagerView.this.rootTargetID = targetID;
		ChatRoomManagerView.this.rootMessageSender = chatRoomContainer.getChatRoomMessageSender();
		setUsernameAndHost(ChatRoomManagerView.this.rootTargetID);
		ChatRoomManagerView.this.setPartName(userName + USERNAME_HOST_DELIMETER + hostName);
		ChatRoomManagerView.this.setTitleToolTip("Host: " + hostName);
		ChatRoomManagerView.this.rootChannelTab.tabItem.setText(hostName);
		if (chatRoomContainer.getConnectedID() == null)
			initializeControls(targetID);
		setEnabled(false);
	}

	private void initializeControls(ID targetID) {
		// clear text output area
		StyledText outputText = getRootTextOutput();
		if (!outputText.isDisposed())
			outputText.setText(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(new Date()) + "\nConnecting to " + targetID.getName() + "\n\n");
	}

	public void setEnabled(boolean enabled) {
		this.rootEnabled = enabled;
		Text inputText = getRootTextInput();
		if (!inputText.isDisposed())
			inputText.setEnabled(enabled);
	}

	public boolean isEnabled() {
		return rootEnabled;
	}

	protected void clearInput() {
		getRootTextInput().setText(""); //$NON-NLS-1$
	}

	protected void handleCommands(String line, String[] tokens) {
		// Look at first one and switch
		String command = tokens[0];
		while (command.startsWith(COMMAND_PREFIX))
			command = command.substring(1);
		String[] args = new String[tokens.length - 1];
		System.arraycopy(tokens, 1, args, 0, tokens.length - 1);
		if (command.equalsIgnoreCase("QUIT")) {
			cleanUp();
		} else if (command.equalsIgnoreCase("JOIN")) {
			String arg1 = args[0];
			String arg2 = "";
			if (args.length > 1) {
				arg2 = args[1];
			}
			doJoinRoom(arg1, arg2);
		} else
			sendMessageLine(line);
	}

	protected void sendMessageLine(String line) {
		try {
			rootMessageSender.sendMessage(line);
		} catch (ECFException e) {
			// And cut ourselves off
			removeLocalUser();
		}
	}

	public void disconnected() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (rootDisposed)
					return;
				setEnabled(false);
				setPartName("(" + getPartName() + ")");
			}
		});
	}

	protected CTabItem getTabItem(String targetName) {
		CTabItem[] items = rootTabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getText().equals(targetName)) {
				return items[i];
			}
		}
		return null;
	}

	protected void doJoinRoom(final String target, final String key) {
		// first, check to see if we already have it open. If so just activate
		ChatRoom room = (ChatRoom) chatRooms.get(target);

		if (room != null && room.isConnected()) {
			room.setSelected();
			return;
		}

		// With manager, first thing we do is get the IChatRoomInfo for the
		// target
		// channel
		IChatRoomInfo roomInfo = rootChatRoomManager.getChatRoomInfo(target);
		// If it's null, we give up
		if (roomInfo == null)
			// no room info for given target...give error message and skip
			return;
		else {
			// Then we create a new chatRoomContainer from the roomInfo
			try {
				final IChatRoomContainer chatRoomContainer = roomInfo.createChatRoomContainer();

				// Setup new user interface (new tab)
				final ChatRoom chatroom = new ChatRoom(chatRoomContainer, new ChatRoomTab(rootTabFolder, target));
				// setup message listener
				chatRoomContainer.addMessageListener(new IIMMessageListener() {
					public void handleMessageEvent(IIMMessageEvent messageEvent) {
						if (messageEvent instanceof IChatRoomMessageEvent) {
							IChatRoomMessage m = ((IChatRoomMessageEvent) messageEvent).getChatRoomMessage();
							chatroom.handleMessage(m.getFromID(), m.getMessage());
						}
					}
				});
				// setup participant listener
				chatRoomContainer.addChatRoomParticipantListener(new IChatRoomParticipantListener() {
					public void handlePresenceUpdated(ID fromID, IPresence presence) {
						chatroom.handlePresence(fromID, presence);
					}

					public void handleArrived(IUser participant) {
					}

					public void handleUpdated(IUser updatedParticipant) {
					}

					public void handleDeparted(IUser participant) {
					}
				});
				chatRoomContainer.addListener(new IContainerListener() {
					public void handleEvent(IContainerEvent evt) {
						if (evt instanceof IContainerDisconnectedEvent || evt instanceof IContainerEjectedEvent) {
							chatroom.disconnected();
						}
					}
				});
				// Now connect/join
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							chatRoomContainer.connect(IDFactory.getDefault().createID(chatRoomContainer.getConnectNamespace(), target), ConnectContextFactory.createPasswordConnectContext(key));
							chatRooms.put(target, chatroom);
						} catch (Exception e) {
							MessageDialog.openError(getSite().getShell(), "Connect Error", NLS.bind("Could connect to {0}.\n\nError is {1}.", target, e.getLocalizedMessage()));
						}
					}
				});
			} catch (Exception e) {
				MessageDialog.openError(getSite().getShell(), "Container Create Error", NLS.bind("Could not create chatRoomContainer for {0}.\n\nError is {1}.", target, e.getLocalizedMessage()));
			}
		}
	}

	class ChatRoom implements IChatRoomInvitationListener, KeyListener {

		private IChatRoomContainer chatRoomContainer;

		private ChatRoomTab chatRoomTab;

		private IChatRoomMessageSender chatRoomMessageSender;

		private IUser localUser;

		private ListViewer chatRoomParticipantViewer = null;

		/**
		 * A list of available nicknames for nickname completion via the 'tab'
		 * key.
		 */
		private ArrayList options;

		/**
		 * Denotes the number of options that should be available for the user
		 * to cycle through when pressing the 'tab' key to perform nickname
		 * completion. The default value is set to 5.
		 */
		private int maximumCyclingOptions = 5;

		/**
		 * The length of a nickname's prefix that has already been typed in by
		 * the user. This is used to remove the beginning part of the available
		 * nickname choices.
		 */
		private int prefixLength;

		/**
		 * The index of the next nickname to select from {@link #options}.
		 */
		private int choice = 0;

		/**
		 * The length of the user's nickname that remains resulting from
		 * subtracting the nickname's length from the prefix that the user has
		 * keyed in already.
		 */
		private int nickRemainder;

		/**
		 * The caret position of when the user first started
		 * cycling through nickname completion options.
		 */
		private int caret;

		/**
		 * The character to enter after the user's nickname has been
		 * autocompleted. The default value is a colon (':').
		 */
		private char nickCompletionSuffix = ':';

		/**
		 * Indicates whether the user is currently cycling over the list of
		 * nicknames for nickname completion.
		 */
		private boolean isCycling = false;

		/**
		 * Check to see whether the user is currently starting the line of text
		 * with a nickname at the beginning of the message. This determines
		 * whether {@link #nickCompletionSuffix} should be inserted when
		 * performing autocompletion. If the user is not at the beginning of the
		 * message, it is likely that the user is typing another user's name to
		 * reference that person and not to direct the message to said person,
		 * as such, the <code>nickCompletionSuffix</code> does not need to be
		 * appeneded.
		 */
		private boolean isAtStart = false;

		private CTabItem itemSelected = null;

		private Text getInputText() {
			return chatRoomTab.getInputText();
		}

		private StyledText getOutputText() {
			return chatRoomTab.getOutputText();
		}

		ChatRoom(IChatRoomContainer container, ChatRoomTab tabItem) {
			Assert.isNotNull(container);
			Assert.isNotNull(tabItem);
			this.chatRoomContainer = container;
			this.chatRoomMessageSender = container.getChatRoomMessageSender();
			this.chatRoomTab = tabItem;
			chatRoomParticipantViewer = this.chatRoomTab.getListViewer();
			options = new ArrayList();
			this.chatRoomTab.setKeyListener(this);

			rootTabFolder.setUnselectedCloseVisible(true);

			rootTabFolder.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					itemSelected = (CTabItem) e.item;
					if (itemSelected == chatRoomTab.tabItem)
						makeTabItemNormal();
				}
			});
		}

		protected void makeTabItemBold() {
			changeTabItem(true);
		}

		protected void makeTabItemNormal() {
			changeTabItem(false);
		}

		protected void changeTabItem(boolean bold) {
			CTabItem item = chatRoomTab.tabItem;
			Font oldFont = item.getFont();
			FontData[] fd = oldFont.getFontData();
			item.setFont(new Font(oldFont.getDevice(), fd[0].getName(), fd[0].getHeight(), (bold) ? SWT.BOLD : SWT.NORMAL));
		}

		public void handleMessage(final ID fromID, final String messageBody) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (rootDisposed)
						return;
					appendText(getOutputText(), new ChatLine(messageBody, new ChatRoomParticipant(fromID)));
					CTabItem item = rootTabFolder.getSelection();
					if (item != chatRoomTab.tabItem)
						makeTabItemBold();
				}
			});
		}

		public void handleInvitationReceived(ID roomID, ID from, String subject, String body) {
			System.out.println("invitation room=" + roomID + ",from=" + from + ",subject=" + subject + ",body=" + body);
		}

		public void keyPressed(KeyEvent e) {
			handleKeyPressed(e);
		}

		public void keyReleased(KeyEvent e) {
			handleKeyReleased(e);
		}

		protected void handleKeyPressed(KeyEvent evt) {
			Text inputText = getInputText();
			if (evt.character == SWT.CR) {
				if (inputText.getText().trim().length() > 0)
					handleTextInput(inputText.getText());
				clearInput();
				evt.doit = false;
				isCycling = false;
			} else if (evt.character == SWT.TAB) {
				// don't propogate the event upwards and insert a tab character
				evt.doit = false;
				int pos = inputText.getCaretPosition();
				// if the user is at the beginning of the line, do nothing
				if (pos == 0)
					return;
				String text = inputText.getText();
				// check to see if the user is currently cycling through the
				// available nicknames
				if (isCycling) {
					// if everything's been cycled over, start over at zero
					if (choice == options.size()) {
						choice = 0;
					}
					// cut of the user's nickname based on what's already
					// entered and at a trailing space
					String append = ((String) options.get(choice++)).substring(prefixLength) + (isAtStart ? nickCompletionSuffix + " " : " ");
					// add what's been typed along with the next nickname option
					// and the rest of the message
					inputText.setText(text.substring(0, caret) + append + text.substring(caret + nickRemainder));
					nickRemainder = append.length();
					// set the caret position to be the place where the nickname
					// completion ended
					inputText.setSelection(caret + nickRemainder, caret + nickRemainder);
				} else {
					// the user is not cycling, so we need to identify what the
					// user has typed based on the current caret position
					int count = pos - 1;
					// keep looping until the whitespace has been identified or
					// the beginning of the message has been reached
					while (count > -1 && !Character.isWhitespace(text.charAt(count))) {
						count--;
					}
					count++;
					// remove all previous options
					options.clear();
					// get the prefix that the user typed
					String prefix = text.substring(count, pos);
					isAtStart = count == 0;
					// if what's found was actually whitespace, do nothing
					if (prefix.trim().equals("")) { //$NON-NLS-1$
						return;
					}
					// get all of the users in this room and store them if they
					// start with the prefix that the user has typed
					String[] participants = chatRoomParticipantViewer.getList().getItems();
					for (int i = 0; i < participants.length; i++) {
						if (participants[i].startsWith(prefix)) {
							options.add(participants[i]);
						}
					}

					// simply return if no matches have been found
					if (options.isEmpty())
						return;

					prefixLength = prefix.length();
					if (options.size() == 1) {
						String nickname = (String) options.get(0);
						// since only one nickname is available, simply insert
						// it after truncating the prefix
						nickname = nickname.substring(prefixLength);
						inputText.insert(nickname + (isAtStart ? nickCompletionSuffix + " " : " "));
					} else if (options.size() <= maximumCyclingOptions) {
						// note that the user is currently cycling through
						// options and also store the current caret position
						isCycling = true;
						caret = pos;
						choice = 0;
						// insert the nickname after removing the prefix
						String nickname = options.get(choice++) + (isAtStart ? nickCompletionSuffix + " " : " ");
						nickname = nickname.substring(prefixLength);
						inputText.insert(nickname);
						// store the length of this truncated nickname so that
						// it can be removed when the user is cycling
						nickRemainder = nickname.length();
					} else {
						// as there are too many choices for the user to pick
						// from, simply display all of the available ones on the
						// chat window so that the user can get a visual
						// indicator of what's available and narrow down the
						// choices by typing a few more additional characters
						StringBuffer choices = new StringBuffer();
						synchronized (choices) {
							for (int i = 0; i < options.size(); i++) {
								choices.append(options.get(i)).append(' ');
							}
							choices.delete(choices.length() - 1, choices.length());
						}
						appendText(getOutputText(), new ChatLine(choices.toString()));
					}
				}
			} else {
				// remove the cycling marking for any other key pressed
				isCycling = false;
			}
		}

		protected void handleKeyReleased(KeyEvent evt) {
			if (evt.character == SWT.TAB) {
				// don't move to the next widget or try to add tabs
				evt.doit = false;
			}
		}

		protected void handleTextInput(String text) {
			if (chatRoomMessageSender == null) {
				MessageDialog.openError(getViewSite().getShell(), "Not connect", "Not connected to channel room");
				return;
			} else
				handleInputLine(text);
		}

		protected void handleInputLine(String line) {
			if ((line != null && line.startsWith(COMMAND_PREFIX))) {
				StringTokenizer st = new StringTokenizer(line, COMMAND_DELIM);
				int countTokens = st.countTokens();
				String toks[] = new String[countTokens];
				for (int i = 0; i < countTokens; i++) {
					toks[i] = st.nextToken();
				}
				String[] tokens = toks;
				handleCommands(line, tokens);
			} else
				sendMessageLine(line);
		}

		protected void handleCommands(String line, String[] tokens) {
			// Look at first one and switch
			String command = tokens[0];
			while (command.startsWith(COMMAND_PREFIX))
				command = command.substring(1);
			String[] args = new String[tokens.length - 1];
			System.arraycopy(tokens, 1, args, 0, tokens.length - 1);
			if (command.equalsIgnoreCase("QUIT")) {
				cleanUp();
			} else if (command.equalsIgnoreCase("PART")) {
				disconnect();
			} else if (command.equalsIgnoreCase("JOIN")) {
				String arg1 = args[0];
				String arg2 = "";
				if (args.length > 1) {
					arg2 = args[1];
				}
				doJoinRoom(arg1, arg2);
			} else {
				sendMessageLine(line);
			}
		}

		protected void disconnect() {
			if (chatRoomContainer != null)
				chatRoomContainer.disconnect();
		}

		protected void clearInput() {
			getInputText().setText(""); //$NON-NLS-1$
		}

		protected void sendMessageLine(String line) {
			try {
				chatRoomMessageSender.sendMessage(line);
			} catch (ECFException e) {
				disconnected();
			}
		}

		public void handlePresence(final ID fromID, final IPresence presence) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (rootDisposed)
						return;
					boolean isAdd = presence.getType().equals(IPresence.Type.AVAILABLE);
					ChatRoomParticipant p = new ChatRoomParticipant(fromID);
					if (isAdd) {
						if (localUser == null)
							localUser = p;
						addParticipant(p);
					} else
						removeParticipant(p);
				}
			});
		}

		public void disconnected() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (rootDisposed)
						return;
					Text inputText = getInputText();
					if (!inputText.isDisposed())
						inputText.setEnabled(false);
				}
			});
		}

		protected boolean isConnected() {
			Text inputText = getInputText();
			return !inputText.isDisposed() && inputText.isEnabled();
		}

		protected void setSelected() {
			rootTabFolder.setSelection(chatRoomTab.tabItem);
		}

		protected void addParticipant(IUser p) {
			if (p != null) {
				ID id = p.getID();
				if (id != null) {
					appendText(getOutputText(), new ChatLine("(" + getDateTime() + ") " + trimUserID(id) + " entered", null));
					chatRoomParticipantViewer.add(p);
				}
			}
		}

		protected boolean isLocalUser(ID id) {
			if (localUser == null)
				return false;
			else if (localUser.getID().equals(id))
				return true;
			else
				return false;
		}

		protected void removeLocalUser() {
			// It's us that's gone away... so we're outta here
			String title = getPartName();
			setPartName("(" + title + ")");
			removeAllParticipants();
			cleanUp();
			setEnabled(false);
		}

		protected void removeParticipant(IUser p) {
			if (p != null) {
				ID id = p.getID();
				if (id != null) {
					appendText(getOutputText(), new ChatLine("(" + getDateTime() + ") " + trimUserID(id) + " left", null));
					chatRoomParticipantViewer.remove(p);
				}
			}
		}

		protected void removeAllParticipants() {
			org.eclipse.swt.widgets.List l = chatRoomParticipantViewer.getList();
			for (int i = 0; i < l.getItemCount(); i++) {
				Object o = chatRoomParticipantViewer.getElementAt(i);
				if (o != null)
					chatRoomParticipantViewer.remove(o);
			}
		}
	}

	protected void handleInputLine(String line) {
		if ((line != null && line.startsWith(COMMAND_PREFIX))) {
			StringTokenizer st = new StringTokenizer(line, COMMAND_DELIM);
			int countTokens = st.countTokens();
			String toks[] = new String[countTokens];
			for (int i = 0; i < countTokens; i++) {
				toks[i] = st.nextToken();
			}
			String[] tokens = toks;
			handleCommands(line, tokens);
		} else
			sendMessageLine(line);
	}

	protected void handleTextInput(String text) {
		if (rootMessageSender == null) {
			MessageDialog.openError(getViewSite().getShell(), "Not connect", "Not connected to chat room");
			return;
		} else
			handleInputLine(text);
	}

	protected void handleEnter() {
		Text inputText = getRootTextInput();
		if (inputText.getText().trim().length() > 0)
			handleTextInput(inputText.getText());
		clearInput();
	}

	protected void handleKeyPressed(KeyEvent evt) {
		if (evt.character == SWT.CR) {
			handleEnter();
			evt.doit = false;
		}
	}

	protected void handleKeyReleased(KeyEvent evt) {
	}

	public void setFocus() {
		getRootTextInput().setFocus();
	}

	protected void setUsernameAndHost(ID chatHostID) {
		URI uri = null;
		try {
			uri = new URI(chatHostID.getName());
			String tmp = uri.getUserInfo();
			if (tmp != null)
				userName = tmp;
			tmp = uri.getHost();
			if (tmp != null)
				hostName = tmp;
		} catch (URISyntaxException e) {
		}
	}

	public void joinRoom(final String room) {
		if (room != null)
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (rootDisposed)
						return;
					doJoinRoom(room, null);
				}
			});
	}

	public void dispose() {
		rootDisposed = true;
		cleanUp();
		super.dispose();
	}

	protected String getMessageString(ID fromID, String text) {
		return fromID.getName() + ": " + text + "\n";
	}

	public void handleMessage(final ID fromID, final String messageBody) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (rootDisposed)
					return;
				appendText(getRootTextOutput(), new ChatLine(messageBody, new ChatRoomParticipant(fromID)));
			}
		});
	}

	private String trimUserID(ID userID) {
		try {
			URI uri = new URI(userID.getName());
			String user = uri.getUserInfo();
			return user == null ? userID.getName() : user;
		} catch (URISyntaxException e) {
			String userAtHost = userID.getName();
			int atIndex = userAtHost.lastIndexOf(USERNAME_HOST_DELIMETER);
			if (atIndex != -1) {
				userAtHost = userAtHost.substring(0, atIndex);
			}
			return userAtHost;
		}
	}

	class ChatRoomParticipant implements IUser {
		private static final long serialVersionUID = 2008114088656711572L;

		ID id;

		public ChatRoomParticipant(ID id) {
			this.id = id;
		}

		public ID getID() {
			return id;
		}

		public String getName() {
			return toString();
		}

		public boolean equals(Object other) {
			if (!(other instanceof ChatRoomParticipant))
				return false;
			ChatRoomParticipant o = (ChatRoomParticipant) other;
			if (id.equals(o.id))
				return true;
			return false;
		}

		public int hashCode() {
			return id.hashCode();
		}

		public String toString() {
			return trimUserID(id);
		}

		public Map getProperties() {
			return null;
		}

		public Object getAdapter(Class adapter) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ecf.core.user.IUser#getNickname()
		 */
		public String getNickname() {
			return getName();
		}
	}

	protected String getCurrentDate(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String res = sdf.format(new Date());
		return res;
	}

	protected String getDateTime() {
		StringBuffer buf = new StringBuffer();
		buf.append(getCurrentDate(DEFAULT_DATE_FORMAT)).append(" ").append(getCurrentDate(DEFAULT_TIME_FORMAT));
		return buf.toString();
	}

	protected void cleanUp() {
		if (rootCloseListener != null) {
			if (rootTargetID == null)
				rootCloseListener.chatRoomViewClosing(null);
			else
				rootCloseListener.chatRoomViewClosing(rootTargetID.getName());
			rootCloseListener = null;
			rootMessageSender = null;
		}
	}

	protected void removeLocalUser() {
		// It's us that's gone away... so we're outta here
		String title = getPartName();
		setPartName("(" + title + ")");
		cleanUp();
		setEnabled(false);
	}

	public void handleInvitationReceived(ID roomID, ID from, String subject, String body) {
		System.out.println("invitation room=" + roomID + ",from=" + from + ",subject=" + subject + ",body=" + body);
	}

	private boolean intelligentAppend(StyledText st, ChatLine text) {
		String line = text.getText();

		int startRange = st.getText().length();
		StringBuffer sb = new StringBuffer();
		// check to see if the message has the user's name contained within
		boolean nickContained = text.getText().indexOf(userName) != -1;
		if (text.getOriginator() != null) {
			// check to make sure that the person referring to the user's name
			// is not the user himself, no highlighting is required in this case
			// as the user is already aware that his name is being referenced
			nickContained = !text.getOriginator().getName().equals(userName) && nickContained;
			sb.append('(').append(getCurrentDate(DEFAULT_TIME_FORMAT)).append(") "); //$NON-NLS-1$
			StyleRange dateStyle = new StyleRange();
			dateStyle.start = startRange;
			dateStyle.length = sb.length();
			dateStyle.foreground = dateColor;
			dateStyle.fontStyle = SWT.NORMAL;
			st.append(sb.toString());
			st.setStyleRange(dateStyle);
			sb = new StringBuffer();
			sb.append(text.getOriginator().getName()).append(": "); //$NON-NLS-1$
			StyleRange sr = new StyleRange();
			sr.start = startRange + dateStyle.length;
			sr.length = sb.length();
			sr.fontStyle = SWT.BOLD;
			// check to see which color should be used
			sr.foreground = nickContained ? highlightColor : otherColor;
			st.append(sb.toString());
			st.setStyleRange(sr);
		}

		if (line != null && !line.equals("")) { //$NON-NLS-1$
			int beforeMessageIndex = st.getText().length();
			st.append(line);
			if (text.getOriginator() == null) {
				StyleRange sr = new StyleRange();
				sr.start = beforeMessageIndex;
				sr.length = line.length();
				sr.foreground = systemColor;
				sr.fontStyle = SWT.BOLD;
				st.setStyleRange(sr);
			} else if (nickContained) {
				// highlight the message itself as necessary
				StyleRange sr = new StyleRange();
				sr.start = beforeMessageIndex;
				sr.length = line.length();
				sr.foreground = highlightColor;
				st.setStyleRange(sr);
			}
		}

		if (!text.isNoCRLF()) {
			st.append("\n"); //$NON-NLS-1$
		}

		String t = st.getText();
		if (t == null)
			return true;
		st.setSelection(t.length());

		return true;
	}

	protected void appendText(StyledText readText, ChatLine text) {
		if (readText == null || text == null) {
			return;
		}
		StyledText st = readText;
		if (st == null || intelligentAppend(st, text)) {
			return;
		}
		int startRange = st.getText().length();
		StringBuffer sb = new StringBuffer();
		// check to see if the message has the user's name contained within
		boolean nickContained = text.getText().indexOf(userName) != -1;
		if (text.getOriginator() != null) {
			// check to make sure that the person referring to the user's name
			// is not the user himself, no highlighting is required in this case
			// as the user is already aware that his name is being referenced
			nickContained = !text.getOriginator().getName().equals(userName) && nickContained;
			sb.append("(").append(getCurrentDate(DEFAULT_TIME_FORMAT)).append(") ");
			StyleRange dateStyle = new StyleRange();
			dateStyle.start = startRange;
			dateStyle.length = sb.length();
			dateStyle.foreground = dateColor;
			dateStyle.fontStyle = SWT.NORMAL;
			st.append(sb.toString());
			st.setStyleRange(dateStyle);
			sb = new StringBuffer();
			sb.append(text.getOriginator().getName()).append(": ");
			StyleRange sr = new StyleRange();
			sr.start = startRange + dateStyle.length;
			sr.length = sb.length();
			sr.fontStyle = SWT.BOLD;
			// check to see which color should be used
			sr.foreground = nickContained ? highlightColor : otherColor;
			st.append(sb.toString());
			st.setStyleRange(sr);
		}
		int beforeMessageIndex = st.getText().length();
		st.append(text.getText());
		if (text.getOriginator() == null) {
			StyleRange sr = new StyleRange();
			sr.start = beforeMessageIndex;
			sr.length = text.getText().length();
			sr.foreground = systemColor;
			sr.fontStyle = SWT.BOLD;
			st.setStyleRange(sr);
		} else if (nickContained) {
			// highlight the message itself as necessary
			StyleRange sr = new StyleRange();
			sr.start = beforeMessageIndex;
			sr.length = text.getText().length();
			sr.foreground = highlightColor;
			st.setStyleRange(sr);
		}
		if (!text.isNoCRLF()) {
			st.append("\n");
		}
		String t = st.getText();
		if (t == null)
			return;
		st.setSelection(t.length());
		// Bold title if view is not visible.
		IWorkbenchSiteProgressService pservice = (IWorkbenchSiteProgressService) this.getSite().getAdapter(IWorkbenchSiteProgressService.class);
		pservice.warnOfContentChange();
	}

	protected void outputClear() {
		if (MessageDialog.openConfirm(null, "Confirm Clear Text Output", "Are you sure you want to clear output?")) {
			getRootTextOutput().setText(""); //$NON-NLS-1$
		}
	}

	protected void outputCopy() {
		StyledText outputText = getRootTextOutput();
		String t = outputText.getSelectionText();
		if (t == null || t.length() == 0) {
			outputText.selectAll();
		}
		outputText.copy();
		outputText.setSelection(outputText.getText().length());
	}

	protected void outputSelectAll() {
		getRootTextOutput().selectAll();
	}

	protected void makeActions() {
		outputSelectAll = new Action() {
			public void run() {
				outputSelectAll();
			}
		};
		outputSelectAll.setText("Select All");
		outputSelectAll.setToolTipText("Select All");
		outputSelectAll.setAccelerator(SWT.CTRL | 'A');
		outputCopy = new Action() {
			public void run() {
				outputCopy();
			}
		};
		outputCopy.setText("Copy");
		outputCopy.setToolTipText("Copy Selected");
		outputCopy.setAccelerator(SWT.CTRL | 'C');
		outputCopy.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		outputClear = new Action() {
			public void run() {
				outputClear();
			}
		};
		outputClear.setText("Clear");
		outputClear.setToolTipText("Clear output window");
		outputPaste = new Action() {
			public void run() {
				getRootTextInput().paste();
			}
		};
		outputPaste.setText("Paste");
		outputPaste.setToolTipText("Paste");
		outputPaste.setAccelerator(SWT.CTRL | 'V');
		outputPaste.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(outputCopy);
		manager.add(outputPaste);
		manager.add(outputClear);
		manager.add(new Separator());
		manager.add(outputSelectAll);
		manager.add(new Separator("Additions"));
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		StyledText outputText = getRootTextOutput();
		Menu menu = menuMgr.createContextMenu(outputText);
		outputText.setMenu(menu);
		ISelectionProvider selectionProvider = new ISelectionProvider() {

			public void addSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public ISelection getSelection() {
				StyledText outputText = getRootTextOutput();
				ISelection selection = new TextSelection(outputText.getSelectionRange().x, outputText.getSelectionRange().y);

				return selection;
			}

			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public void setSelection(ISelection selection) {
				StyledText outputText = getRootTextOutput();
				if (selection instanceof ITextSelection) {
					ITextSelection textSelection = (ITextSelection) selection;
					outputText.setSelection(textSelection.getOffset(), textSelection.getOffset() + textSelection.getLength());
				}
			}

		};
		getSite().registerContextMenu(menuMgr, selectionProvider);
	}

	private Color colorFromRGBString(String rgb) {
		Color color = null;
		if (rgb == null || rgb.equals("")) {
			color = new Color(getViewSite().getShell().getDisplay(), 0, 0, 0);
			return color;
		}
		if (color != null) {
			color.dispose();
		}
		StringTokenizer st = new StringTokenizer(rgb, ",");
		String[] vals = new String[3];
		for (int i = 0; i < 3; i++) {
			vals[i] = st.nextToken();
		}
		color = new Color(getViewSite().getShell().getDisplay(), Integer.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer.parseInt(vals[2]));
		return color;
	}
}