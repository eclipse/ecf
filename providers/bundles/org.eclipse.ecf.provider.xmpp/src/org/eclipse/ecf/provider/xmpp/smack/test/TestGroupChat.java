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
package org.eclipse.ecf.provider.xmpp.smack.test;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
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
        //GroupChat newGroupChat = conn.createGroupChat("test@ecf1.osuosl.org");
        //newGroupChat.join("slewis");
        //      Send a message to all the other people in the chat room.
        //newGroupChat.sendMessage("Howdy!");
        
        boolean supports = MultiUserChat.isServiceEnabled(conn, "slewis@ecf1");
        System.out.println("supports is "+supports);
        MultiUserChat muc = new MultiUserChat(conn,"myroom3@conference.ecf1");
        muc.create("slewis");
        muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
        System.out.println("created multiuser chat");
        
        muc.addMessageListener(new PacketListener() {

			public void processPacket(Packet arg0) {
				if (arg0 instanceof Message) {
					Message mess = (Message) arg0;
					System.out.println("From="+mess.getFrom()+",To="+mess.getTo()+",Subject="+mess.getSubject()+",Thread="+mess.getThread()+",Type="+mess.getType()+",Body="+mess.getBody());
				}
			}
        	
        });
        
        muc.join(args[1]);
        for(int i=0; i < 10; i++) {
        	muc.sendMessage("hello there: "+i);
        }
        Thread.sleep(200000);
    }
}
