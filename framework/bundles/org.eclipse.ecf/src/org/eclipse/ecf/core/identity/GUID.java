/*******************************************************************************
 * Copyright (c) 2004 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Composent, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.ecf.core.identity;

import java.security.SecureRandom;

import org.eclipse.ecf.core.identity.provider.IDInstantiator;
import org.eclipse.ecf.core.util.Base64;

/**
 * Globally unique ID class.
 * 
 */
public class GUID extends StringID {

    public static class Creator implements IDInstantiator {
        public ID makeInstance(Namespace ns, Class[] argTypes, Object[] args)
                throws IDInstantiationException {
            if (args.length == 1) return new GUID(ns, ((Integer) args[0]).intValue());
            else return new GUID(ns);
        }
    }

    public static final String SR_DEFAULT_ALGO = "SHA1PRNG";
    public static final String SR_DEFAULT_PROVIDER = null;
    public static final int DEFAULT_BYTE_LENGTH = 20;

    public static final String GUID_NAME = GUID.class.getName();
    public static final String GUID_INSTANTIATOR_CLASS = GUID.Creator.class
            .getName();

    // Class specific SecureRandom instance
    protected static transient SecureRandom random;

    /**
     * Protected constructor for factory-based construction
     * 
     * @param n
     *            the Namespace this identity will belong to
     * @param s
     *            the String defining this StringID
     */
    protected GUID(Namespace n, String algo, String provider, int byteLength)
            throws IDInstantiationException {
        super(n, "");
        // Get SecureRandom instance for class
        SecureRandom r = null;
        try {
            r = getRandom(algo, provider);
        } catch (Exception e) {
            throw new IDInstantiationException("GUID creation failure: "
                    + e.getMessage());
        }
        // make sure we have reasonable byteLength
        if (byteLength <= 0)
            byteLength = 1;
        byte[] newBytes = new byte[byteLength];
        // Fill up random bytes
        random.nextBytes(newBytes);
        // Set value
        value = Base64.encode(newBytes);
    }

    protected GUID(Namespace n, int byteLength) throws IDInstantiationException {
        this(n, SR_DEFAULT_ALGO, SR_DEFAULT_PROVIDER, byteLength);
    }

    protected GUID(Namespace n) throws IDInstantiationException {
        this(n, DEFAULT_BYTE_LENGTH);
    }

    /**
     * Get SecureRandom instance for creation of random number.
     * 
     * @param algo
     *            the String algorithm specification (e.g. "SHA1PRNG") for
     *            creation of the SecureRandom instance
     * @param provider
     *            the provider of the implementation of the given algorighm
     *            (e.g. "SUN")
     * @return SecureRandom
     * @exception Exception
     *                thrown if SecureRandom instance cannot be created/accessed
     */
    protected static synchronized SecureRandom getRandom(String algo,
            String provider) throws Exception {
        // Given algo and provider, get SecureRandom instance
        if (random == null) {
            initializeRandom(algo, provider);
        }
        return random;
    }

    protected static synchronized void initializeRandom(String algo,
            String provider) throws Exception {
        random = provider == null ? SecureRandom.getInstance(algo)
                : SecureRandom.getInstance(algo, provider);
    }

    public String toString() {
        if (value == null)
            return "";
        int strlen = value.length();
        StringBuffer sb = new StringBuffer(strlen + 10);
        String packname = this.getClass().getPackage().getName();
        int pnLength = packname.length();
        sb.insert(0, packname + ".GUID[").insert(pnLength + 6, value).insert(
                strlen + pnLength + 6, ']');
        return sb.toString();
    }

    // Test code
    public static final void main(String[] args) throws Exception {
        System.out.println("Testing creation of GUID instance");
        System.out.println("Calling initializeRandom() for the first time...");
        initializeRandom(SR_DEFAULT_ALGO, SR_DEFAULT_PROVIDER);
        System.out
                .println("This will take a long time because of the SecureRandom seed process");
        GUID myGUID = new GUID(null, 24);
        System.out.println("Created GUID: " + myGUID);
        GUID myGUID2 = new GUID(null);
        System.out.println("Created second GUID: " + myGUID2);
    }
}