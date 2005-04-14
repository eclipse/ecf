package org.eclipse.ecf.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddBuddyDialog extends Dialog {

	private Text usertext;
	private Text nicknametext;
	private Combo groups;
	
	private String user = null;
	private String [] existing = null;
	private int selectedGroup = -1;
	
	private int result;
	
	private String userresult = null;
	private String nicknameresult = null;
	private String groupsresult = null;
	
	public AddBuddyDialog(Shell parentShell,String username, String [] existingGroups, int selectedGroup) {
		super(parentShell);
		this.user = username;
		this.existing = existingGroups;
		this.selectedGroup = selectedGroup;
	}
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		container.setLayout(gridLayout);

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		composite.setLayout(gridLayout_2);

		final Label label_3 = new Label(composite, SWT.NONE);
		label_3.setText("User ID:");

		usertext = new Text(composite, SWT.BORDER);
		usertext.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		if (user != null) {
			usertext.setText(user);
			usertext.setEnabled(false);
		}

		final Label label_1 = new Label(composite, SWT.NONE);
		label_1.setText("Group:");

		groups = new Combo(composite, SWT.NONE);
		groups.setToolTipText("Select group or enter new group");
		final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 141;
		groups.setLayoutData(gridData);
		if (existing != null) {
			for(int i=0; i < existing.length; i++) {
				groups.add(existing[i]);
			}
			if (selectedGroup != -1) groups.select(selectedGroup);
			else groups.select(0);
		}

		final Label label_2 = new Label(composite, SWT.NONE);
		label_2.setText("Nickname:");

		nicknametext = new Text(composite, SWT.BORDER);
		final GridData gridData_1 = new GridData(GridData.FILL_HORIZONTAL);
		gridData_1.widthHint = 192;
		nicknametext.setLayoutData(gridData_1);
		//
		return container;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}
	public String getGroup() {
		return groupsresult;
	}
	public String getUser() {
		return userresult;
	}
	public String getNickname() {
		return nicknameresult;
	}
	protected Point getInitialSize() {
		return new Point(302, 168);
	}
	public void buttonPressed(int button) {
		result = button;
		userresult = usertext.getText();
		nicknameresult = nicknametext.getText();
		groupsresult = groups.getText();
		close();
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add Buddy");
	}

}
