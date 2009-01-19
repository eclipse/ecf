package org.ecllpse.ecf.tests.provider.xmpp.search;

import org.eclipse.ecf.presence.search.ISearch;
import org.eclipse.ecf.presence.search.IUserSearchEvent;

public class UserSearchEvent implements IUserSearchEvent {

	private ISearch search;

	public UserSearchEvent(ISearch search){
		this.search = search;
	}
	
	public ISearch getSearch() {
		return search;
	}

}
