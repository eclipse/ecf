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
package org.eclipse.ecf.example.collab.ui;

import java.util.Iterator;
import java.util.List;
import org.eclipse.ecf.core.SharedObjectContainerDescription;
import org.eclipse.ecf.core.SharedObjectContainerFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class JoinGroupWizardPage extends WizardPage {
    protected static final String PAGE_DESCRIPTION = "Join ECF Collaboration Group";
    private static final String JOINGROUP_FIELDNAME = "Group ID:";
    private static final String NICKNAME_FIELDNAME = "Nickname:";
    private static final String ECF_DEFAULT_URL = "ecftcp://localhost:3282/server";
    protected static final String ECF_TEMPLATE_URL = "ecftcp://<machinename>:<port>/<name>";
    protected static final String PAGE_TITLE = "Join ECF Group";
    
    public JoinGroupWizardPage() {
        super("wizardPage");
        setTitle(PAGE_TITLE);
        setDescription(PAGE_DESCRIPTION);
    }

    protected Text nickname_text;
    protected Text joingroup_text;
    protected Combo combo;
    protected Text password_text;
    protected List containerDescriptions;
    
    protected void fillCombo() {
        combo.add("Default");
        containerDescriptions = SharedObjectContainerFactory.getDescriptions();
        for(Iterator i=containerDescriptions.iterator(); i.hasNext(); ) {
            SharedObjectContainerDescription desc = (SharedObjectContainerDescription) i.next();
            String name = desc.getName();
            String description = desc.getDescription();
            if (description != null && !description.equals("")) {
                name = name + " - " + description;
            }
            combo.add(name);
        }
        // Set to default
        combo.select(0);
    }
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);
        //
        setControl(container);

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("ECF Provider:");

        combo = new Combo(container, SWT.NONE);
        final GridData gridData_1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        combo.setLayoutData(gridData_1);
        fillCombo();

        final Label label_2 = new Label(container, SWT.NONE);

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText(ECF_TEMPLATE_URL);

        final Label label = new Label(container, SWT.NONE);
        label.setText(JOINGROUP_FIELDNAME);

        joingroup_text = new Text(container, SWT.BORDER);
        joingroup_text.setText(ECF_DEFAULT_URL);
        final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = 140;
        joingroup_text.setLayoutData(gridData);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText(NICKNAME_FIELDNAME);

        nickname_text = new Text(container, SWT.BORDER);
        final GridData nickname = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        nickname_text.setLayoutData(nickname);
        nickname_text.setText(System.getProperty("user.name"));

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setText("Password:");

        password_text = new Text(container, SWT.BORDER);
        password_text.setEchoChar('*');
        password_text.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    }
    
    public String getJoinGroupText() {
        return joingroup_text.getText().trim();
    }
    
    public String getNicknameText() {
        return nickname_text.getText().trim();
    }
    
    public String getPasswordText() {
        return password_text.getText();
    }
    
    public String getContainerType() {
        int index = combo.getSelectionIndex();
        if (index == 0) return null;
        else {
            SharedObjectContainerDescription desc = (SharedObjectContainerDescription) containerDescriptions.get(index-1);
            return desc.getName();
        }
    }
}
