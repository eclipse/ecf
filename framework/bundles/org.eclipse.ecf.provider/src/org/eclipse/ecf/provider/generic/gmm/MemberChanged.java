package org.eclipse.ecf.provider.generic.gmm;

public class MemberChanged {
	Member member;
	boolean added;

	public MemberChanged(Member member, boolean added) {
		this.member = member;
		this.added = added;
	}

	public Member getMember() {
		return member;
	}

	public boolean getAdded() {
		return added;
	}
}