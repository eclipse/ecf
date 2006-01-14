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

import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class JoinGroupWizardPage extends WizardPage {
    
    protected static final String CLASSNAME = JoinGroupWizardPage.class.getName();
    
    protected static final String USER_NAME_SYSTEM_PROPERTY = "user.name";
    
    protected static final String ISSERVER_PROP_NAME = CLASSNAME+".isServer";
    protected static final String DEFAULTGROUPID_PROP_NAME = CLASSNAME+".defaultgroupid";
    protected static final String EXAMPLEGROUPID_PROP_NAME = CLASSNAME+".examplegroupid";
    protected static final String USEPASSWORD_PROP_NAME = CLASSNAME+".usepassword";
    protected static final String USENICKNAME_PROP_NAME = CLASSNAME+".usenickname";
    protected static final String URLPREFIX_NAME = CLASSNAME+".urlprefix";
    protected static final String GROUPIDLABEL_PROP_NAME = CLASSNAME+".groupIDLabel";
	protected static final String NAMESPACE_PROP_NAME = CLASSNAME+".namespace";
    
    protected static final String PAGE_DESCRIPTION = "Select protocol provider, complete account info and login";
    protected static final String JOINGROUP_FIELDNAME = "Group ID:";
    protected static final String NICKNAME_FIELDNAME = "Nickname:";
    protected static final String ECF_DEFAULT_URL = "ecftcp://localhost:3282/server";
    protected static final String ECF_TEMPLATE_URL = "<protocol>://<machinename>:<port>/<servicename>";
    protected static final String PAGE_TITLE = "Connect with ECF";
    
    protected static final String DEFAULT_CLIENT = "ecf.generic.client";
    
    private static final String DIALOG_SETTINGS = CLASSNAME;
    
    public JoinGroupWizardPage() {
        super("wizardPage");
        setTitle(PAGE_TITLE);
        setDescription(PAGE_DESCRIPTION);
    }

    protected String template_url = ECF_TEMPLATE_URL;
    protected String default_url = ECF_DEFAULT_URL;
    protected boolean showPassword = true;
    protected boolean showNickname = true;
    
    protected Label password_label;
    protected Text nickname_text;
    protected Label nickname_label;
    protected Text joingroup_text;
    protected Label example_label;
    protected Combo combo;
    protected Text password_text;
    protected List containerDescriptions = new ArrayList();
    protected String urlPrefix = "";
    protected Label groupIDLabel;
	
	protected String namespace = null;
	
    protected void modifyUI(Map props) {
        if (props != null) {
            String usePassword = (String) props.get(USEPASSWORD_PROP_NAME);
            String examplegroupid = (String) props.get(EXAMPLEGROUPID_PROP_NAME);
            String defaultgroupid = (String) props.get(DEFAULTGROUPID_PROP_NAME);
            String useNickname = (String) props.get(USENICKNAME_PROP_NAME);
            urlPrefix = (String) props.get(URLPREFIX_NAME); 
			namespace = (String) props.get(NAMESPACE_PROP_NAME);
            if (urlPrefix == null) urlPrefix = "";
            String groupLabel = (String) props.get(GROUPIDLABEL_PROP_NAME);
            if (groupLabel != null) {
                groupIDLabel.setText(groupLabel);
            } else {
                groupIDLabel.setText(JOINGROUP_FIELDNAME);
            }
            // turn off password unless used
            if (usePassword != null){
                password_label.setVisible(true);
                password_text.setVisible(true);
            } else {
                password_label.setVisible(false);
                password_text.setVisible(false);                        
            }
            // turn off nickname unless used
            if (useNickname != null){
                nickname_label.setVisible(true);
                nickname_text.setVisible(true);
            } else {
                nickname_label.setVisible(false);
                nickname_text.setVisible(false);                        
            }
            // set examplegroupid text
            example_label.setText((examplegroupid != null)?examplegroupid:"");
            joingroup_text.setText((defaultgroupid != null)?defaultgroupid:"");
        }
    }
    protected void fillCombo() {
        List rawDescriptions = ContainerFactory.getDefault().getDescriptions();
        int index = 0;
        int def = 0;
        Map defProps = null;
        for(Iterator i=rawDescriptions.iterator(); i.hasNext(); ) {
            final ContainerTypeDescription desc = (ContainerTypeDescription) i.next();
            String name = desc.getName();
            String description = desc.getDescription();
            Map props = desc.getProperties();
            String isServer = (String) props.get(ISSERVER_PROP_NAME);
            if (isServer == null || !isServer.equalsIgnoreCase("true")) {
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
                ContainerTypeDescription desc = (ContainerTypeDescription) combo.getData(combo.getSelectionIndex()+"");
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
    
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);
        //
        setControl(container);
        
        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("Provider:");
        final GridData gridData_0 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        label_4.setLayoutData(gridData_0);
        
        combo = new Combo(container, SWT.READ_ONLY);
        final GridData gridData_1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        combo.setLayoutData(gridData_1);

        Label l5 = new Label(container, SWT.NONE);
        l5.setText("");
        example_label = new Label(container, SWT.NONE);
        example_label.setText(template_url);
        
        groupIDLabel = new Label(container, SWT.NONE);
        groupIDLabel.setText(JOINGROUP_FIELDNAME);

        joingroup_text = new Text(container, SWT.BORDER);
    	joingroup_text.setText(default_url);
        final GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gridData.widthHint = 140;
        joingroup_text.setLayoutData(gridData);
        joingroup_text.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                String t = joingroup_text.getText();
                joingroup_text.setSelection(t.length());
            }

            public void focusLost(FocusEvent e) {
            }
        });

        nickname_label = new Label(container, SWT.NONE);
        nickname_label.setLayoutData(new GridData());
        nickname_label.setText(NICKNAME_FIELDNAME);

        nickname_text = new Text(container, SWT.BORDER);
        final GridData nickname = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        nickname_text.setLayoutData(nickname);
    	nickname_text.setText(System.getProperty(USER_NAME_SYSTEM_PROPERTY));
        nickname_text.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                    nickname_text.selectAll();
            }

            public void focusLost(FocusEvent e) {
            }
        });
        if (!showNickname) {
            nickname_text.setVisible(false);
            nickname_label.setVisible(false);
        }

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
        restoreDialogSettings();
    }
    
    private void restoreDialogSettings() {
        IDialogSettings dialogSettings = getDialogSettings();
        if (dialogSettings != null) {
        	IDialogSettings pageSettings = dialogSettings.getSection(DIALOG_SETTINGS);
        	if (pageSettings != null) {
            	String strVal = pageSettings.get("provider");
            	if (strVal != null) {
            		String[] items = combo.getItems();
            		for (int i = 0; i < items.length; ++i)
            			if (strVal.equals(items[i])) {
							combo.select(i);
							ContainerTypeDescription desc = (ContainerTypeDescription) combo
									.getData(String.valueOf(i));
							modifyUI(desc.getProperties());
							break;
						}
            	}

            	strVal = pageSettings.get("url");
        		if (strVal != null)
        	    	joingroup_text.setText(strVal);

        		strVal = pageSettings.get("nickname");
        		if (strVal != null)
        			nickname_text.setText(strVal);
        	}
        }
    }
    
    public void saveDialogSettings() {
        IDialogSettings dialogSettings = getDialogSettings();
        if (dialogSettings != null) {
        	IDialogSettings pageSettings = dialogSettings.getSection(DIALOG_SETTINGS);
        	if (pageSettings == null)
        		pageSettings = dialogSettings.addNewSection(DIALOG_SETTINGS);
        	
        	pageSettings.put("url", joingroup_text.getText());
        	pageSettings.put("nickname", nickname_text.getText());
        	int i = combo.getSelectionIndex();
        	if (i >= 0)
        		pageSettings.put("provider", combo.getItem(i));
        }
    }
    
    public String getJoinGroupText() {
        String textValue = joingroup_text.getText().trim();
		String namespace = getNamespace();
		if (namespace != null) {
			return textValue;
		} else {
	        if (!urlPrefix.equals("") && !textValue.startsWith(urlPrefix)) {
	            textValue = urlPrefix+textValue;
	        }
	        return textValue;
		}
    }
    
    public String getNicknameText() {
        if (nickname_text == null) return null;
        return nickname_text.getText().trim();
    }
    
    public String getPasswordText() {
        return password_text.getText();
    }
    
    public String getContainerType() {
        int index = combo.getSelectionIndex();
        if (index == -1) return null;
        else {
            ContainerTypeDescription desc = (ContainerTypeDescription) containerDescriptions.get(index);
            return desc.getName();
        }
    }
	
	public String getNamespace() {
		return namespace;
	}
}
