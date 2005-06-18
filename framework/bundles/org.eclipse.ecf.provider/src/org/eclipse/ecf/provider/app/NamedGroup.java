/**
 * 
 */
package org.eclipse.ecf.provider.app;

public class NamedGroup {
	Connector parent;
	String name;
	
	public NamedGroup(String name) {
		this.name = name;
	}
	protected void setParent(Connector c) {
		this.parent = c;
	}
	public String getName() {
		return cleanGroupName(name);
	}
	public String getIDForGroup() {
		return parent.getID()+getName();
	}
	protected String cleanGroupName(String n) {
		String res = ((n.startsWith("/"))?n:"/"+n);
		return res;
	}
}