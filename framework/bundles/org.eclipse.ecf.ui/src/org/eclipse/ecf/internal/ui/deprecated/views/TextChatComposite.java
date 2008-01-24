/****************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/

package org.eclipse.ecf.internal.ui.deprecated.views;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ecf.core.user.IUser;
import org.eclipse.ecf.internal.ui.Activator;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.part.ViewPart;

public class TextChatComposite extends Composite {

	protected static final int DEFAULT_INPUT_HEIGHT = 25;

	protected static final int DEFAULT_INPUT_SEPARATOR = 5;

	protected String TEXT_INPUT_INIT = "<input chat text here>";

	protected Color meColor = null;

	protected Color otherColor = null;

	protected Color systemColor = null;

	protected StyledText styledText;

	// protected TextViewer textoutput;

	protected Text textinput;

	protected int[] sashWeights = new int[] {7, 2};

	protected boolean isTyping;

	protected String initText;

	protected ILocalInputHandler inputHandler;

	SimpleDateFormat df = new SimpleDateFormat("hh:mm a");

	protected IUser localUser;

	protected IUser remoteUser;

	protected boolean showTimestamp = true;

	private Action outputClear = null;

	private Action outputCopy = null;

	private Action outputPaste = null;

	private Action outputSelectAll = null;

	private ViewPart view = null;

	public TextChatComposite(ViewPart view, Composite parent, int style, String initText, ILocalInputHandler handler, IUser localUser, IUser remoteUser) {
		super(parent, style);

		this.view = view;
		this.initText = initText;
		this.inputHandler = handler;

		this.localUser = localUser;
		this.remoteUser = remoteUser;

		this.meColor = new Color(getShell().getDisplay(), 23, 135, 65);
		this.otherColor = new Color(getShell().getDisplay(), 65, 13, 165);
		this.systemColor = new Color(getShell().getDisplay(), 123, 135, 165);

		this.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				meColor.dispose();
				otherColor.dispose();
				systemColor.dispose();
			}

		});

		setLayout(new GridLayout());
		SashForm sash = new SashForm(this, SWT.VERTICAL | SWT.SMOOTH);
		sash.setLayoutData(new GridData(GridData.FILL_BOTH));

		styledText = createStyledTextWidget(sash);
		styledText.setEditable(false);

		// Setup text input
		textinput = new Text(sash, SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		textinput.setText(TEXT_INPUT_INIT);

		textinput.selectAll();
		textinput.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent evt) {
				handleKeyPressed(evt);
			}

			public void keyReleased(KeyEvent evt) {
			}
		});
		textinput.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				String t = textinput.getText();
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
				String t = textinput.getText();
				if (t.equals(TEXT_INPUT_INIT)) {
					textinput.selectAll();
				}
			}
		});
		textinput.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (isTyping && textinput.getText().trim().length() == 0)
					isTyping = false;
				else if (!isTyping) {
					isTyping = true;
					sendStartedTyping();
				}
			}
		});

		sash.setWeights(sashWeights);

		makeActions();
		hookContextMenu();

		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(Activator.PREF_DISPLAY_TIMESTAMP)) {
					showTimestamp = ((Boolean) event.getNewValue()).booleanValue();
				}
			}

		});

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

	private void makeActions() {
		outputSelectAll = new Action() {
			public void run() {
				outputSelectAll();
			}
		};
		outputSelectAll.setText("Select All");
		outputSelectAll.setAccelerator(SWT.CTRL | 'A');
		outputCopy = new Action() {
			public void run() {
				outputCopy();
			}
		};
		outputCopy.setText("Copy");
		outputCopy.setAccelerator(SWT.CTRL | 'C');
		outputCopy.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));

		outputClear = new Action() {
			public void run() {
				outputClear();
			}
		};
		outputClear.setText("Clear");

		outputPaste = new Action() {
			public void run() {
				outputPaste();
			}
		};
		outputPaste.setText("Paste");
		outputCopy.setAccelerator(SWT.CTRL | 'V');
		outputPaste.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));

	}

	protected void outputClear() {
		if (MessageDialog.openConfirm(null, "Confirm Clear Text Output", "Are you sure you want to clear output?"))
			styledText.setText("");
	}

	protected void outputCopy() {
		String t = styledText.getSelectionText();
		if (t == null || t.length() == 0) {
			styledText.selectAll();
		}
		styledText.copy();
		styledText.setSelection(styledText.getText().length());
	}

	protected void outputPaste() {
		textinput.paste();
	}

	protected void outputSelectAll() {
		styledText.selectAll();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(styledText);
		styledText.setMenu(menu);
		ISelectionProvider selectionProvider = new ISelectionProvider() {

			public void addSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public ISelection getSelection() {
				ISelection selection = new TextSelection(styledText.getSelectionRange().x, styledText.getSelectionRange().y);

				return selection;
			}

			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}

			public void setSelection(ISelection selection) {
				if (selection instanceof ITextSelection) {
					ITextSelection textSelection = (ITextSelection) selection;
					styledText.setSelection(textSelection.getOffset(), textSelection.getOffset() + textSelection.getLength());
				}
			}

		};
		view.getSite().registerContextMenu(menuMgr, selectionProvider);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(outputCopy);
		manager.add(outputPaste);
		manager.add(outputClear);
		manager.add(new Separator());
		manager.add(outputSelectAll);
		manager.add(new Separator());
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public void setLocalUser(IUser newUser) {
		this.localUser = newUser;
	}

	public void setRemoteUser(IUser remoteUser) {
		this.remoteUser = remoteUser;
	}

	public IUser getRemoteUser() {
		return this.remoteUser;
	}

	protected String getCurrentDateTime() {
		StringBuffer sb = new StringBuffer("(");
		sb.append(df.format(new Date())).append(") ");
		return sb.toString();
	}

	private String createLineWithTimestamp(String line) {
		if (showTimestamp)
			return getCurrentDateTime() + line;
		return line;
	}

	public IUser getLocalUser() {
		return localUser;
	}

	public void appendText(ChatLine text) {
		StyledText st = styledText;

		if (text == null || st == null)
			return;

		int startRange = st.getText().length();
		StringBuffer sb = new StringBuffer();

		if (text.getOriginator() != null) {
			sb.append(createLineWithTimestamp(text.getOriginator().getName() + ": "));
			StyleRange sr = new StyleRange();
			sr.start = startRange;
			sr.length = sb.length();
			IUser lu = getLocalUser();
			if (lu != null && lu.getID().equals(text.getOriginator().getID())) {
				sr.foreground = meColor;
			} else {
				sr.foreground = otherColor;
			}
			st.append(sb.toString());
			st.setStyleRange(sr);
		}

		int beforeMessageIndex = st.getText().length();

		st.append(text.getText().trim());

		if (text.getOriginator() == null) {
			StyleRange sr = new StyleRange();
			sr.start = beforeMessageIndex;
			sr.length = text.getText().length();
			sr.foreground = systemColor;
			st.setStyleRange(sr);
		}

		if (!text.isNoCRLF()) {
			st.append("\n");
		}

		String t = st.getText();
		if (t == null)
			return;
		st.setSelection(t.length());
	}

	protected void handleKeyPressed(KeyEvent evt) {
		if (evt.character == SWT.CR) {
			handleEnter();
			evt.doit = false;
		}
	}

	protected void handleEnter() {
		String text = textinput.getText().trim();
		if (text != null)
			sendTextLineInput(text);

		clearInput();
		isTyping = false;
	}

	protected void clearInput() {
		textinput.setText("");
	}

	protected void sendTextLineInput(String text) {
		if (inputHandler != null) {
			IUser lu = getLocalUser();
			IUser ru = getRemoteUser();
			if (lu != null && ru != null) {
				inputHandler.inputText(ru.getID(), text);
				appendText(new ChatLine(text, lu));
			} else {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 100, "Null localUser or remoteUser for textchatcomposite", new NullPointerException()));
			}
		} else {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 100, "No inputhandler available for textchatcomposite", new NullPointerException()));
		}
	}

	protected void sendStartedTyping() {
		if (inputHandler != null) {
			IUser lu = getLocalUser();
			IUser ru = getRemoteUser();
			if (lu != null && ru != null) {
				inputHandler.startTyping(ru.getID());
			} else {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 100, "Null localUser or remoteUser for textchatcomposite", new NullPointerException()));
			}
		} else {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 100, "No inputhandler available for textchatcomposite", new NullPointerException()));
		}
	}

	protected String getShellName() {
		return "org.eclipse.ecf.ui.views.TextChatComposite";
	}

	public void dispose() {
		super.dispose();
	}

	protected void setDisposed() {
		textinput.setEnabled(false);
	}

	protected void checkSubclass() {
	}
}
