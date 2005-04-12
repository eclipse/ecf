package org.eclipse.ecf.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class AuthorizeRequest extends Dialog {

	private Label target_username;
	private Label requester_userid;
	private static final int REFUSE_ID = IDialogConstants.CLIENT_ID + 3;
	private static final int AUTHORIZE_AND_ADD = IDialogConstants.CLIENT_ID + 2;
	private static final int AUTHORIZE_ID = IDialogConstants.CLIENT_ID + 1;
	
	public AuthorizeRequest(Shell parentShell) {
		super(parentShell);
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);

		final Composite composite = new Composite(container, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.numColumns = 4;
		composite.setLayout(gridLayout);
		final GridData gridData_1 = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL);
		gridData_1.heightHint = 22;
		gridData_1.verticalSpan = 2;
		composite.setLayoutData(gridData_1);

		requester_userid = new Label(composite, SWT.NONE);
		final GridData gridData_2 = new GridData();
		requester_userid.setLayoutData(gridData_2);
		requester_userid.setText("slewis@cerf.composent.com");

		final Label label_4 = new Label(composite, SWT.NONE);
		label_4.setText(" would like to add ");

		target_username = new Label(composite, SWT.NONE);
		target_username.setLayoutData(new GridData());
		target_username.setText("slewis@composent.com");

		final Label label_5 = new Label(composite, SWT.NONE);
		label_5.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label_5.setText(" to their buddy list");

		final Composite composite_1 = new Composite(container, SWT.NONE);
		composite_1.setLayout(new FillLayout(SWT.VERTICAL));
		final GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.heightHint = 82;
		composite_1.setLayoutData(gridData);

		final Label label = new Label(composite_1, SWT.WRAP);
		label.setText("\nYou can choose to authorize and also add them to your own buddy list, authorize them without adding them to your buddy list, or refuse to authorize");

		final Label label_3 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		final GridData gridData_3 = new GridData(GridData.FILL_HORIZONTAL);
		gridData_3.heightHint = 6;
		gridData_3.widthHint = 469;
		label_3.setLayoutData(gridData_3);
		//
		return container;
	}

	protected void createButtonsForButtonBar(Composite parent) {

		createButton(parent, AUTHORIZE_AND_ADD, "Authorize and Add Buddy", false);

		createButton(parent, AUTHORIZE_ID, "Authorize Only", false);

		createButton(parent, REFUSE_ID, "Refuse", true);
	}

	protected Point getInitialSize() {
		return new Point(507, 196);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("ECF Authorization Request");
	}

}
