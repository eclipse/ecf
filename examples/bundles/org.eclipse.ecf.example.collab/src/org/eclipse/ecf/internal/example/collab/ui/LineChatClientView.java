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

package org.eclipse.ecf.internal.example.collab.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.example.collab.share.HelloMessageSharedObject;
import org.eclipse.ecf.example.collab.share.TreeItem;
import org.eclipse.ecf.example.collab.share.User;
import org.eclipse.ecf.example.collab.share.url.ShowURLSharedObject;
import org.eclipse.ecf.example.collab.share.url.StartProgramSharedObject;
import org.eclipse.ecf.internal.example.collab.ClientPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.part.ViewPart;

public class LineChatClientView implements FileSenderUI {
	public static final String DEFAULT_UNIX_BROWSER = "mozilla";
	public static final String ENTER_STRING = "ARRIVED";
	public static final String EXECPROGARGTYPES[] = { ID.class.getName(),
			"[Ljava.lang.String;", "[Ljava.lang.String;",
			Boolean.class.getName(), Boolean.class.getName() };
	public static final String EXECPROGCLASSNAME = StartProgramSharedObject.class
			.getName();
	public static final String LEFT_STRING = "LEFT";
	public static final String MESSAGECLASSNAME = HelloMessageSharedObject.class
			.getName();
	public static final String REMOTEFILEPATH = null;
	public static final String SHOWURLARGTYPES[] = { ID.class.getName(),
			"java.lang.String" };
	public static final String SHOWURLCLASSNAME = ShowURLSharedObject.class
			.getName();
	
	private boolean showTimestamp = ClientPlugin.getDefault()
			.getPreferenceStore().getBoolean(
					ClientPlugin.PREF_DISPLAY_TIMESTAMP);
	private SimpleDateFormat df = new SimpleDateFormat("MM/dd hh:mm a"); //$NON-NLS-1$
	String downloaddir;
	LineChatHandler lch;
	Hashtable myNames = new Hashtable();
	String name;
	private TeamChat teamChat;
	User userdata;
	LineChatView view;

	private List users;

	public LineChatClientView(LineChatHandler lch, LineChatView view,
			String name, String initText, String downloaddir) {
		super();
		this.lch = lch;
		this.view = view;
		this.name = name;
		this.teamChat = new TeamChat(this, view.tabFolder, SWT.NULL, initText);
		this.userdata = lch.getUser();
		this.downloaddir = downloaddir;
		users = new ArrayList();
		teamChat.getTableViewer().setInput(users);
		if (userdata != null)
			addUser(userdata);

		ClientPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(new IPropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent event) {
						if (event.getProperty().equals(
								ClientPlugin.PREF_DISPLAY_TIMESTAMP)) {
							showTimestamp = ((Boolean) event.getNewValue())
									.booleanValue();
						}
					}

				});

		JFaceResources.getColorRegistry().put(ViewerToolTip.HEADER_BG_COLOR,
				new RGB(255, 255, 255));
		JFaceResources.getFontRegistry().put(
				ViewerToolTip.HEADER_FONT,
				JFaceResources.getFontRegistry().getBold(
						JFaceResources.getDefaultFont().getFontData()[0]
								.getName()).getFontData());

		ToolTip toolTip = new ViewerToolTip(teamChat.getTableViewer()
				.getControl());
		toolTip.setShift(new Point(-5, -5));
		toolTip.setHideOnMouseDown(false);
	}

	public ViewPart getView() {
		return view;
	}

	public Control getTextControl() {
		return teamChat.getTextControl();
	}

	public Control getTreeControl() {
		return teamChat.getTreeControl();
	}

	public boolean addUser(User ud) {
		if (ud == null)
			return false;
		ID userID = ud.getUserID();
		String username = ud.getNickname();
		if (myNames.containsKey(userID)) {
			String existingName = (String) myNames.get(userID);
			if (!existingName.equals(username)) {
				myNames.put(userID, username);
				final String str = existingName + " changed name to "
						+ username;
				showLine(new ChatLine(str));
			}
			return false;
		} else {
			myNames.put(userID, username);
			addUserToTree(ud);
			showLine(new ChatLine(username + " " + ENTER_STRING));
			return true;
		}
	}

	protected void addUserToTree(final User user) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				users.add(user);
				teamChat.getTableViewer().add(user);
			}
		});
	}

	protected void appendAndScrollToBottom(final ChatLine str) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (teamChat != null)
					teamChat.appendText(str);
			}
		});
	}

	public boolean changeUser(User user) {
		return changeUserInTree(user);
	}

	protected boolean changeUserInTree(final User userdata) {
		for (int i = 0; i < users.size(); i++) {
			final User user = (User) users.get(i);
			if (user.getUserID().equals(userdata.getUserID())) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						TableViewer view = teamChat.getTableViewer();
						view.remove(user);
						users.remove(user);
						view.add(userdata);
						users.add(userdata);
					}
				});
				return true;
			}
		}
		return false;
	}

	protected void closeClient() {
		if (lch != null) {
			lch.chatGUIDestroy();
		}
	}

	protected String getCurrentDateTime() {
		StringBuffer sb = new StringBuffer("["); //$NON-NLS-1$
		sb.append(df.format(new Date())).append(']');
		return sb.toString();
	}

	public void disposeClient() {
		myNames.clear();
		users.clear();
		if (teamChat != null) {
			final ChatWindow chatWindow = teamChat.chatWindow;
			if (chatWindow != null && !Display.getDefault().isDisposed()) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						chatWindow.close();
					}
				});
			}

			teamChat = null;
		}
		if (lch != null) {
			lch = null;
		}
		view.disposeClient(this);
	}

	protected TeamChat getTeamChat() {
		return teamChat;
	}

	protected String getUserData(ID id) {
		return (String) myNames.get(id);
	}

	public User getUser(ID id) {
		if (id == null) {
			return null;
		} else {
			for (int i = 0; i < users.size(); i++) {
				User user = (User) users.get(i);
				if (id.equals(user.getUserID())) {
					return user;
				}
			}
			return null;
		}
	}

	protected void handleTextInput(String text) {
		ChatLine line = new ChatLine(text, getCurrentDateTime());

		if (lch != null) {
			line.setOriginator(userdata);
		}
		appendAndScrollToBottom(line);
		teamChat.clearInput();

		if (lch != null)
			lch.inputText(text);
	}

	protected void createObject(ID target, String className, String[] args) {
		createObject(target, className, null, args);
	}

	protected void createObject(ID target, final String className,
			String[] argTypes, Object[] args) {
		if (lch != null) {
			HashMap map = new HashMap();
			map.put("args", args);
			map.put("types", argTypes);
			try {
				lch.createObject(target, className, map);
			} catch (final Exception e) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageDialog.openInformation(null,
								"Make Object Exception",
								"Exception creating instance of '" + className
										+ "'. \nException: " + e);
					}
				});
				e.printStackTrace();
				lch.chatException(e, "createObject(" + className + ")");
			}
		}
	}

	protected void refreshTreeView() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (teamChat != null) {
					try {
						teamChat.getTableViewer().refresh();
					} catch (Exception e) {
					}
				}
			}
		});
	}

	public void removeUser(ID id) {
		String name = getUserData(id);
		if (name != null) {
			showLine(new ChatLine(name + " " + LEFT_STRING));
		}
		myNames.remove(id);
		removeUserFromTree(id);
	}

	protected void removeUserFromTree(ID id) {
		if (id == null) {
			return;
		} else {
			for (int i = 0; i < users.size(); i++) {
				final User user = (User) users.get(i);
				if (user.getUserID().equals(id)) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							teamChat.getTableViewer().remove(user);
						}
					});
					users.remove(i);
					break;
				}
			}
		}
	}

	protected void runProgram(ID receiver, String program, String[] env) {
		String[] cmds = { program };
		Object[] args = { receiver, cmds, env, new Boolean(receiver == null),
				new Boolean(false) };
		// Do it
		createObject(null, EXECPROGCLASSNAME, EXECPROGARGTYPES, args);
	}

	public void sendData(File aFile, long dataLength) {
	}

	public void sendDone(File aFile, Exception e) {
		if (e != null) {
			showLine(new ChatLine("Exception '" + e.getMessage()
					+ "' sending file '" + aFile.getName()));
		} else {
			showLine(new ChatLine("\tSend of '" + aFile.getName()
					+ "' completed"));
			if (lch != null)
				lch.refreshProject();
		}
	}

	public void sendStart(File aFile, long length, float rate) {
		// present user with notification that file is being transferred
		showLine(new ChatLine("\tSending '" + aFile.getName() + "'"));
	}

	public void setTitle(String title) {
		// NOTHING HAPPENS
	}

	public void showLine(ChatLine line) {
		if (showTimestamp) {
			line.setDate(getCurrentDateTime());
		}
		appendAndScrollToBottom(line);
	}

	public void startedTyping(final User user) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (teamChat != null)
					teamChat.setStatus(user.getNickname() + " is typing...");
			}
		});
	}

	public void toFront() {
		view.setActiveTab(name);
	}

	public boolean updateTreeDisplay(ID id, TreeItem item) {
		if (id == null) {
			return false;
		} else {
			for (int i = 0; i < users.size(); i++) {
				User user = (User) users.get(i);
				if (user.getUserID().equals(id)) {
					teamChat.getTableViewer().refresh(user);
					return true;
				}
			}
			return false;
		}
	}

	private class ViewerToolTip extends ToolTip {

		public static final String HEADER_BG_COLOR = ClientPlugin.PLUGIN_ID
				+ ".TOOLTIP_HEAD_BG_COLOR"; //$NON-NLS-1$

		public static final String HEADER_FONT = ClientPlugin.PLUGIN_ID
				+ ".TOOLTIP_HEAD_FONT"; //$NON-NLS-1$

		public ViewerToolTip(Control control) {
			super(control);
		}

		protected Composite createToolTipContentArea(Event event,
				Composite parent) {
			Widget item = teamChat.getTableViewer().getTable().getItem(
					new Point(event.x, event.y));
			User user = (User) item.getData();

			GridLayout gl = new GridLayout();
			gl.marginBottom = 0;
			gl.marginTop = 0;
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			gl.marginLeft = 0;
			gl.marginRight = 0;
			gl.verticalSpacing = 1;
			parent.setLayout(gl);

			Composite topArea = new Composite(parent, SWT.NONE);
			GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			data.widthHint = 200;
			topArea.setLayoutData(data);
			topArea.setBackground(JFaceResources.getColorRegistry().get(
					HEADER_BG_COLOR));

			gl = new GridLayout();
			gl.marginBottom = 2;
			gl.marginTop = 2;
			gl.marginHeight = 0;
			gl.marginWidth = 0;
			gl.marginLeft = 5;
			gl.marginRight = 2;

			topArea.setLayout(gl);

			Label l = new Label(topArea, SWT.NONE);
			l.setText(user.getNickname());
			l.setBackground(JFaceResources.getColorRegistry().get(
					HEADER_BG_COLOR));
			l.setFont(JFaceResources.getFontRegistry().get(HEADER_FONT));
			l.setLayoutData(data);

			createContentArea(parent, user.getUserFields()).setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, true));

			return parent;
		}

		protected Control createContentArea(Composite parent, Vector fields) {
			Text label = new Text(parent, SWT.READ_ONLY | SWT.MULTI);
			label.setBackground(parent.getDisplay().getSystemColor(
					SWT.COLOR_INFO_BACKGROUND));
			StringBuffer buffer = new StringBuffer();
			synchronized (buffer) {
				for (int i = 0; i < fields.size(); i++) {
					buffer.append(fields.get(i));
					buffer.append(Text.DELIMITER);
				}
			}
			label.setText(buffer.toString().trim());
			label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
					false));
			return label;
		}

		protected boolean shouldCreateToolTip(Event e) {
			if (super.shouldCreateToolTip(e)) {
				Widget item = teamChat.getTableViewer().getTable().getItem(
						new Point(e.x, e.y));
				if (item != null) {
					User user = (User) item.getData();
					Vector fields = user.getUserFields();
					return fields != null && !fields.isEmpty();
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}
}