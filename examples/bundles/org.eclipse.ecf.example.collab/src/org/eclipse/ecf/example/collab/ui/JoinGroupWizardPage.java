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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.ecf.core.SharedObjectContainerDescription;
import org.eclipse.ecf.core.SharedObjectContainerFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
    
    protected static final String DEFAULT_CLIENT = "org.eclipse.ecf.provider.generic.Client";
    
    public JoinGroupWizardPage() {
        super("wizardPage");
        setTitle(PAGE_TITLE);
        setDescription(PAGE_DESCRIPTION);
    }

    protected String template_url = ECF_TEMPLATE_URL;
    protected String default_url = ECF_DEFAULT_URL;
    protected boolean showPassword = true;
    
    protected Label password_label;
    protected Text nickname_text;
    protected Text joingroup_text;
    protected Label example_label;
    protected Combo combo;
    protected Text password_text;
    protected List containerDescriptions = new ArrayList();
    
    protected void modifyUI(Map props) {
        if (props != null) {
            String usePassword = (String) props.get("usepassword");
            String examplegroupid = (String) props.get("examplegroupid");
            String defaultgroupid = (String) props.get("defaultgroupid");
            // turn off password unless used
            if (usePassword != null){
                password_label.setVisible(true);
                password_text.setVisible(true);
            } else {
                password_label.setVisible(false);
                password_text.setVisible(false);                        
            }
            // set examplegroupid text
            example_label.setText((examplegroupid != null)?examplegroupid:"");
            joingroup_text.setText((defaultgroupid != null)?defaultgroupid:"");
        }
    }
    protected void fillCombo() {
        List rawDescriptions = SharedObjectContainerFactory.getDescriptions();
        int index = 0;
        int def = 0;
        Map defProps = null;
        for(Iterator i=rawDescriptions.iterator(); i.hasNext(); ) {
            final SharedObjectContainerDescription desc = (SharedObjectContainerDescription) i.next();
            String name = desc.getName();
            String description = desc.getDescription();
            Map props = desc.getProperties();
            String isClient = (String) props.get("isClient");
            if (isClient != null) {
                if (DEFAULT_CLIENT.equals(name)) {
                    def = index;
                    defProps = props;
                }
                combo.add(description+" - "+name,index);
                combo.setData(""+index,desc);
                containerDescriptions.add(desc);
                index++;
            }
        }
        combo.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                SharedObjectContainerDescription desc = (SharedObjectContainerDescription) combo.getData(combo.getSelectionIndex()+"");
                Map props = desc.getProperties();
                modifyUI(props);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        // Set to default
        if (combo.getItemCount() > 0) combo.select(def);
        if (defProps != null) modifyUI(defProps);
    }
    
    //protected 
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);
        //
        setControl(container);

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("Provider:");

        combo = new Combo(container, SWT.NONE);
        final GridData gridData_1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        combo.setLayoutData(gridData_1);

        final Label label_2 = new Label(container, SWT.NONE);

        example_label = new Label(container, SWT.NONE);
        example_label.setText(template_url);

        final Label label = new Label(container, SWT.NONE);
        label.setText(JOINGROUP_FIELDNAME);

        joingroup_text = new Text(container, SWT.BORDER);
        joingroup_text.setText(default_url);
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

        password_label = new Label(container, SWT.NONE);
        password_label.setText("Password:");

        password_text = new Text(container, SWT.BORDER);
        password_text.setEchoChar('*');
        password_text.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        if (!showPassword) {
            password_text.setVisible(false);
            password_label.setVisible(false);
        }
        fillCombo();
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
        if (index == -1) return null;
        else {
            SharedObjectContainerDescription desc = (SharedObjectContainerDescription) containerDescriptions.get(index);
            return desc.getName();
        }
    }
}
