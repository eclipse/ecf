/*
 * Created on Mar 20, 2005
 *
 */
package org.eclipse.ecf.provider.xmpp.smack.test;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * 
 */
public class TestGroupChat {
    public TestGroupChat() {
        super();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Connecting to server "+args[0]);
        XMPPConnection conn = new XMPPConnection(args[0]);
        System.out.println("connected");
        System.out.println("logging in user "+args[1]);
        conn.login(args[1],args[2]);
        System.out.println("logged in user "+args[1]);
        System.out.println("checking whether service is enabled...");
        boolean supports = MultiUserChat.isServiceEnabled(conn, args[1]+"@"+args[0]+"/Smack");
        System.out.println("supports is "+supports);
        MultiUserChat muc = new MultiUserChat(conn,"myroom@conference.cerf.composent.com");
        muc.create("testbot");
        muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
        System.out.println("created multiuser chat");
        
        
        muc.join(args[1]);
        muc.sendMessage("hello there");
        Thread.sleep(200000);
    }
}
