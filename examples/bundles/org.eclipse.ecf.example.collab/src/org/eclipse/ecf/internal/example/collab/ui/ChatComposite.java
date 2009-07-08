/****************************************************************************
 * Copyright (c) 2004, 2008 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *    Hiroyuki Inaba <Hiroyuki <hiroyuki.inaba@gmail.com> - bug 222253
 *****************************************************************************/

package org.eclipse.ecf.internal.example.collab.ui;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.core.util.StringUtils;
import org.eclipse.ecf.example.collab.share.io.FileSenderUI;
import org.eclipse.ecf.example.collab.share.io.FileTransferParams;
import org.eclipse.ecf.internal.example.collab.ClientPlugin;
import org.eclipse.ecf.presence.IPresenceContainerAdapter;
import org.eclipse.ecf.presence.im.IChatManager;
import org.eclipse.ecf.presence.ui.MessagesView;
import org.eclipse.ecf.ui.screencapture.IImageSender;
import org.eclipse.ecf.ui.screencapture.ScreenCaptureJob;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.views.*;

public class ChatComposite extends Composite {
	private static final String CHAT_OUTPUT_FONT = "ChatFont"; //$NON-NLS-1$
	final LineChatClientView view;
	private Color meColor = null;
	private Color otherColor = null;
	private Color systemColor = null;

	Action appShare = null;

	Action coBrowseURL = null;
	Action outputClear = null;
	Action outputCopy = null;
	Action outputPaste = null;
	Action outputSelectAll = null;
	Action sendFileToGroup = null;
	Action sendFileToGroupAndLaunch = null;
	Action sendMessage = null;
	Action closeGroup = null;

	Action sendShowViewRequest = null;

	Action showChatWindow;

	protected final String TEXT_INPUT_INIT = MessageLoader.getString("LineChatClientView.textinputinit"); //$NON-NLS-1$
	protected static final int DEFAULT_INPUT_HEIGHT = 25;
	protected static final int DEFAULT_INPUT_SEPARATOR = 5;

	Text textinput = null;
	StyledText textoutput = null;
	TableViewer tableView = null;
	ChatDropTarget chatDropTarget = null;
	TreeDropTarget treeDropTarget = null;

	ChatWindow chatWindow;
	boolean typing;

	ChatComposite(LineChatClientView view, Composite parent, TableViewer table, String initText) {
		this(view, parent, table, initText, null);
	}

	ChatComposite(LineChatClientView view, Composite parent, TableViewer table, String initText, ChatWindow chatWindow) {
		super(parent, SWT.NONE);
		this.view = view;
		this.chatWindow = chatWindow;
		setLayout(new FillLayout());
		meColor = colorFromRGBString(ClientPlugin.getDefault().getPluginPreferences().getString(ClientPlugin.PREF_ME_TEXT_COLOR));
		otherColor = colorFromRGBString(ClientPlugin.getDefault().getPluginPreferences().getString(ClientPlugin.PREF_OTHER_TEXT_COLOR));
		systemColor = colorFromRGBString(ClientPlugin.getDefault().getPluginPreferences().getString(ClientPlugin.PREF_SYSTEM_TEXT_COLOR));
		ClientPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(new ColorPropertyChangeListener());

		this.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if (meColor != null) {
					meColor.dispose();
				}

				if (otherColor != null) {
					otherColor.dispose();
				}

				if (systemColor != null) {
					systemColor.dispose();
				}
			}

		});
		final Composite chattingComposite = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		chattingComposite.setLayout(layout);

		tableView = table;

		textoutput = createStyledTextWidget(chattingComposite);
		textoutput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final String fontName = ClientPlugin.getDefault().getPluginPreferences().getString(ClientPlugin.PREF_CHAT_FONT);
		if (!(fontName == null) && !(fontName.equals(""))) { //$NON-NLS-1$
			final FontRegistry fr = ClientPlugin.getDefault().getFontRegistry();
			final FontData[] newFont = {new FontData(fontName)};

			fr.put(CHAT_OUTPUT_FONT, newFont);
			textoutput.setFont(fr.get(CHAT_OUTPUT_FONT));
		}

		ClientPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(new FontPropertyChangeListener());

		textoutput.append(initText);

		textinput = new Text(chattingComposite, SWT.SINGLE | SWT.BORDER);
		textinput.setText(TEXT_INPUT_INIT);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		GC gc = new GC(textinput);
		gc.setFont(textinput.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		gd.heightHint = fontMetrics.getHeight() * 2;
		textinput.setLayoutData(gd);

		textinput.selectAll();
		textinput.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent evt) {
				handleKeyPressed(evt);
			}

			public void keyReleased(KeyEvent evt) {
				handleKeyReleased(evt);
			}
		});
		textinput.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				final String t = textinput.getText();
				if (t.equals(TEXT_INPUT_INIT)) {
					textinput.selectAll();
				}
			}

			public void focusLost(FocusEvent e) {
			}
		});
		textinput.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
				final String t = textinput.getText();
				if (t.equals(TEXT_INPUT_INIT)) {
					textinput.selectAll();
				}
			}
		});
		textinput.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (typing && textinput.getText().trim().length() == 0)
					typing = false;
				else if (!typing) {
					typing = true;
					ChatComposite.this.view.lch.sendStartedTyping();
				}
			}
		});

		// make actions
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		initializeDropTargets();
	}

	private StyledText createStyledTextWidget(Composite parent) {
		try {
			final SourceViewer result = new SourceViewer(parent, null, null, true, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
			result.configure(new TextSourceViewerConfiguration(EditorsUI.getPreferenceStore()));
			result.setDocument(new Document());
			return result.getTextWidget();
		} catch (final Exception e) {
			ClientPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, ClientPlugin.PLUGIN_ID, IStatus.WARNING, MessageLoader.getString("ChatComposite.NO_HYPERLINKING"), e)); //$NON-NLS-1$
			return new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
		} catch (final NoClassDefFoundError e) {
			ClientPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, ClientPlugin.PLUGIN_ID, IStatus.WARNING, MessageLoader.getString("ChatComposite.NO_HYPERLINKING"), e)); //$NON-NLS-1$
			return new StyledText(parent, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
		}
	}

	public void appendText(ChatLine text) {
		if (text == null || textoutput == null || textoutput.isDisposed()) {
			return;
		}

		final IUser user = text.getOriginator();
		final StyleRange range = new StyleRange();
		range.start = textoutput.getText().length();
		if (user != null) {
			String prefix = user.getNickname() + ": "; //$NON-NLS-1$
			final String date = text.getDate();
			if (date != null && !date.equals("")) { //$NON-NLS-1$
				prefix = date + ' ' + prefix;
			}
			range.length = prefix.length();
			range.foreground = user.equals(view.userdata) ? meColor : otherColor;
			textoutput.append(prefix);
			textoutput.setStyleRange(range);
			textoutput.append(text.getText());
		} else {
			String content = text.getText();
			final String date = text.getDate();
			if (date != null && !date.equals("")) { //$NON-NLS-1$
				content = date + ' ' + content;
			}
			range.length = content.length();
			range.foreground = otherColor;
			textoutput.append(content);
		}

		if (!text.isNoCRLF()) {
			textoutput.append(Text.DELIMITER);
		}

		// scroll to end
		final String t = textoutput.getText();
		if (t != null) {
			textoutput.setSelection(t.length());
		}
	}

	protected void clearInput() {
		textinput.setText(""); //$NON-NLS-1$
	}

	private void contributeToActionBars() {
		final IActionBars bars = this.view.view.getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		// fillLocalToolBar(bars.getToolBarManager());
		// bars.getToolBarManager().markDirty();

	}

	protected void copyFileLocally(String inputFile, String outputFile) throws IOException {
		final File aFile = new java.io.File(outputFile);
		final File dir = aFile.getParentFile();
		dir.mkdirs();
		final java.io.BufferedInputStream ins = new java.io.BufferedInputStream(new java.io.FileInputStream(inputFile));
		final byte[] buf = new byte[1024];
		final java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(new java.io.FileOutputStream(aFile));
		// Actually copy file
		while (ins.read(buf) != -1)
			bos.write(buf);
		// Close input and output streams
		ins.close();
		bos.close();
	}

	private void fillContextMenu(IMenuManager manager) {
		if (chatWindow != null) {
			manager.add(showChatWindow);
			manager.add(new Separator());
		}

		manager.add(outputCopy);
		manager.add(outputPaste);
		manager.add(outputClear);
		manager.add(new Separator());
		manager.add(outputSelectAll);
		manager.add(new Separator());
		manager.add(sendFileToGroup);
		// manager.add(sendFileToGroupAndLaunch);
		manager.add(coBrowseURL);
		manager.add(new Separator());
		manager.add(sendMessage);
		manager.add(sendShowViewRequest);
		manager.add(new Separator());
		manager.add(closeGroup);
		// other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalPullDown(IMenuManager manager) {
		if (chatWindow != null) {
			manager.add(showChatWindow);
			manager.add(new Separator());
		}

		manager.add(outputCopy);
		manager.add(outputPaste);
		manager.add(outputClear);
		manager.add(new Separator());
		manager.add(outputSelectAll);
		manager.add(new Separator());
		manager.add(sendFileToGroup);
		manager.add(coBrowseURL);
		// manager.add(startProgram);
		manager.add(appShare);
		manager.add(new Separator());
		manager.add(sendMessage);
		manager.add(sendShowViewRequest);
		manager.add(new Separator());
		manager.add(closeGroup);
	}

	private void fillTreeContextMenu(IMenuManager manager) {
		final IStructuredSelection iss = (IStructuredSelection) tableView.getSelection();
		final Object element = iss.getFirstElement();
		if (element == null || !(element instanceof IUser)) {
			fillContextMenu(manager);
		} else {
			fillTreeContextMenuUser(manager, (IUser) element);
		}
	}

	private void sendImage(final IUser toUser) {
		if (MessageDialog.openQuestion(null, MessageLoader.getString("ChatComposite.DIALOG_SCREEN_CAPTURE_TITLE"), MessageLoader.getString("ChatComposite.DIALOG_SCREEN_CAPTURE_TEXT"))) { //$NON-NLS-1$ //$NON-NLS-2$
			final Job job = new ScreenCaptureJob(getDisplay(), toUser.getID(), toUser.getNickname(), new IImageSender() {
				public void sendImage(ID targetID, ImageData imageData) {
					view.lch.sendImage(toUser.getID(), imageData);
				}
			});
			job.schedule(5000);
		}
	}

	private void fillTreeContextMenuUser(IMenuManager man, final IUser user) {
		boolean toUs = false;
		if (this.view.userdata != null) {
			if (this.view.userdata.getID().equals(user.getID())) {
				// this is us...so we have a special menu
				toUs = true;
			}
		}
		if (!toUs) {
			final Action sendImageToUser = new Action() {
				public void run() {
					sendImage(user);
				}
			};
			sendImageToUser.setText(NLS.bind(MessageLoader.getString("ChatComposite.SEND_SCREEN_CAPTURE_TEXT"), user.getNickname())); //$NON-NLS-1$
			sendImageToUser.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
			man.add(sendImageToUser);

			final Action sendFileToUser = new Action() {
				public void run() {
					sendFileToUser(user, false);
				}
			};
			sendFileToUser.setText(NLS.bind(MessageLoader.getString("ChatComposite.SEND_FILE_TEXT"), user.getNickname())); //$NON-NLS-1$
			sendFileToUser.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
			// XXX disabled
			sendFileToUser.setEnabled(false);
			man.add(sendFileToUser);

			final Action sendFileToUserAndLaunch = new Action() {
				public void run() {
					sendFileToUser(user, true);
				}
			};
			sendFileToUserAndLaunch.setText(NLS.bind(MessageLoader.getString("ChatComposite.SEND_FILE_AND_LAUNCH_TEXT"), user.getNickname())); //$NON-NLS-1$
			sendFileToUserAndLaunch.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
			// XXX disabled
			sendFileToUserAndLaunch.setEnabled(false);
			man.add(sendFileToUserAndLaunch);

			final Action coBrowseToUser = new Action() {
				public void run() {
					sendCoBrowseToUser(user);
				}
			};
			coBrowseToUser.setText(NLS.bind(MessageLoader.getString("ChatComposite.COBROWSE_TEXT"), user.getNickname())); //$NON-NLS-1$
			man.add(coBrowseToUser);
			man.add(new Separator());

			man.add(new Separator());
			final Action ringUser = new Action() {
				public void run() {
					sendRingMessageToUser(user);
				}
			};
			ringUser.setText(NLS.bind(MessageLoader.getString("ChatComposite.RING_OTHER_TEXT"), user.getNickname())); //$NON-NLS-1$
			man.add(ringUser);
			final Action sendMessageToUser = new Action() {
				public void run() {
					sendPrivateTextMsg(user);
				}
			};
			sendMessageToUser.setText(NLS.bind(MessageLoader.getString("ChatComposite.SEND_PRIVATE_MESSAGE_TEXT"), user.getNickname())); //$NON-NLS-1$
			man.add(sendMessageToUser);

			final Action sendShowViewRequest = new Action() {
				public void run() {
					sendShowViewRequest(user);
				}
			};
			sendShowViewRequest.setText(NLS.bind(MessageLoader.getString("ChatComposite.SEND_SHOW_VIEW_REQUEST_TEXT"), user.getNickname())); //$NON-NLS-1$
			man.add(sendShowViewRequest);
		} else {
			// This is a menu to us
			final Action sendMessageToUser = new Action() {
				public void run() {
					MessageDialog.openError(null, MessageLoader.getFormattedString("ChatComposite.MESSAGE_TO_TITLE", user.getNickname()), //$NON-NLS-1$ 
							MessageLoader.getFormattedString("ChatComposite.MESSAGE_TO_TEXT", user.getNickname()) //$NON-NLS-1$ 
									+ "\n\tID:  " + user.getID().getName()); //$NON-NLS-1$
				}
			};
			sendMessageToUser.setText(MessageLoader.getString("ChatComposite.MENU_SEND_MESSAGE_TO_YOURSELF_TEXT")); //$NON-NLS-1$
			man.add(sendMessageToUser);
		}
		// other plug-ins can contribute there actions here
		man.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected String[] getArgs(String aString) {
		final StringTokenizer st = new StringTokenizer(aString);
		final int argscount = st.countTokens() - 1;
		if (argscount < 1)
			return null;
		final String[] newArray = new String[argscount];
		st.nextToken();
		int i = 0;
		while (st.hasMoreTokens()) {
			newArray[i++] = st.nextToken();
		}
		return newArray;
	}

	protected String getCommand(String aString) {
		final StringTokenizer st = new StringTokenizer(aString);
		return st.nextToken();
	}

	private String getID(String title, String message, String initialValue) {
		final InputDialog id = new InputDialog(this.view.view.getSite().getShell(), title, message, initialValue, null);
		id.setBlockOnOpen(true);
		final int res = id.open();
		if (res == InputDialog.OK)
			return id.getValue();
		else
			return null;
	}

	protected void handleEnter() {
		if (textinput.getText().trim().length() > 0)
			this.view.handleTextInput(textinput.getText());

		clearInput();
		typing = false;
	}

	protected void handleKeyPressed(KeyEvent evt) {
		if (evt.character == SWT.CR) {
			handleEnter();
		} else if (evt.character == SWT.ESC && chatWindow != null) {
			chatWindow.getShell().setVisible(false);
		}
	}

	protected void handleKeyReleased(KeyEvent evt) {
	}

	private void hookContextMenu() {
		final MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		final Menu menu = menuMgr.createContextMenu(textoutput);
		textoutput.setMenu(menu);
		final ISelectionProvider selectionProvider = new ISelectionProvider() {

			public void addSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public ISelection getSelection() {
				final ISelection selection = new TextSelection(textoutput.getSelectionRange().x, textoutput.getSelectionRange().y);

				return selection;
			}

			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public void setSelection(ISelection selection) {
				if (selection instanceof ITextSelection) {
					final ITextSelection textSelection = (ITextSelection) selection;
					textoutput.setSelection(textSelection.getOffset(), textSelection.getOffset() + textSelection.getLength());
				}
			}

		};
		this.view.view.getSite().registerContextMenu(menuMgr, selectionProvider);

		final MenuManager treeMenuMgr = new MenuManager();
		treeMenuMgr.setRemoveAllWhenShown(true);
		treeMenuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillTreeContextMenu(manager);
			}
		});
		final Menu treeMenu = treeMenuMgr.createContextMenu(tableView.getControl());
		tableView.getControl().setMenu(treeMenu);
		this.view.view.getSite().registerContextMenu(treeMenuMgr, tableView);
	}

	protected Control getTreeControl() {
		return tableView.getControl();
	}

	protected Control getTextControl() {
		return textoutput;
	}

	protected void makeActions() {
		outputSelectAll = new Action() {
			public void run() {
				outputSelectAll();
			}
		};
		outputSelectAll.setText(MessageLoader.getString("LineChatClientView.contextmenu.selectall")); //$NON-NLS-1$
		outputSelectAll.setToolTipText(MessageLoader.getString("LineChatClientView.contextmenu.selectall.tooltip")); //$NON-NLS-1$
		outputSelectAll.setAccelerator(SWT.CTRL | 'A');
		outputCopy = new Action() {
			public void run() {
				outputCopy();
			}
		};
		outputCopy.setText(MessageLoader.getString("LineChatClientView.contextmenu.copy")); //$NON-NLS-1$
		outputCopy.setToolTipText(MessageLoader.getString("LineChatClientView.contextmenu.copy.tooltip")); //$NON-NLS-1$
		outputCopy.setAccelerator(SWT.CTRL | 'C');
		outputCopy.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));

		outputClear = new Action() {
			public void run() {
				outputClear();
			}
		};
		outputClear.setText(MessageLoader.getString("LineChatClientView.contextmenu.clear")); //$NON-NLS-1$
		outputClear.setToolTipText(MessageLoader.getString("LineChatClientView.contextmenu.clear.tooltip")); //$NON-NLS-1$

		outputPaste = new Action() {
			public void run() {
				outputPaste();
			}
		};
		outputPaste.setText(MessageLoader.getString("LineChatClientView.contextmenu.paste")); //$NON-NLS-1$
		outputPaste.setToolTipText(MessageLoader.getString("LineChatClientView.contextmenu.paste.tooltip")); //$NON-NLS-1$
		outputPaste.setAccelerator(SWT.CTRL | 'V');
		outputPaste.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));

		sendFileToGroup = new Action() {
			public void run() {
				sendFileToGroup(false);
			}
		};
		sendFileToGroup.setText(MessageLoader.getString("LineChatClientView.contextmenu.sendfile")); //$NON-NLS-1$
		sendFileToGroup.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
		// XXX disabled
		sendFileToGroup.setEnabled(false);
		coBrowseURL = new Action() {
			public void run() {
				sendCoBrowseToUser(null);
			}
		};
		coBrowseURL.setText(MessageLoader.getString("LineChatClientView.contextmenu.cobrowse")); //$NON-NLS-1$

		appShare = new Action() {
			public void run() {
			}
		};
		appShare.setText(MessageLoader.getString("LineChatClientView.contextmenu.appshare")); //$NON-NLS-1$
		appShare.setEnabled(Platform.getOS().equalsIgnoreCase(Platform.OS_WIN32));

		sendMessage = new Action() {
			public void run() {
				sendMessageToGroup();
			}
		};
		sendMessage.setText(MessageLoader.getString("LineChatClientView.contextmenu.sendmessage")); //$NON-NLS-1$

		// Close projectGroup
		closeGroup = new Action() {
			public void run() {
				closeProjectGroup(null);
			}
		};
		closeGroup.setText(MessageLoader.getString("LineChatClientView.contextmenu.leaveGroup")); //$NON-NLS-1$
		closeGroup.setEnabled(true);

		sendShowViewRequest = new Action() {
			public void run() {
				sendShowViewRequest(null);
			}
		};
		sendShowViewRequest.setText(MessageLoader.getString("LineChatClientView.contextmenu.sendShowViewRequest")); //$NON-NLS-1$
		sendShowViewRequest.setEnabled(true);

		if (chatWindow != null) {
			showChatWindow = new Action() {
				public void run() {
					chatWindow.open();
					if (!chatWindow.hasFocus())
						chatWindow.getShell().forceActive();
				}
			};
			showChatWindow.setText(MessageLoader.getString("ChatComposite.SHOW_CHAT_WINDOW_TEXT")); //$NON-NLS-1$
		}
	}

	protected void sendShowViewRequest(IUser touser) {
		final IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage page = ww.getActivePage();
		if (page == null)
			return;

		final ElementTreeSelectionDialog dlg = new ElementTreeSelectionDialog(getShell(), new LabelProvider() {
			private HashMap images = new HashMap();

			public Image getImage(Object element) {
				ImageDescriptor desc = null;
				if (element instanceof IViewCategory)
					desc = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
				else if (element instanceof IViewDescriptor)
					desc = ((IViewDescriptor) element).getImageDescriptor();

				if (desc == null)
					return null;

				Image image = (Image) images.get(desc);
				if (image == null) {
					image = desc.createImage();
					images.put(desc, image);
				}

				return image;
			}

			public String getText(Object element) {
				String label;
				if (element instanceof IViewCategory)
					label = ((IViewCategory) element).getLabel();
				else if (element instanceof IViewDescriptor)
					label = ((IViewDescriptor) element).getLabel();
				else
					label = super.getText(element);

				for (int i = label.indexOf('&'); i >= 0 && i < label.length() - 1; i = label.indexOf('&', i + 1))
					if (!Character.isWhitespace(label.charAt(i + 1)))
						return label.substring(0, i) + label.substring(i + 1);

				return label;
			}

			public void dispose() {
				for (final Iterator i = images.values().iterator(); i.hasNext();)
					((Image) i.next()).dispose();

				images = null;
				super.dispose();
			}
		}, new ITreeContentProvider() {
			private HashMap parents = new HashMap();

			public Object[] getChildren(Object element) {
				if (element instanceof IViewRegistry)
					return ((IViewRegistry) element).getCategories();
				else if (element instanceof IViewCategory) {
					final IViewDescriptor[] children = ((IViewCategory) element).getViews();
					for (int i = 0; i < children.length; ++i)
						parents.put(children[i], element);

					return children;
				} else
					return new Object[0];
			}

			public Object getParent(Object element) {
				if (element instanceof IViewCategory)
					return PlatformUI.getWorkbench().getViewRegistry();
				else if (element instanceof IViewDescriptor)
					return parents.get(element);
				else
					return null;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof IViewRegistry || element instanceof IViewCategory)
					return true;
				else
					return false;
			}

			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			public void dispose() {
				parents = null;
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				parents.clear();
			}
		});
		dlg.setTitle(MessageLoader.getString("LineChatClientView.contextmenu.sendShowViewRequest")); //$NON-NLS-1$
		dlg.setMessage(MessageLoader.getString("LineChatClientView.contextmenu.sendShowViewRequest.dialog.title")); //$NON-NLS-1$
		dlg.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IViewDescriptor && "org.eclipse.ui.internal.introview".equals(((IViewDescriptor) element).getId())) //$NON-NLS-1$
					return false;
				else
					return true;
			}
		});
		dlg.setComparator(new ViewerComparator());
		dlg.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				for (int i = 0; i < selection.length; ++i)
					if (!(selection[i] instanceof IViewDescriptor))
						return new Status(Status.ERROR, ClientPlugin.getDefault().getBundle().getSymbolicName(), 0, "", null); //$NON-NLS-1$

				return new Status(Status.OK, ClientPlugin.getDefault().getBundle().getSymbolicName(), 0, "", null); //$NON-NLS-1$
			}
		});
		final IViewRegistry reg = PlatformUI.getWorkbench().getViewRegistry();
		dlg.setInput(reg);
		final IDialogSettings dlgSettings = ClientPlugin.getDefault().getDialogSettings();
		final String DIALOG_SETTINGS = "SendShowViewRequestDialog"; //$NON-NLS-1$
		final String SELECTION_SETTING = "SELECTION"; //$NON-NLS-1$
		IDialogSettings section = dlgSettings.getSection(DIALOG_SETTINGS);
		if (section == null)
			section = dlgSettings.addNewSection(DIALOG_SETTINGS);
		else {
			final String[] selectedIDs = section.getArray(SELECTION_SETTING);
			if (selectedIDs != null && selectedIDs.length > 0) {
				final ArrayList list = new ArrayList(selectedIDs.length);
				for (int i = 0; i < selectedIDs.length; ++i) {
					final IViewDescriptor desc = reg.find(selectedIDs[i]);
					if (desc != null)
						list.add(desc);
				}

				dlg.setInitialElementSelections(list);
			}
		}

		dlg.open();
		if (dlg.getReturnCode() == Window.CANCEL)
			return;

		final Object[] descs = dlg.getResult();
		if (descs == null)
			return;

		final String[] selectedIDs = new String[descs.length];
		for (int i = 0; i < descs.length; ++i) {
			selectedIDs[i] = ((IViewDescriptor) descs[i]).getId();
			view.lch.sendShowView(touser, selectedIDs[i]);
		}

		section.put(SELECTION_SETTING, selectedIDs);
	}

	protected void closeProjectGroup(IUser user) {
		if (MessageDialog.openConfirm(null, MessageLoader.getString("LineChatClientView.contextmenu.closeMessageTitle"), //$NON-NLS-1$
				MessageLoader.getFormattedString("LineChatClientView.contextmenu.closeMessageMessage", this.view.name))) { //$NON-NLS-1$
			this.view.lch.chatGUIDestroy();

		}

	}

	protected void outputClear() {
		if (MessageDialog.openConfirm(null, MessageLoader.getString("ChatComposite.DIALOG_CONFIRM_CLEAR_TITLE"), MessageLoader.getString("ChatComposite.DIALOG_CONFIRM_CLEAR_TEXT"))) //$NON-NLS-1$ //$NON-NLS-2$
			textoutput.setText(""); //$NON-NLS-1$
	}

	protected void outputCopy() {
		final String t = textoutput.getSelectionText();
		if (t == null || t.length() == 0) {
			textoutput.selectAll();
		}
		textoutput.copy();
		textoutput.setSelection(textoutput.getText().length());
	}

	protected void outputPaste() {
		textinput.paste();
	}

	protected void outputSelectAll() {
		textoutput.selectAll();
	}

	protected int getChunkPreference() {
		final IPreferenceStore pstore = ClientPlugin.getDefault().getPreferenceStore();
		int chunksize = pstore.getInt(ClientPlugin.DEFAULT_FILE_TRANSFER_CHUNKTIME_NAME);
		if (chunksize <= 0) {
			chunksize = 1024;
		}
		return chunksize;
	}

	protected int getDelayPreference() {
		final IPreferenceStore pstore = ClientPlugin.getDefault().getPreferenceStore();
		int delay = pstore.getInt(ClientPlugin.DEFAULT_FILE_TRANSFER_DELAY_NAME);
		if (delay <= 0) {
			delay = 10;
		}
		return delay;
	}

	protected void readStreamAndSend(java.io.InputStream local, String fileName, Date startDate, ID target, final boolean launch) {
		try {
			final ID eclipseStageID = IDFactory.getDefault().createStringID(org.eclipse.ecf.example.collab.share.EclipseCollabSharedObject.ID);
			final java.io.BufferedInputStream ins = new java.io.BufferedInputStream(local);
			final java.io.File remoteFile = new File((new File(fileName)).getName());
			final FileTransferParams sp = new FileTransferParams(remoteFile, getChunkPreference(), getDelayPreference(), null, true, -1, null);
			final Object[] args = {view, target, ins, sp, eclipseStageID};
			// Do it
			new Thread(new Runnable() {
				public void run() {
					if (launch) {
						ChatComposite.this.view.createObject(null, org.eclipse.ecf.example.collab.share.io.EclipseFileTransferAndLaunch.class.getName(), new String[] {FileSenderUI.class.getName(), ID.class.getName(), java.io.InputStream.class.getName(), FileTransferParams.class.getName(), ID.class.getName()}, args);
					} else {
						ChatComposite.this.view.createObject(null, org.eclipse.ecf.example.collab.share.io.EclipseFileTransfer.class.getName(), new String[] {FileSenderUI.class.getName(), ID.class.getName(), java.io.InputStream.class.getName(), FileTransferParams.class.getName(), ID.class.getName()}, args);
					}
				}
			}, "FileRepObject creator").start(); //$NON-NLS-1$
		} catch (final Exception e) {
			if (this.view.lch != null)
				this.view.lch.chatException(e, "readStreamAndSend()"); //$NON-NLS-1$
		}

	}

	protected void sendCoBrowseToUser(IUser user) {
		String res = null;
		ID userID = null;
		if (user != null) {
			res = getID(NLS.bind(MessageLoader.getString("ChatComposite.COBROWSE_TITLE"), user.getNickname()), MessageLoader.getString("ChatComposite.COBROWSE_URL_TEXT"), "http://"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			userID = user.getID();
		} else {
			res = getID(MessageLoader.getString("ChatComposite.DIALOG_COBROWSE_TITLE"), MessageLoader.getString("ChatComposite.DIALOG_COBROWSE_TEXT"), MessageLoader.getString("ChatComposite.DIALOG_COBROWSE_HTTPPREFIX")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		if (res != null) {
			final Object[] args = {userID, res};
			// Do it
			this.view.createObject(null, LineChatClientView.SHOWURLCLASSNAME, LineChatClientView.SHOWURLARGTYPES, args);
		}
	}

	protected void sendFile(String pathName, final String fileName, Date startDate, ID target, boolean launch) {

		try {
			copyFileLocally(pathName, fileName);
		} catch (final IOException e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null, MessageLoader.getString("ChatComposite.DIALOG_COPY_ERROR_TITLE"), MessageLoader.getString("ChatComposite.DIALOG_COPY_ERROR_TEXT") + e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			});
			if (this.view.lch != null)
				this.view.lch.chatException(e, "sendFile(" + pathName + "/" + fileName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}

		java.io.FileInputStream ins = null;
		try {
			ins = new java.io.FileInputStream(pathName);
		} catch (final java.io.FileNotFoundException e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(null, MessageLoader.getString("ChatComposite.DIALOG_FILE_OPEN_ERROR_TITLE"), NLS.bind(MessageLoader.getString("ChatComposite.EXCEPTION_FILE_NOT_FOUND_TEXT"), fileName, e.getLocalizedMessage())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			});
			if (this.view.lch != null)
				this.view.lch.chatException(e, NLS.bind(MessageLoader.getString("ChatComposite.EXCEPTION_FILE_NOT_FOUND_TEXT1"), fileName)); //$NON-NLS-1$
		}
		readStreamAndSend(ins, fileName, startDate, target, launch);
	}

	protected void sendFileToGroup(boolean launch) {
		final FileDialog fd = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
		fd.setFilterPath(System.getProperty("user.dir")); //$NON-NLS-1$
		fd.setText(MessageLoader.getString("ChatComposite.SELECT_FILE_FOR_GROUP_TEXT")); //$NON-NLS-1$
		final String res = fd.open();
		if (res != null) {
			final java.io.File selected = new java.io.File(res);
			final File localTarget = new File(this.view.downloaddir, selected.getName());
			sendFile(selected.getPath(), localTarget.getAbsolutePath(), null, null, launch);
		}
	}

	protected void sendFileToUser(IUser user, boolean launch) {
		final FileDialog fd = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
		fd.setFilterPath(System.getProperty("user.dir")); //$NON-NLS-1$
		fd.setText(NLS.bind(MessageLoader.getString("ChatComposite.SELECt_FILE_TITLE"), user.getNickname())); //$NON-NLS-1$
		final String res = fd.open();
		if (res != null) {
			final java.io.File selected = new java.io.File(res);
			final File localTarget = new File(this.view.downloaddir, selected.getName());
			sendFile(selected.getPath(), localTarget.getAbsolutePath(), null, user.getID(), launch);
		}
	}

	protected void sendMessageToGroup() {
		final String res = getID(MessageLoader.getString("ChatComposite.MESSAGE_TO_GROUP_TITLE"), MessageLoader.getString("ChatComposite.MESSAGE_TO_GROUP_TEXT"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (res != null & !res.equals("")) { //$NON-NLS-1$
			final String[] args = {res, this.view.userdata.getNickname()};
			this.view.createObject(null, LineChatClientView.MESSAGECLASSNAME, args);
		}
	}

	protected void sendPrivateTextMsg(IUser data) {
		if (this.view.lch != null) {
			IPresenceContainerAdapter ipca = this.view.lch.getPresenceContainer();
			MessagesView messagesView = this.view.lch.findMessagesView();
			if (messagesView != null) {
				IChatManager chatManager = ipca.getChatManager();
				messagesView.openTab(chatManager.getChatMessageSender(), chatManager.getTypingMessageSender(), this.view.lch.getContainerID(), data.getID());
			}
		}
	}

	protected void sendRepObjectToGroup(IUser user) {
		final String result = getID(MessageLoader.getString("ChatComposite.SEND_REPLICATED_OBJECT_TITLE"), MessageLoader.getString("ChatComposite.SEND_REPLICATED_OBJECT_TEXT"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (result != null && !result.equals("")) { //$NON-NLS-1$
			this.view.createObject(null, getCommand(result), getArgs(result));
		}
	}

	protected void sendRepObjectToServer() {
		// XXX TODO
	}

	protected void sendRingMessageToUser(IUser data) {
		String res = null;
		if (this.view.lch != null) {
			if (data != null) {
				res = getID(MessageLoader.getFormattedString("ChatComposite.RING_TITLE", data.getNickname()), MessageLoader.getString("ChatComposite.RING_MESSAGE_TEXT"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				res = getID(MessageLoader.getString("ChatComposite.RING_GROUP_TITLE"), MessageLoader.getString("ChatComposite.RING_MESSAGE_TEXT"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			if (res != null)
				this.view.lch.sendRingMessageToUser(data, res);
		}
	}

	protected void startProgram(IUser ud) {
		String res = null;
		ID receiver = null;
		if (ud == null) {
			res = getID(MessageLoader.getString("ChatComposite.START_PROGRAM_GROUP_TITLE"), MessageLoader.getString("ChatComposite.START_PROGRAM_GROUP_TEXT"), ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			res = getID(MessageLoader.getFormattedString("ChatComposite.START_PROGRAM_TITLE", ud.getNickname()), MessageLoader.getFormattedString("ChatComposite.START_PROGRAM_TEXT", ud.getNickname()) + ":", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			receiver = ud.getID();
		}
		if (res != null)
			this.view.runProgram(receiver, res, null);
	}

	protected void initializeDropTargets() {
		chatDropTarget = new ChatDropTarget(view, textoutput, this);
		treeDropTarget = new TreeDropTarget(view, tableView.getControl(), this);
	}

	private Color colorFromRGBString(String rgb) {
		if (rgb == null || rgb.equals("")) { //$NON-NLS-1$
			return new Color(getShell().getDisplay(), 0, 0, 0);
		} else {
			final String[] vals = StringUtils.split(rgb, ","); //$NON-NLS-1$
			return new Color(getShell().getDisplay(), Integer.parseInt(vals[0]), Integer.parseInt(vals[1]), Integer.parseInt(vals[2]));
		}
	}

	private class ColorPropertyChangeListener implements org.eclipse.core.runtime.Preferences.IPropertyChangeListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
		 */
		public void propertyChange(PropertyChangeEvent event) {
			meColor.dispose();
			otherColor.dispose();
			systemColor.dispose();
			meColor = colorFromRGBString(ClientPlugin.getDefault().getPluginPreferences().getString(ClientPlugin.PREF_ME_TEXT_COLOR));
			otherColor = colorFromRGBString(ClientPlugin.getDefault().getPluginPreferences().getString(ClientPlugin.PREF_OTHER_TEXT_COLOR));
			systemColor = colorFromRGBString(ClientPlugin.getDefault().getPluginPreferences().getString(ClientPlugin.PREF_SYSTEM_TEXT_COLOR));
		}

	}

	private class FontPropertyChangeListener implements org.eclipse.core.runtime.Preferences.IPropertyChangeListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
		 */
		public void propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
			if (event.getProperty().equals(ClientPlugin.PREF_CHAT_FONT)) {
				final String fontName = ClientPlugin.getDefault().getPluginPreferences().getString(ClientPlugin.PREF_CHAT_FONT);
				if (!(fontName == null) && !(fontName.equals(""))) { //$NON-NLS-1$
					final FontRegistry fr = ClientPlugin.getDefault().getFontRegistry();
					final FontData[] newFont = {new FontData(fontName)};

					fr.put(CHAT_OUTPUT_FONT, newFont);
					textoutput.setFont(fr.get(CHAT_OUTPUT_FONT));
				}
			}
		}

	}

}