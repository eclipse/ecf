package org.eclipse.ecf.provider.generic.gmm;

import org.eclipse.ecf.core.identity.ID;

public class Member implements Comparable {
    
	ID member;
	Object data;

	public Member(ID member) {
		this(member, null);
	}

	public Member(ID member, Object data) {
		this.member = member;
		this.data = data;
	}

	public boolean equals(Object o) {
		if (o != null && o instanceof Member) {
			return member.equals(((Member) o).member);
		} else
			return false;
	}

	public int hashCode() {
		return member.hashCode();
	}

	public int compareTo(Object o) {
		if (o != null && o instanceof Member) {
			return member.compareTo(((Member) o).member);
		} else
			return 0;
	}

	public ID getID() {
		return member;
	}

	public Object getData() {
		return data;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Member[").append(member).append(";").append(
			data).append(
			"]");
		return sb.toString();
	}

}