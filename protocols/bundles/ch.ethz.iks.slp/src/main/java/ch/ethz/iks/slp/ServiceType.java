/* modified version by Jan S. Rellermeyer, ETH Zurich 
 * Copyright 2005-2007 IKS, ETH Zurich. All rights reserved.
 *
 * based on the Java SLP implementation by Solers Corporation 
 * Copyright 2003 Solers Corporation. All rights reserved.
 *
 * Modification and use of this SLP API software and associated documentation
 * ("Software") is permitted provided that the conditions specified in the
 * LICENSE.txt file included within this distribution are met.
 *
 * Author of the original class: Patrick Callis
 */
package ch.ethz.iks.slp;

import java.io.Serializable;

/**
 * Implementation of the SLP ServiceType class defined in RFC 2614.
 * 
 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 * @author Patrick Callis, Solers Corp.
 * @since 0.1
 */
public final class ServiceType implements Serializable {

	/**
	 * the serial UID.
	 */
	private static final long serialVersionUID = 1652247274399819356L;

	/**
	 * the type.
	 */
	private String type = new String();

	/**
	 * is it a service ?
	 */
	private boolean isService = false;

	/**
	 * is it abstract ?
	 */
	private boolean isAbstract = false;

	/**
	 * the concrete type.
	 */
	private String concreteType = new String();

	/**
	 * the principle type.
	 */
	private String principleType = new String();

	/**
	 * the abstract type.
	 */
	private String abstractType = new String();

	/**
	 * the naming authority.
	 */
	private String namingAuthority = new String();

	/**
	 * creates a new ServiceType instance.
	 * 
	 * @param serviceType
	 *            the string representation of a ServiceType, e.g.
	 * 
	 * <pre>
	 *      service:osgi:remote
	 * </pre>
	 */
	public ServiceType(final String serviceType) {
		type = serviceType;
		if (type.startsWith("service:")) {
			isService = true;

			int principleStart = 8;
			int principleEnd = type.indexOf(":", principleStart);

			if (principleEnd != -1) {
				isAbstract = true;
				principleType = type.substring(principleStart, principleEnd);
				abstractType = type.substring(0, principleEnd);
				concreteType = type.substring(principleEnd + 1);
			} else {
				isAbstract = false;
				principleType = type.substring(principleStart);
				abstractType = "";
				concreteType = "";
			}

			int namingStart = type.indexOf(".") + 1;
			if (namingStart != 0) {
				int namingEnd = type.indexOf(":", namingStart);
				String na = "";
				if (namingEnd == -1) {
					na = type.substring(namingStart);
				} else {
					na = type.substring(namingStart, namingEnd);
				}
				// 1954772: isNADefault returns false for "IANA"
				if("IANA".equalsIgnoreCase(na)) {
					namingAuthority = "";
					// remove "iana" from type so toString() is consistent
					type = type.substring(0, namingStart - 1) + type.substring(namingStart + 4, type.length());
				} else {
					namingAuthority = na;
				}
			} else {
				namingAuthority = "";
			}
		}
	}

	/**
	 * is the ServiceType instance a ServiceURL ?
	 * 
	 * @return true if this is the case.
	 */
	public boolean isServiceURL() {
		return isService;
	}

	/**
	 * is the ServiceType instance an abstract type ?
	 * 
	 * @return true if thie is the case.
	 */
	public boolean isAbstractType() {
		return isAbstract;
	}

	/**
	 * is the naming authority default (IANA) ?
	 * 
	 * @return true if this is the case.
	 */
	public boolean isNADefault() {
		return "".equals(namingAuthority);
	}

	/**
	 * get the concrete type part of this ServiceType instance.
	 * 
	 * @return a String representing the concrete type.
	 */
	public String getConcreteTypeName() {
		return concreteType;
	}

	/**
	 * get the principle type part of this ServiceType instance.
	 * 
	 * @return a String representing the principle part.
	 */
	public String getPrincipleTypeName() {
		return principleType;
	}

	/**
	 * get the name of the abstract type of this ServiceType instance.
	 * 
	 * @return a String representing the abstract type.
	 */
	public String getAbstractTypeName() {
		return abstractType;
	}

	/**
	 * get the naming authority.
	 * 
	 * @return the naming authority.
	 */
	public String getNamingAuthority() {
		return namingAuthority;
	}

	/**
	 * check if two ServiceTypes are equal.
	 * 
	 * @param obj
	 *            another ServiceType.
	 * @return true if they equal.
	 */
	public boolean equals(final Object obj) {
		if (!(obj instanceof ServiceType)) {
			return false;
		}
		ServiceType t = (ServiceType) obj;
		return (isService == t.isService && isAbstract == t.isAbstract
				&& concreteType.equals(t.concreteType)
				&& principleType.equals(t.principleType)
				&& abstractType.equals(t.abstractType) && namingAuthority
				.equals(t.namingAuthority));
	}

	/**
	 * check if a ServiceType matches a ServiceURL or another ServiceType.
	 * 
	 * @param obj
	 *            the object to be compared to.
	 * @return true if this type matches the other object.
	 */
	public boolean matches(final Object obj) {
		if (!(obj instanceof ServiceType)) {
			return false;
		}
		ServiceType t = (ServiceType) obj;
		if (!isAbstract) {
			return equals(t);
		} else {
			return equals(t) || t.toString().equals(getAbstractTypeName());
		}
	}

	/**
	 * get a String representation of this ServiceType instance.
	 * 
	 * @return the String representation.
	 */
	public String toString() {
		return type;
	}

	/**
	 * get the hashCode of this ServiceType instance.
	 * 
	 * @return the int value of the hashCode.
	 */
	public int hashCode() {
		int code = 0;

		if (concreteType != null) {
			code ^= (concreteType.hashCode());
		}
		if (principleType != null) {
			code ^= (principleType.hashCode() << 8);
		}
		if (abstractType != null) {
			code ^= (abstractType.hashCode() << 16);
		}
		if (namingAuthority != null) {
			code ^= (namingAuthority.hashCode() << 24);
		}
		return code;
	}

}
