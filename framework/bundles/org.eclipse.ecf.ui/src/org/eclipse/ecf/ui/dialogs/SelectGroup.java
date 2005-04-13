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

public class SelectGroup extends Dialog {

	private Label username;
	private Combo groups;
	
	private String user = null;
	private String [] existing = null;
	
	private int result;
	
	public SelectGroup(Shell parentShell,String username, String [] existing) {
		super(parentShell);
		this.user = username;
		this.existing = existing;
	}
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		container.setLayout(gridLayout);

		final Composite composite_1 = new Composite(container, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.horizontalSpacing = 0;
		gridLayout_1.numColumns = 2;
		composite_1.setLayout(gridLayout_1);

		final Label label = new Label(composite_1, SWT.NONE);
		label.setText("Please select a group for");

		username = new Label(composite_1, SWT.NONE);
		username.setText(" user");

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());

		groups = new Combo(composite, SWT.NONE);
		groups.setToolTipText("Select group or enter new group");
		final GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
		gridData.widthHint = 141;
		groups.setLayoutData(gridData);
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
		return groups.getText();
	}
	protected Point getInitialSize() {
		return new Point(292, 141);
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select Group");
	}

}
