package org.eclipse.ecf.ui.views;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.presence.IInvitationListener;
import org.eclipse.ecf.presence.IMessageListener;
import org.eclipse.ecf.presence.IMessageSender;
import org.eclipse.ecf.presence.IParticipantListener;
import org.eclipse.ecf.presence.IPresence;
import org.eclipse.ecf.presence.chat.IChatRoomContainer;
import org.eclipse.ecf.presence.chat.IRoomInfo;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class ChatRoomView extends ViewPart implements IMessageListener, IParticipantListener, IInvitationListener {
	private Composite mainComp = null;
	private IRoomInfo roomInfo = null;
	private Text writeText = null;
	private Text readText = null;
	private ListViewer memberViewer = null;
	
	IMessageSender messageSender = null;
	IChatRoomContainer chatRoomContainer = null;
	
	public void createPartControl(Composite parent) {
		mainComp = new Composite(parent, SWT.NONE);
		mainComp.setLayout(new FillLayout());
		
		SashForm form = new SashForm(mainComp,SWT.HORIZONTAL);
		form.setLayout(new FillLayout());
		
				
		Composite memberComp = new Composite(form,SWT.NONE);
		memberComp.setLayout(new FillLayout());
		memberViewer = new ListViewer(memberComp, SWT.BORDER);

		Composite rightComp = new Composite(form, SWT.NONE);
		rightComp.setLayout(new FillLayout());
		
		SashForm rightSash = new SashForm(rightComp, SWT.VERTICAL);
		rightSash.setLayout(new FillLayout());
		
		
		Composite readComp = new Composite(rightSash, SWT.NONE);
		readComp.setLayout(new FillLayout());
		readText = new Text(readComp, SWT.BORDER);
		readText.setEditable(false);
		
		Composite writeComp = new Composite(rightSash, SWT.NONE);
		writeComp.setLayout(new FillLayout());
		writeText = new Text(writeComp, SWT.BORDER | SWT.MULTI);
		
		form.setWeights(new int[] {30, 70});
		rightSash.setWeights(new int[] {70, 30});
	}

	public void setFocus() {
		writeText.setFocus();
	}

	public IRoomInfo getRoomInfo() {
		return roomInfo;
	}

	public void initialize(IChatRoomContainer container, IRoomInfo info, IMessageSender sender) {
		this.chatRoomContainer = container;
		this.messageSender = sender;
		this.roomInfo = info;
		this.setPartName(roomInfo.getName());
	}

	protected String getMessageString(ID fromID, String text) {
		return fromID.getName() + ": "+text+"\n";
	}
	public void handleMessage(final ID fromID, final ID toID, final Type type, final String subject, final String messageBody) {
		System.out.println("Room message from="+fromID+",to="+toID+",type="+type+",sub="+subject+",body="+messageBody);
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
            	readText.append(getMessageString(fromID,messageBody));
            }
        });
	}

	class Participant {
		String name;
		ID id;
		
		public Participant(ID id) {
			this.id = id;
		}
		
		public ID getID() {
			return id;
		}
		public String getName() {
			return id.getName();
		}
		public boolean equals(Object other) {
			if (!(other instanceof Participant)) return false;
			Participant o = (Participant) other;
			if (id.equals(o.id)) return true;
			return false;
		}
		public int hashCode() {
			return id.hashCode();
		}
		public String toString() {
			String fullName = getName();
			int atIndex = fullName.indexOf('@');
			if (atIndex != -1) {
				fullName = fullName.substring(0,atIndex);
			}
			return fullName;
		}
	}
	public void handlePresence(final ID fromID, final IPresence presence) {
		System.out.println("chat presence from="+fromID+",presence="+presence);
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
            	boolean isAdd = presence.getType().equals(IPresence.Type.AVAILABLE);
            	if (isAdd) {
            		memberViewer.add(new Participant(fromID));
            	} else {
            		memberViewer.remove(new Participant(fromID));
            	}
            }
        });
	}

	public void handleInvitationReceived(ID roomID, ID from, ID toID, String subject, String body) {
		System.out.println("invitation room="+roomID+",from="+from+",to="+toID+",subject="+subject+",body="+body);
	}

}
