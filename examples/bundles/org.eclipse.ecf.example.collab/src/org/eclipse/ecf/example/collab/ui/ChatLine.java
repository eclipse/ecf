/*
 * Created on Feb 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ecf.example.collab.ui;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.example.collab.share.User;



/**
 * @author kgilmer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ChatLine {
	private User originator = null;	//
	private String text = null;
	private boolean isPrivate = false;
	private boolean isRaw = false;
	private boolean noCRLF = false;
	
	public ChatLine() {
		
	}
	
	public ChatLine(String text) {
		this.text = text;
	
	}
	
	public ChatLine(String text, User user) {
		this.text = text;
		this.originator = user;
	}
	/**
	 * @return Returns the originator.
	 */
	public User getOriginator() {
		return originator;
	}
	/**
	 * @param originator The originator to set.
	 */
	public void setOriginator(User originator) {
		this.originator = originator;
	}
	/**
	 * @return Returns the text.
	 */
	public String getText() {
		return text;
	}
	/**
	 * @param text The text to set.
	 */
	public void setText(String text) {
		this.text = text;
	}
	/**
	 * @return Returns the isPrivate.
	 */
	public boolean isPrivate() {
		return isPrivate;
	}
	/**
	 * @param isPrivate The isPrivate to set.
	 */
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}
	/**
	 * @return Returns the isRaw.
	 */
	public boolean isRaw() {
		return isRaw;
	}
	/**
	 * @param isRaw The isRaw to set.
	 */
	public void setRaw(boolean isRaw) {
		this.isRaw = isRaw;
	}
	/**
	 * @return Returns the noCRLF.
	 */
	public boolean isNoCRLF() {
		return noCRLF;
	}
	/**
	 * @param noCRLF The noCRLF to set.
	 */
	public void setNoCRLF(boolean noCRLF) {
		this.noCRLF = noCRLF;
	}
}
