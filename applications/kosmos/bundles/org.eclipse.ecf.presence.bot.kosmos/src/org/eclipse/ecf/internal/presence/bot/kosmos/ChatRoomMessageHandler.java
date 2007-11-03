/*******************************************************************************
 * Copyright (c) 2007 Remy Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Remy Suen <remy.suen@gmail.com> - initial API and implementation
 *    Markus Kuppe <mkuppe@versant.com> - bug 1830436
 ******************************************************************************/
package org.eclipse.ecf.internal.presence.bot.kosmos;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDCreateException;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.util.ECFException;
import org.eclipse.ecf.presence.IIMMessageEvent;
import org.eclipse.ecf.presence.IIMMessageListener;
import org.eclipse.ecf.presence.bot.IChatRoomBotEntry;
import org.eclipse.ecf.presence.bot.IChatRoomMessageHandler;
import org.eclipse.ecf.presence.chatroom.IChatRoomContainer;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessage;
import org.eclipse.ecf.presence.chatroom.IChatRoomMessageSender;
import org.eclipse.ecf.presence.im.IChatMessageEvent;
import org.eclipse.ecf.presence.im.IChatMessageSender;
import org.eclipse.osgi.util.NLS;

public class ChatRoomMessageHandler implements IChatRoomMessageHandler {

	private static final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$

	private static final String LICENSE = "###############################################################################" //$NON-NLS-1$
			+ NEWLINE
			+ "# Copyright (c) 2007 Remy Suen and others." //$NON-NLS-1$
			+ NEWLINE
			+ "# All rights reserved. This program and the accompaning materials" //$NON-NLS-1$
			+ NEWLINE
			+ "# are made available under the terms of the Eclipse Public License v1.0" //$NON-NLS-1$
			+ NEWLINE
			+ "# which accompanies this distribution, and is available at" //$NON-NLS-1$
			+ NEWLINE
			+ "# http://www.eclipse.org/legal/epl-v10.html" //$NON-NLS-1$
			+ NEWLINE
			+ "#" //$NON-NLS-1$
			+ NEWLINE
			+ "# Contributors:" //$NON-NLS-1$
			+ NEWLINE
			+ "#    Remy Suen <remy.suen@gmail.com> - initial API and implementation" //$NON-NLS-1$
			+ NEWLINE
			+ "#    Markus Kuppe <mkuppe@versant.com> - bug 1830436" //$NON-NLS-1$
			+ NEWLINE
			+ "################################################################################"; //$NON-NLS-1$

	private static final String BUG_DATABASE_PREFIX = "https://bugs.eclipse.org/bugs/show_bug.cgi?id="; //$NON-NLS-1$
	private static final String BUG_DATABASE_POSTFIX = "&ctype=xml"; //$NON-NLS-1$
	private static final String SUM_OPEN_TAG = "<short_desc>"; //$NON-NLS-1$
	private static final String SUM_CLOSE_TAG = "</short_desc>"; //$NON-NLS-1$
	private static final File HTML_FILE = new File(
			"/home/rcjsuen/public_html/messages.html"); //$NON-NLS-1$

	private static final String URL_REGEX = "(http://.+|https://.+|ftp://.+)"; //$NON-NLS-1$
	private static final String CMD_REGEX = "(~.+)"; //$NON-NLS-1$

	private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
	private static final Pattern CMD_PATTERN = Pattern.compile(CMD_REGEX);

	private Map messageSenders;
	private Map newsgroups;
	private Set operators;
	private Properties messages;
	private JavadocAnalyzer analyzer;

	private IContainer container;

	private IChatMessageSender chatMessageSender;

	private String password;

	private static final String xmlDecode(String string) {
		if (string == null || string.equals("")) { //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}

		int index = string.indexOf("&amp;"); //$NON-NLS-1$
		while (index != -1) {
			string = string.substring(0, index) + '&'
					+ string.substring(index + 5);
			index = string.indexOf("&amp;", index + 1); //$NON-NLS-1$
		}

		index = string.indexOf("&quot;"); //$NON-NLS-1$
		while (index != -1) {
			string = string.substring(0, index) + '"'
					+ string.substring(index + 6);
			index = string.indexOf("&quot;", index + 1); //$NON-NLS-1$
		}

		index = string.indexOf("&apos;"); //$NON-NLS-1$
		while (index != -1) {
			string = string.substring(0, index) + '\''
					+ string.substring(index + 6);
			index = string.indexOf("&apos;", index + 1); //$NON-NLS-1$
		}

		index = string.indexOf("&lt;"); //$NON-NLS-1$
		while (index != -1) {
			string = string.substring(0, index) + '<'
					+ string.substring(index + 4);
			index = string.indexOf("&lt;", index + 1); //$NON-NLS-1$
		}

		index = string.indexOf("&gt;"); //$NON-NLS-1$
		while (index != -1) {
			string = string.substring(0, index) + '>'
					+ string.substring(index + 4);
			index = string.indexOf("&gt;", index + 1); //$NON-NLS-1$
		}
		return string;
	}

	public ChatRoomMessageHandler() {
		messageSenders = new HashMap();
		analyzer = new JavadocAnalyzer();

		try {
			parseOperators();
		} catch (Exception e) {
			operators = Collections.EMPTY_SET;
		}

		try {
			parseMessages();
		} catch (Exception e) {
			messages = new Properties();
		}

		try {
			parseNewsgroup();
		} catch (Exception e) {
			newsgroups = Collections.EMPTY_MAP;
		}
	}

	private void parseOperators() throws IOException {
		Properties properties = new Properties();
		InputStream stream = FileLocator.openStream(Activator.getBundle(),
				new Path("operators.properties"), false);
		properties.load(stream);
		stream.close();
		String operatorString = properties.getProperty("operators");
		String[] operators = operatorString.split(",");
		this.operators = new HashSet(operators.length);
		for (int i = 0; i < operators.length; i++) {
			this.operators.add(operators[i].trim());
		}
	}

	private void parseMessages() throws IOException {
		messages = new Properties();
		InputStream stream = FileLocator.openStream(Activator.getBundle(),
				new Path("messages.properties"), false);
		messages.load(stream);
		stream.close();
	}

	private void parseNewsgroup() throws IOException {
		Properties properties = new Properties();
		properties.load(JavadocAnalyzer.class
				.getResourceAsStream("newsgroup.txt"));
		newsgroups = new HashMap();
		for (Iterator it = properties.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			Object value = properties.get(key);
			newsgroups.put(key, value);
			newsgroups.put(value, value);
		}
	}

	private void sendMessage(ID id, String message) {
		try {
			if (container != null) {
				IChatRoomMessageSender sender = (IChatRoomMessageSender) messageSenders
						.get(id);
				if (sender == null) {
					chatMessageSender.sendChatMessage(id, message);
				} else {
					sender.sendMessage(message);
				}
			}
		} catch (ECFException e) {
			e.printStackTrace();
			container.disconnect();
			container = null;
		}
	}

	private void sendBug(ID roomID, String target, String number, String comment) {
		String urlString = BUG_DATABASE_PREFIX + number;
		if (comment != null) {
			urlString = urlString + "#c" + comment; //$NON-NLS-1$
		}
		if (target == null) {
			try {
				HttpURLConnection hURL = (HttpURLConnection) new URL(
						BUG_DATABASE_PREFIX + number + BUG_DATABASE_POSTFIX)
						.openConnection();
				hURL.setAllowUserInteraction(true);
				hURL.connect();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(hURL.getInputStream()));
				StringBuffer buffer = new StringBuffer();
				try {
					if (hURL.getResponseCode() != HttpURLConnection.HTTP_OK) {
						sendMessage(roomID, NLS.bind(CustomMessages.Bug,
								number, urlString));
						return;
					}

					String input = reader.readLine();
					buffer.append(input);
					while (input.indexOf(SUM_CLOSE_TAG) == -1) {
						input = reader.readLine();
						buffer.append(input);
					}
					hURL.disconnect();
				} catch (EOFException e) {
					hURL.disconnect();
					sendMessage(roomID, NLS.bind(CustomMessages.Bug, number,
							urlString));
					e.printStackTrace();
					return;
				}
				String webPage = buffer.toString();
				int summaryStartIndex = webPage.indexOf(SUM_OPEN_TAG);
				int summaryEndIndex = webPage.indexOf(SUM_CLOSE_TAG,
						summaryStartIndex);
				if (summaryStartIndex != -1 & summaryEndIndex != -1) {
					String summary = webPage.substring(summaryStartIndex
							+ SUM_OPEN_TAG.length(), summaryEndIndex);
					sendMessage(roomID, NLS.bind(CustomMessages
							.getString(CustomMessages.BugContent),
							new Object[] { number, xmlDecode(summary),
									urlString }));
				} else {
					sendMessage(roomID, NLS.bind(CustomMessages
							.getString(CustomMessages.BugContent),
							new Object[] { number, urlString }));
				}
			} catch (IOException e) {
				sendMessage(roomID, NLS.bind(CustomMessages
						.getString(CustomMessages.Bug), new Object[] { number,
						urlString }));
				e.printStackTrace();
			}
		} else {
			try {
				HttpURLConnection hURL = (HttpURLConnection) new URL(
						BUG_DATABASE_PREFIX + number + BUG_DATABASE_POSTFIX)
						.openConnection();
				hURL.setAllowUserInteraction(true);
				hURL.connect();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(hURL.getInputStream()));
				StringBuffer buffer = new StringBuffer();
				try {
					if (hURL.getResponseCode() != HttpURLConnection.HTTP_OK) {
						sendMessage(roomID, NLS.bind(CustomMessages
								.getString(CustomMessages.Bug_Reply),
								new Object[] { target, number, urlString }));
						return;
					}

					String input = reader.readLine();
					buffer.append(input);
					while (input.indexOf(SUM_CLOSE_TAG) == -1) {
						input = reader.readLine();
						buffer.append(input);
					}
					hURL.disconnect();
				} catch (EOFException e) {
					hURL.disconnect();
					sendMessage(roomID, NLS.bind(CustomMessages
							.getString(CustomMessages.Bug_Reply), new Object[] {
							target, number, urlString }));
					return;
				}
				String webPage = buffer.toString();
				int summaryStartIndex = webPage.indexOf(SUM_OPEN_TAG);
				int summaryEndIndex = webPage.indexOf(SUM_CLOSE_TAG,
						summaryStartIndex);
				if (summaryStartIndex != -1 & summaryEndIndex != -1) {
					String summary = webPage.substring(summaryStartIndex
							+ SUM_OPEN_TAG.length(), summaryEndIndex);
					sendMessage(roomID, NLS.bind(CustomMessages
							.getString(CustomMessages.BugContent_Reply),
							new Object[] { target, number, xmlDecode(summary),
									urlString }));
				} else {
					sendMessage(roomID, NLS.bind(CustomMessages
							.getString(CustomMessages.BugContent_Reply),
							new Object[] { target, number, urlString }));
				}
			} catch (IOException e) {
				sendMessage(roomID, NLS.bind(CustomMessages
						.getString(CustomMessages
								.getString(CustomMessages.Bug_Reply)),
						new Object[] { target, number, urlString }));
			}
		}
	}

	private void sendNewsgroupSearch(ID roomID, String target, String query) {
		String[] strings = query.split(" "); //$NON-NLS-1$
		if (strings.length == 1) {
			// no search terms provided
			return;
		}
		for (int i = 0; i < strings.length; i++) {
			try {
				strings[i] = URLEncoder.encode(strings[i].trim(), "UTF-8"); //$NON-NLS-1$
			} catch (UnsupportedEncodingException e) {
				// technically this should never happen, but better safe than
				// sorry
				strings[i] = URLEncoder.encode(strings[i].trim());
			}
		}
		String newsgroup = (String) newsgroups.get(strings[0]);
		if (target == null) {
			StringBuffer buffer = new StringBuffer();
			synchronized (buffer) {
				for (int i = 1; i < strings.length; i++) {
					buffer.append(strings[i] + '+');
				}
				buffer.deleteCharAt(buffer.length() - 1);
			}
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.NewsgroupSearch), buffer
					.toString(), newsgroup));
		} else {
			StringBuffer buffer = new StringBuffer();
			synchronized (buffer) {
				for (int i = 1; i < strings.length; i++) {
					buffer.append(strings[i] + '+');
				}
				buffer.deleteCharAt(buffer.length() - 1);
			}
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.NewsgroupSearch_Reply),
					new Object[] { target, buffer.toString(), newsgroup }));
		}
	}

	private void sendGoogle(ID roomID, String target, String searchString) {
		searchString = searchString.replace(' ', '+');
		if (target == null) {
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.Google), searchString));
		} else {
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.Google_Reply), target,
					searchString));
		}
	}

	private void sendWiki(ID roomID, String target, String articleName) {
		articleName = articleName.replace(' ', '_');
		if (target == null) {
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.Wiki), articleName));
		} else {
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.Wiki_Reply), target, articleName));
		}
	}

	private void sendEclipseHelp(ID roomID, String target, String searchString) {
		searchString = searchString.replace(' ', '+');
		if (target == null) {
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.EclipseHelp), searchString));
		} else {
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.EclipseHelp_Reply), target,
					searchString));
		}
	}

	private void sendJavaDoc(ID roomID, String target, String parameter) {
		String append = target == null ? "" : target + ": ";
		String message = null;
		int index = parameter.indexOf('#');
		if (index == -1) {
			message = analyzer.getJavadocs(parameter);
		} else {
			String className = parameter.substring(0, index);
			parameter = parameter.substring(index + 1);
			index = parameter.indexOf('(');
			if (index == -1) {
				message = className + '#' + parameter + " - "
						+ analyzer.getJavadocs(className, parameter);
			} else {
				String method = parameter.substring(0, index);
				parameter = parameter.substring(index + 1);
				parameter = parameter.substring(0, parameter.indexOf(')'));
				String[] parameters = parameter.split(",");
				for (int i = 0; i < parameters.length; i++) {
					parameters[i] = parameters[i].trim();
				}
				message = className + '#' + method + " - "
						+ analyzer.getJavadocs(className, method, parameters);
			}
		}
		sendMessage(roomID, append + message);
	}

	private void sendMessageList(ID roomID, String target) {
		if (target == null) {
			sendMessage(roomID, CustomMessages.getString(CustomMessages.MessageList));
		} else {
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.MessageList_Reply), target));
		}
	}

	private void writeToHTML() throws IOException {
		FileWriter out = new FileWriter(HTML_FILE);
		out
				.write("<html>\n<head><title>KOS-MOS Commands</title></head>\n<body>\n<table cellspacing=\"2\" cellpadding=\"2\" border=\"0\">\n"); //$NON-NLS-1$
		Iterator it = messages.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			String val = messages.getProperty(key);
			out.write(formatTableRow(key, val));
		}

		out.write("</table>\n</body></html>\n"); //$NON-NLS-1$
		out.flush();

		try {
			out.close();
		} catch (IOException e) {
			// ignored
		}
	}

	private String formatTableRow(String key, String val) {
		return "<tr valign=\"top\"><td><b>" //$NON-NLS-1$
				+ key
				+ "</b></td><td>" //$NON-NLS-1$
				+ text2html(val)
				+ "</td></tr>\n<tr><td colspan=\"2\"><hr noshade=\"noshade\" size=\"1\" width=\"100%\"/></td></tr>\n\n"; //$NON-NLS-1$
	}

	private String text2html(String val) {
		StringTokenizer st = new StringTokenizer(val, " )(\"", true); //$NON-NLS-1$
		StringBuffer sb = new StringBuffer();
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			Matcher patternMatcher = URL_PATTERN.matcher(tok);
			if (patternMatcher.matches()) {
				sb.append("<a href=\""); //$NON-NLS-1$
				sb.append(patternMatcher.group(1));
				sb.append("\">"); //$NON-NLS-1$
				sb.append(patternMatcher.group(1));
				sb.append("</a>"); //$NON-NLS-1$
			} else {
				Matcher cmdMatcher = CMD_PATTERN.matcher(tok);
				if (cmdMatcher.matches()) {
					sb.append("<b style=\"color:red\">"); //$NON-NLS-1$
					sb.append(cmdMatcher.group(1));
					sb.append("</b>"); //$NON-NLS-1$
				} else {
					sb.append(tok);
				}
			}
		}
		return sb.toString();
	}

	private boolean isProcessed(ID roomID, String target, String msg) {
		String reply = (String) messages.get(msg);
		if (reply == null) {
			return false;
		}
		if (target == null) {
			sendMessage(roomID, reply);
		} else {
			sendMessage(roomID, target + ": " + reply); //$NON-NLS-1$
		}
		return true;
	}

	private void learn(ID roomID, String contents) {
		String key = contents.split(" ")[0];
		try {
			URL url = FileLocator.find(Activator.getBundle(), new Path(
					"messages.properties"), null);
			url = FileLocator.resolve(url);

			String property = messages.getProperty(key);
			if (property == null) {
				OutputStream stream = new FileOutputStream(url.getPath());
				messages.setProperty(key, contents.substring(key.length())
						.trim());
				messages.store(stream, LICENSE);
				writeToHTML();
				sendMessage(roomID, NLS.bind(CustomMessages
						.getString(CustomMessages.Learn_Reply), key));
			} else {
				sendMessage(roomID, NLS.bind(CustomMessages
						.getString(CustomMessages.Learn_Conflict), key,
						property));
			}
		} catch (Exception e) {
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.Learn_Failure), key));
		}
	}

	private void update(ID roomID, String contents) {
		String key = contents.split(" ")[0];
		try {
			URL url = FileLocator.find(Activator.getBundle(), new Path(
					"messages.properties"), null);
			url = FileLocator.resolve(url);

			OutputStream stream = new FileOutputStream(url.getPath());
			messages.setProperty(key, contents.substring(key.length()).trim());
			messages.store(stream, LICENSE);
			writeToHTML();
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.Learn_Update), key));
		} catch (Exception e) {
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.Learn_Failure), key));
		}
	}

	private void remove(ID roomID, String contents) {
		String key = contents.split(" ")[0];
		try {
			URL url = FileLocator.find(Activator.getBundle(), new Path(
					"messages.properties"), null);
			url = FileLocator.resolve(url);

			OutputStream stream = new FileOutputStream(url.getPath());
			messages.remove(key);
			messages.store(stream, LICENSE);
			writeToHTML();
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.Learn_Remove), key));
		} catch (Exception e) {
			sendMessage(roomID, NLS.bind(CustomMessages
					.getString(CustomMessages.Learn_Failure), key));
		}
	}

	private void send(ID fromID, ID roomID, String target, String msg) {
		if (isProcessed(roomID, target, msg)) {
			return;
		}

		if (msg.startsWith("add ")) {
			if (operators.contains(fromID.getName())) {
				learn(roomID, msg.substring(4).trim());
			} else {
				sendMessage(
						roomID,
						fromID
								+ ": "
								+ CustomMessages
										.getString(CustomMessages.No_Operation_Privileges));
			}
		} else if (msg.startsWith("set ") || msg.startsWith("update")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (operators.contains(fromID.getName())) {
				update(roomID, msg.substring(4).trim());
			} else {
				sendMessage(
						roomID,
						fromID
								+ ": "
								+ CustomMessages
										.getString(CustomMessages.No_Operation_Privileges));
			}
		} else if (msg.startsWith("remove ")) { //$NON-NLS-1$
			if (operators.contains(fromID.getName())) {
				remove(roomID, msg.substring(7).trim());
			} else {
				sendMessage(
						roomID,
						fromID
								+ ": "
								+ CustomMessages
										.getString(CustomMessages.No_Operation_Privileges));
			}
		} else if (msg.startsWith("bug")) { //$NON-NLS-1$
			msg = msg.substring(3).trim();
			int index = msg.indexOf('c');
			if (index == -1) {
				try {
					// check if what's before the 'c' is a valid number
					Integer.parseInt(msg);
					sendBug(roomID, target, msg, null);
				} catch (NumberFormatException e) {
					// ignored
				}
			} else {
				try {
					// check if what's before the 'c' is a valid number
					Integer.parseInt(msg.substring(0, index));
					sendBug(roomID, target, msg.substring(0, index), msg
							.substring(index + 1));
				} catch (NumberFormatException e) {
					// ignored
				}
			}
		} else if (msg.startsWith("javadoc ")) { //$NON-NLS-1$
			sendJavaDoc(roomID, target, msg.substring(8));
		} else if (msg.startsWith("api ")) { //$NON-NLS-1$
			sendJavaDoc(roomID, target, msg.substring(4));
		} else if (msg.startsWith("news ")) { //$NON-NLS-1$
			sendNewsgroupSearch(roomID, target, msg.substring(5));
		} else if (msg.startsWith("newsgroup ")) {
			sendNewsgroupSearch(roomID, target, msg.substring(10));
		} else if (msg.startsWith("g ")) { //$NON-NLS-1$
			sendGoogle(roomID, target, msg.substring(2));
		} else if (msg.startsWith("wiki ")) { //$NON-NLS-1$
			sendWiki(roomID, target, msg.substring(5));
		} else if (msg.startsWith("eh")) { //$NON-NLS-1$
			sendEclipseHelp(roomID, target, msg.substring(3));
		} else if (msg.equals("list")) { //$NON-NLS-1$
			sendMessageList(roomID, target);
		} else {
			int index = msg.indexOf('c');
			if (index == -1) {
				try {
					// check if what's before the 'c' is a valid number
					Integer.parseInt(msg);
					sendBug(roomID, target, msg, null);
				} catch (NumberFormatException e) {
					// ignored
				}
			} else {
				try {
					// check if what's before the 'c' is a valid number
					Integer.parseInt(msg.substring(0, index));
					sendBug(roomID, target, msg.substring(0, index), msg
							.substring(index + 1));
				} catch (NumberFormatException e) {
					// ignored
				}
			}
		}
	}

	private String[] parseInput(String msg) {
		if (msg.startsWith("tell")) { //$NON-NLS-1$
			msg = msg.substring(5);
			int index = msg.indexOf(' ');
			if (index == -1) {
				return null;
			}
			String user = msg.substring(0, index);
			msg = msg.substring(index + 1);
			index = msg.indexOf(' ');
			if (index == -1) {
				return null;
			}
			String tmp = msg.substring(0, index);
			if (tmp.equals("about")) { //$NON-NLS-1$
				msg = msg.substring(index + 1);
			}
			return new String[] { user, msg };
		} else {
			return new String[] { null, msg };
		}
	}

	private void handleMessage(ID fromID, ID roomID, String message) {
		try {
			String[] info = parseInput(message);
			if (info != null) {
				send(fromID, roomID, info[0], info[1]);
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}

	public void handleRoomMessage(IChatRoomMessage message) {
		String msg = message.getMessage();
		switch (msg.charAt(0)) {
		case '~':
		case '!':
			handleMessage(message.getFromID(), message.getChatRoomID(), msg
					.substring(1).trim());
			break;
		}
	}

	public void init(IChatRoomBotEntry robot) {
		// nothing to do
	}

	public void preChatRoomConnect(IChatRoomContainer roomContainer, ID roomID) {
		messageSenders.put(roomID, roomContainer.getChatRoomMessageSender());
		if (password != null) {
			try {
				sendMessage(IDFactory.getDefault().createStringID("nickserv"),
						"identify " + password);
			} catch (IDCreateException e) {
			}
		}
	}

	public void preContainerConnect(IContainer container, ID targetID) {
		File file = new File(Platform.getInstanceLocation().getURL().getPath(),
				"password.properties");
		if (file.exists()) {
			Properties properties = new Properties();
			try {
				properties.load(new FileInputStream(file));
				password = properties.getProperty("password");
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
		this.container = container;
		IChatRoomContainer chatRoomContainer = (IChatRoomContainer) container
				.getAdapter(IChatRoomContainer.class);
		chatMessageSender = chatRoomContainer.getPrivateMessageSender();
		chatRoomContainer.addMessageListener(new IIMMessageListener() {
			public void handleMessageEvent(IIMMessageEvent e) {
				if (e instanceof IChatMessageEvent) {
					IChatMessageEvent event = (IChatMessageEvent) e;
					String msg = event.getChatMessage().getBody();
					switch (msg.charAt(0)) {
					case '~':
					case '!':
						handleMessage(event.getFromID(), event.getFromID(), msg
								.substring(1).trim());
						break;
					default:
						handleMessage(event.getFromID(), event.getFromID(), msg
								.trim());
						break;
					}
				}
			}
		});
	}

}
