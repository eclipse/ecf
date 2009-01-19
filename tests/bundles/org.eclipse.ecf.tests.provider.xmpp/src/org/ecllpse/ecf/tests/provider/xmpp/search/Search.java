package org.ecllpse.ecf.tests.provider.xmpp.search;

import org.eclipse.ecf.presence.search.ICriteria;
import org.eclipse.ecf.presence.search.IResultList;
import org.eclipse.ecf.presence.search.ISearch;
import org.eclipse.ecf.presence.search.ResultList;

public class Search implements ISearch {

	private ICriteria criteria;
	private IResultList resultList;

	public Search(ICriteria criteria){
		this.criteria = criteria;
		resultList = new ResultList();
	}
	
	public ICriteria getCriteria() {
		return this.criteria;
	}

	public IResultList getResultList() {
		return this.resultList;
	}

	public void setResultList(IResultList resultList) {
		this.resultList = resultList;

	}

}
