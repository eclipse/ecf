package org.eclipse.ecf.ui.views;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.eclipse.ecf.core.IContainerListener;
import org.eclipse.ecf.core.events.IContainerDisconnectedEvent;
import org.eclipse.ecf.core.events.IContainerEvent;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.security.ConnectContextFactory;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.presence.IInvitationListener;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.IParticipantListener;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.chat.IChatMessageSender;
import org.eclipse.ecf.presence.chat.IChatParticipantListener;
import org.eclipse.ecf.presence.chat.IChatRoomContainer;
import org.eclipse.ecf.presence.chat.IChatRoomManager;
import org.eclipse.ecf.presence.chat.IRoomInfo;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public class ChatRoomManagerView extends ViewPart implements IMessageListener,
		IInvitationListener {
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

	protected static final String DEFAULT_DATE_COLOR = "0,0,0";

	protected static final String VIEW_PREFIX = "IRC: ";

	protected static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

	protected static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	protected static final int DEFAULT_INPUT_HEIGHT = 25;

	protected static final int DEFAULT_INPUT_SEPARATOR = 5;

	private Composite mainComp = null;

	private SimpleLinkTextViewer readText = null;

	private Text writeText = null;

	private CTabFolder tabFolder = null;

	private Manager rootChatRoomTabItem = null;

	IChatRoomViewCloseListener closeListener = null;

	IChatMessageSender messageSender = null;

	IChatRoomContainer chatRoomContainer = null;

	IChatRoomManager chatRoomManager = null;

	private Color otherColor = null;

	private Color systemColor = null;

	private Color dateColor = null;

	Action outputClear = null;

	Action outputCopy = null;

	Action outputPaste = null;

	Action outputSelectAll = null;

	boolean disposed = false;

	private ID chatHostID;

	private String userName = "<user>";

	private String hostName = "<host>";

	class Manager {
		SashForm fullChat;

		CTabItem tabItem;

		SashForm rightSash;

		KeyListener keyListener;

		SimpleLinkTextViewer textOutput;

		Text textInput;

		ListViewer listViewer;

		Manager(CTabFolder parent, String name) {
			this(true, parent, name, null);
		}

		Manager(boolean withParticipantsList, CTabFolder parent, String name,
				KeyListener keyListener) {
			tabItem = new CTabItem(parent, SWT.NULL);
			tabItem.setText(name);
			if (withParticipantsList) {
				fullChat = new SashForm(parent, SWT.HORIZONTAL);
				fullChat.setLayout(new FillLayout());
				Composite memberComp = new Composite(fullChat, SWT.NONE);
				memberComp.setLayout(new FillLayout());
				listViewer = new ListViewer(memberComp, SWT.BORDER
						| SWT.V_SCROLL | SWT.H_SCROLL);
				listViewer.setSorter(new ViewerSorter());
				Composite rightComp = new Composite(fullChat, SWT.NONE);
				rightComp.setLayout(new FillLayout());
				rightSash = new SashForm(rightComp, SWT.VERTICAL);
			} else
				rightSash = new SashForm(parent, SWT.VERTICAL);
			Composite readInlayComp = new Composite(rightSash, SWT.FILL);
			readInlayComp.setLayout(new GridLayout());
			readInlayComp.setLayoutData(new GridData(GridData.FILL_BOTH));
			textOutput = new SimpleLinkTextViewer(readInlayComp, SWT.V_SCROLL
					| SWT.H_SCROLL | SWT.WRAP);
			textOutput.getTextWidget().setEditable(false);
			textOutput.getTextWidget().setLayoutData(
					new GridData(GridData.FILL_BOTH));
			Composite writeComp = new Composite(rightSash, SWT.NONE);
			writeComp.setLayout(new FillLayout());
			textInput = new Text(writeComp, SWT.BORDER | SWT.MULTI | SWT.WRAP
					| SWT.V_SCROLL);
			if (keyListener != null)
				textInput.addKeyListener(keyListener);
			rightSash
					.setWeights(new int[] { RATIO_READ_PANE, RATIO_WRITE_PANE });
			if (withParticipantsList) {
				fullChat.setWeights(new int[] { RATIO_PRESENCE_PANE,
						RATIO_READ_WRITE_PANE });
				tabItem.setControl(fullChat);
			} else
				tabItem.setControl(rightSash);
			parent.setSelection(tabItem);
		}

		protected void setTabName(String name) {
			tabItem.setText(name);
		}

		protected Text getTextInput() {
			return textInput;
		}

		protected SimpleLinkTextViewer getTextOutput() {
			return textOutput;
		}

		protected void setKeyListener(KeyListener listener) {
			if (listener != null)
				textInput.addKeyListener(listener);
		}

		protected ListViewer getListViewer() {
			return listViewer;
		}
	}

	public void createPartControl(Composite parent) {
		otherColor = colorFromRGBString(DEFAULT_OTHER_COLOR);
		systemColor = colorFromRGBString(DEFAULT_SYSTEM_COLOR);
		dateColor = colorFromRGBString(DEFAULT_DATE_COLOR);
		mainComp = new Composite(parent, SWT.NONE);
		mainComp.setLayout(new FillLayout());
		tabFolder = new CTabFolder(mainComp, SWT.NORMAL);
		// The following will allow tab folder to have close buttons on tab
		// items
		// tabFolder = new CTabFolder(mainComp, SWT.CLOSE);
		// tabFolder.setUnselectedCloseVisible(false);
		tabFolder.setSimple(false);
		tabFolder.addCTabFolder2Listener(new CTabFolder2Listener() {
			public void close(CTabFolderEvent event) {
				System.out.println("close(" + event + ")");
			}

			public void maximize(CTabFolderEvent event) {
				System.out.println("maximize(" + event + ")");
			}

			public void minimize(CTabFolderEvent event) {
				System.out.println("minimize(" + event + ")");
			}

			public void restore(CTabFolderEvent event) {
				System.out.println("restore(" + event + ")");
			}

			public void showList(CTabFolderEvent event) {
				System.out.println("showList(" + event + ")");
			}
		});
		rootChatRoomTabItem = new Manager(false, tabFolder, hostName,
				new KeyListener() {
					public void keyPressed(KeyEvent evt) {
						handleKeyPressed(evt);
					}

					public void keyReleased(KeyEvent evt) {
						handleKeyReleased(evt);
					}
				});
		writeText = rootChatRoomTabItem.getTextInput();
		readText = rootChatRoomTabItem.getTextOutput();
		setEnabled(false);
		makeActions();
		hookContextMenu();
	}

	public void initialize(final IChatRoomViewCloseListener parent,
			final IChatRoomContainer container,
			final IChatRoomManager chatRoomManager, final ID targetID,
			final IChatMessageSender sender) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				ChatRoomManagerView.this.chatRoomManager = chatRoomManager;
				ChatRoomManagerView.this.closeListener = parent;
				ChatRoomManagerView.this.chatRoomContainer = container;
				ChatRoomManagerView.this.chatHostID = targetID;
				ChatRoomManagerView.this.messageSender = sender;
				setUsernameAndHost(ChatRoomManagerView.this.chatHostID);
				ChatRoomManagerView.this.setPartName(VIEW_PREFIX + userName
						+ USERNAME_HOST_DELIMETER + hostName);
				ChatRoomManagerView.this.setTitleToolTip("IRC Host: "
						+ hostName);
				ChatRoomManagerView.this.rootChatRoomTabItem
						.setTabName(hostName);
				setEnabled(true);
			}
		});
	}

	protected void setEnabled(boolean enabled) {
		if (!writeText.isDisposed())
			writeText.setEnabled(enabled);
	}

	protected void clearInput() {
		writeText.setText("");
	}

	protected void handleCommands(String line, String[] tokens) {
		// Look at first one and switch
		String command = tokens[0];
		while (command.startsWith(COMMAND_PREFIX))
			command = command.substring(1);
		String[] args = new String[tokens.length - 1];
		System.arraycopy(tokens, 1, args, 0, tokens.length - 1);
		if (command.equalsIgnoreCase("QUIT")) {
			doQuit();
		} else if (command.equalsIgnoreCase("JOIN")) {
			String arg1 = args[0];
			String arg2 = "";
			if (args.length > 1) {
				arg2 = args[1];
			}
			doJoin(arg1, arg2);
		} else
			sendMessageLine(line);
	}

	protected void sendMessageLine(String line) {
		try {
			messageSender.sendMessage(line);
		} catch (IOException e) {
			// And cut ourselves off
			removeLocalUser();
		}
	}

	public void disconnected() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (disposed)
					return;
				setEnabled(false);
				setPartName("(" + getPartName() + ")");
			}
		});
	}

	protected void doQuit() {
		cleanUp();
	}

	protected void doJoin(String target, String key) {
		// With manager, first thing we do is get the IRoomInfo for the target
		// channel
		IRoomInfo roomInfo = chatRoomManager.getChatRoomInfo(target);
		// If it's null, we give up
		if (roomInfo == null) {
			// no room info for given target...give error message and skip
			return;
		} else {
			IChatRoomContainer chatRoomContainer = null;
			try {
				// Then we create a new container from the roomInfo
				chatRoomContainer = roomInfo.createChatRoomContainer();
				// Setup new user interface (new tab)
				final ChatRoom chatroomview = new ChatRoom(chatRoomContainer,
						new Manager(tabFolder, target));
				// setup message listener
				chatRoomContainer.addMessageListener(new IMessageListener() {
					public void handleMessage(ID fromID, ID toID, Type type,
							String subject, String messageBody) {
						chatroomview.handleMessage(fromID, toID, type, subject,
								messageBody);
					}
				});
				// setup invitation listener
				chatRoomContainer
						.addInvitationListener(new IInvitationListener() {
							public void handleInvitationReceived(ID roomID,
									ID from, ID toID, String subject,
									String body) {
								chatroomview.handleInvitationReceived(roomID,
										from, toID, subject, body);
							}
						});
				// setup participant listener
				chatRoomContainer
						.addChatParticipantListener(new IChatParticipantListener() {
							public void handlePresence(ID fromID,
									IPresence presence) {
								chatroomview.handlePresence(fromID, presence);
							}

							public void joined(ID user) {
								chatroomview.handleJoin(user);
							}

							public void left(ID user) {
								chatroomview.handleLeave(user);
							}
						});
				chatRoomContainer.addListener(new IContainerListener() {
					public void handleEvent(IContainerEvent evt) {
						if (evt instanceof IContainerDisconnectedEvent) {
							chatroomview.disconnected();
						}
					}
				}, null);
				// Now connect/join
				chatRoomContainer
						.connect(
								IDFactory.getDefault()
										.createID(
												chatRoomContainer
														.getConnectNamespace(),
												target), ConnectContextFactory
										.createPasswordConnectContext(key));
			} catch (Exception e) {
				// TODO: handle exception properly
				e.printStackTrace();
			}
		}
	}

	class ChatRoom implements IMessageListener, IInvitationListener,
			IParticipantListener, KeyListener {
		IChatRoomContainer container;

		Manager tabUI;

		Text inputText;

		SimpleLinkTextViewer outputText;

		IChatMessageSender channelMessageSender;

		private List otherUsers = Collections.synchronizedList(new ArrayList());

		IUser localUser;

		private ListViewer memberViewer = null;

		ChatRoom(IChatRoomContainer container, Manager tabItem) {
			this.container = container;
			this.channelMessageSender = container.getChatMessageSender();
			this.tabUI = tabItem;
			inputText = this.tabUI.getTextInput();
			outputText = this.tabUI.getTextOutput();
			memberViewer = this.tabUI.getListViewer();
			this.tabUI.setKeyListener(this);
		}

		public void handleMessage(final ID fromID, final ID toID,
				final Type type, final String subject, final String messageBody) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (disposed)
						return;
					appendText(outputText, new ChatLine(messageBody,
							new Participant(fromID)));
				}
			});
		}

		public void handleInvitationReceived(ID roomID, ID from, ID to,
				String subject, String body) {
			System.out.println("invitation room=" + roomID + ",from=" + from
					+ ",to=" + to + ",subject=" + subject + ",body=" + body);
		}

		public void keyPressed(KeyEvent e) {
			handleKeyPressed(e);
		}

		public void keyReleased(KeyEvent e) {
			handleKeyReleased(e);
		}

		protected void handleKeyPressed(KeyEvent evt) {
			if (evt.character == SWT.CR) {
				if (inputText.getText().trim().length() > 0)
					handleTextInput(inputText.getText());
				clearInput();
				evt.doit = false;
			}
		}

		protected void handleKeyReleased(KeyEvent evt) {
		}

		protected void handleTextInput(String text) {
			if (channelMessageSender == null) {
				MessageDialog.openError(getViewSite().getShell(),
						"Not connect", "Not connected to channel room");
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
				doQuit();
			} else if (command.equalsIgnoreCase("PART")) {
				doPartChannel();
			} else
				sendMessageLine(line);
		}

		protected void doPartChannel() {
			if (container != null)
				container.disconnect();
		}

		protected void clearInput() {
			inputText.setText("");
		}

		protected void sendMessageLine(String line) {
			try {
				channelMessageSender.sendMessage(line);
			} catch (IOException e) {
				// XXX handle gracefully
				e.printStackTrace();
			}
		}

		public void handlePresence(final ID fromID, final IPresence presence) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (disposed)
						return;
					boolean isAdd = presence.getType().equals(
							IPresence.Type.AVAILABLE);
					Participant p = new Participant(fromID);
					if (isAdd) {
						if (localUser == null && !otherUsers.contains(fromID)) {
							localUser = p;
						}
						addParticipant(p);
					} else {
						removeParticipant(p);
						if (isLocalUser(fromID))
							removeLocalUser();
					}
				}
			});
		}

		public void disconnected() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (disposed)
						return;
					if (!inputText.isDisposed())
						inputText.setEnabled(false);
				}
			});
		}

		protected void addParticipant(IUser p) {
			if (p != null) {
				ID id = p.getID();
				if (id != null) {
					appendText(outputText, new ChatLine("(" + getDateTime()
							+ ") " + trimUserID(id) + " entered", null));
					memberViewer.add(p);
				}
			}
		}

		protected boolean isLocalUser(ID id) {
			if (localUser == null)
				return false;
			else if (localUser.getID().equals(id)) {
				return true;
			} else
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
					if (otherUsers.contains(id))
						appendText(outputText, new ChatLine("(" + getDateTime()
								+ ") " + trimUserID(id) + " left", null));
					memberViewer.remove(p);
					if (isLocalUser(id))
						removeLocalUser();
				}
			}
		}

		protected void removeAllParticipants() {
			org.eclipse.swt.widgets.List l = memberViewer.getList();
			for (int i = 0; i < l.getItemCount(); i++) {
				Object o = memberViewer.getElementAt(i);
				if (o != null)
					memberViewer.remove(o);
			}
		}

		public void handleJoin(ID user) {
			if (disposed)
				return;
			otherUsers.add(user);
		}

		public void handleLeave(ID user) {
			if (disposed)
				return;
			otherUsers.remove(user);
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
		if (messageSender == null) {
			MessageDialog.openError(getViewSite().getShell(), "Not connect",
					"Not connected to chat room");
			return;
		} else
			handleInputLine(text);
	}

	protected void handleEnter() {
		if (writeText.getText().trim().length() > 0)
			handleTextInput(writeText.getText());
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
		writeText.setFocus();
	}

	protected void setUsernameAndHost(ID chatHostID) {
		URI uri = null;
		try {
			uri = chatHostID.toURI();
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
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (disposed)
					return;
				if (room != null)
					doJoin(room, null);
			}
		});
	}

	public void dispose() {
		disposed = true;
		cleanUp();
		super.dispose();
	}

	protected String getMessageString(ID fromID, String text) {
		return fromID.getName() + ": " + text + "\n";
	}

	public void handleMessage(final ID fromID, final ID toID, final Type type,
			final String subject, final String messageBody) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (disposed)
					return;
				appendText(readText, new ChatLine(messageBody, new Participant(
						fromID)));
			}
		});
	}

	private String trimUserID(ID userID) {
		URI aURI = null;
		try {
			aURI = userID.toURI();
		} catch (URISyntaxException e) {
			aURI = null;
		}
		if (aURI != null) {
			String user = aURI.getUserInfo();
			if (user != null)
				return user;
			else
				return userID.getName();
		} else {
			String userathost = userID.getName();
			int atIndex = userathost.lastIndexOf(USERNAME_HOST_DELIMETER);
			if (atIndex != -1) {
				userathost = userathost.substring(0, atIndex);
			}
			return userathost;
		}
	}

	class Participant implements IUser {
		private static final long serialVersionUID = 2008114088656711572L;

		ID id;

		public Participant(ID id) {
			this.id = id;
		}

		public ID getID() {
			return id;
		}

		public String getName() {
			return toString();
		}

		public boolean equals(Object other) {
			if (!(other instanceof Participant))
				return false;
			Participant o = (Participant) other;
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
	}

	protected String getCurrentDate(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String res = sdf.format(new Date());
		return res;
	}

	protected String getDateTime() {
		StringBuffer buf = new StringBuffer();
		buf.append(getCurrentDate(DEFAULT_DATE_FORMAT)).append(" ").append(
				getCurrentDate(DEFAULT_TIME_FORMAT));
		return buf.toString();
	}

	protected void cleanUp() {
		if (closeListener != null) {
			if (chatHostID == null)
				closeListener.chatRoomViewClosing(null);
			else
				closeListener.chatRoomViewClosing(chatHostID.getName());
			closeListener = null;
			chatRoomContainer = null;
			messageSender = null;
		}
	}

	protected void removeLocalUser() {
		// It's us that's gone away... so we're outta here
		String title = getPartName();
		setPartName("(" + title + ")");
		cleanUp();
		setEnabled(false);
	}

	public void handleInvitationReceived(ID roomID, ID from, ID toID,
			String subject, String body) {
		System.out.println("invitation room=" + roomID + ",from=" + from
				+ ",to=" + toID + ",subject=" + subject + ",body=" + body);
	}

	protected void appendText(SimpleLinkTextViewer readText, ChatLine text) {
		StyledText st = readText.getTextWidget();
		if (text == null || readText == null || st == null)
			return;
		int startRange = st.getText().length();
		StringBuffer sb = new StringBuffer();
		if (text.getOriginator() != null) {
			sb.append("(").append(getCurrentDate(DEFAULT_TIME_FORMAT)).append(
					") ");
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
			sr.foreground = otherColor;
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
		}
		if (!text.isNoCRLF()) {
			st.append("\n");
		}
		String t = st.getText();
		if (t == null)
			return;
		st.setSelection(t.length());
		// Bold title if view is not visible.
		IWorkbenchSiteProgressService pservice = (IWorkbenchSiteProgressService) this
				.getSite().getAdapter(IWorkbenchSiteProgressService.class);
		pservice.warnOfContentChange();
	}

	protected void outputClear() {
		if (MessageDialog.openConfirm(null, "Confirm Clear Text Output",
				"Are you sure you want to clear output?"))
			readText.getTextWidget().setText("");
	}

	protected void outputCopy() {
		String t = readText.getTextWidget().getSelectionText();
		if (t == null || t.length() == 0) {
			readText.getTextWidget().selectAll();
		}
		readText.getTextWidget().copy();
		readText.getTextWidget().setSelection(
				readText.getTextWidget().getText().length());
	}

	protected void outputPaste() {
		writeText.paste();
	}

	protected void outputSelectAll() {
		readText.getTextWidget().selectAll();
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
		outputCopy.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_COPY));
		outputClear = new Action() {
			public void run() {
				outputClear();
			}
		};
		outputClear.setText("Clear");
		outputClear.setToolTipText("Clear output window");
		outputPaste = new Action() {
			public void run() {
				outputPaste();
			}
		};
		outputPaste.setText("Paste");
		outputPaste.setToolTipText("Paste");
		outputCopy.setAccelerator(SWT.CTRL | 'V');
		outputPaste.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_PASTE));
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
		Menu menu = menuMgr.createContextMenu(readText.getControl());
		readText.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, readText);
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
		String[] vals = rgb.split(",");
		color = new Color(getViewSite().getShell().getDisplay(), Integer
				.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer
				.parseInt(vals[2]));
		return color;
	}
}