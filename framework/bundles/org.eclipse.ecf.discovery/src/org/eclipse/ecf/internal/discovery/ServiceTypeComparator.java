package org.eclipse.ecf.internal.discovery;

import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.ecf.discovery.identity.IServiceTypeID;

public class ServiceTypeComparator implements Comparator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object object1, Object object2) {
		if (object1 instanceof IServiceTypeID
				&& object2 instanceof IServiceTypeID) {
			IServiceTypeID type1 = (IServiceTypeID) object1;
			IServiceTypeID type2 = (IServiceTypeID) object2;

			String name1 = type1.getNamingAuthority();
			String name2 = type2.getNamingAuthority();
			if (!name1.equals("*") || !name2.equals("*")
					|| !name1.equals(name2)) {
				return -1;
			}

			String[] services1 = type1.getServices();
			String[] services2 = type2.getServices();
			if (!services1[0].equals("*") || !services2[0].equals("*")
					|| !Arrays.equals(services1, services2)) {
				return -1;
			}

			String[] protocols1 = type1.getProtocols();
			String[] protocols2 = type2.getProtocols();
			if (!protocols1[0].equals("*") || !protocols2[0].equals("*")
					|| !Arrays.equals(protocols1, protocols2)) {
				return -1;
			}

			String[] scopes1 = type1.getScopes();
			String[] scopes2 = type2.getScopes();
			if (!scopes1[0].equals("*") || !scopes2[0].equals("*")
					|| !Arrays.equals(scopes1, scopes2)) {
				return -1;
			}
			return 0;
		}
		return -1;
	}

}
