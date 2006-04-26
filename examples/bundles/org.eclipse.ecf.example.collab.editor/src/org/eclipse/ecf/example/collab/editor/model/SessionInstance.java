package org.eclipse.ecf.example.collab.editor.model;

import java.io.Serializable;
import java.util.Date;

/**
 * This model class represents a shared editor session.
 * 
 * @author kg11212
 *
 */
public class SessionInstance implements Serializable {
	private static final long serialVersionUID = 4224951859333859979L;
	
	private String name;
	private String owner;
	private Date created;
	private String channelID;
	
	public SessionInstance() {
		
	}
	
	public SessionInstance(String channelID, String name, String owner, Date created) {
		this.channelID = channelID;
		this.name = name;
		this.owner = owner;
		this.created = created;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof SessionInstance) {
			SessionInstance si = (SessionInstance) obj;
			
			if (name.equals(si.getName()) && channelID.equals(si.getChannelID()) && owner.equals(si.getOwner())) {
				return true;
			}
			
			return false;
		}
		return super.equals(obj);
	}
	
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getChannelID() {
		return channelID;
	}

	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}

}
