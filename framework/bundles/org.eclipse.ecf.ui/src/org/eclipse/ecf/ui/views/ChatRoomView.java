package org.eclipse.ecf.ui.views;

import org.eclipse.ecf.presence.chat.IRoomInfo;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class ChatRoomView extends ViewPart {
	private Composite mainComp = null;
	private IRoomInfo roomInfo = null;
	private Text writeText = null;
	private Text readText = null;
	
	public void createPartControl(Composite parent) {
		mainComp = new Composite(parent, SWT.NONE);
		mainComp.setLayout(new FillLayout());
		
		SashForm form = new SashForm(mainComp,SWT.HORIZONTAL);
		form.setLayout(new FillLayout());
		
				
		Composite memberComp = new Composite(form,SWT.NONE);
		memberComp.setLayout(new FillLayout());
		ListViewer memberViewer = new ListViewer(memberComp, SWT.BORDER);
		
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

	public void setRoomInfo(IRoomInfo roomInfo) {
		this.roomInfo = roomInfo;
	}

	public void initialize() {
		if (roomInfo == null) {
			return;
		}
		
		this.setPartName(roomInfo.getName());
		
		//Setup container
	}

}
