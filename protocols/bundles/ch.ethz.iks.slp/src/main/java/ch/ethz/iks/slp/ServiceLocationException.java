/* Copyright (c) 2005-2007 Jan S. Rellermeyer
 * Information and Communication Systems Research Group (IKS),
 * Department of Computer Science, ETH Zurich.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    - Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    - Neither the name of ETH Zurich nor the names of its contributors may be
 *      used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package ch.ethz.iks.slp;

import java.io.Serializable;

/**
 * This exception is thrown whenever a part of the SLP framework causes an
 * exception. The error code is a hint why the exception occured.
 *
 * @author Jan S. Rellermeyer, IKS, ETH Zurich
 * @since 0.1
 */
public class ServiceLocationException extends Exception
                                        implements Serializable {

    /**
     * @serial for serialization.
     */
    private static final long serialVersionUID = 5718658752610460537L;

    /**
     * There is data for the service type in the scope in the AttrRqst or
     * SrvRqst, but not in the requested language.
     */
    public static final short LANGUAGE_NOT_SUPPORTED = 1;

    /**
     * The message fails to obey SLP syntax.
     */
    public static final short PARSE_ERROR = 2;

    /**
     * The SrvReg has problems -- e.g., a zero lifetime or an omitted Language
     * Tag.
     */
    public static final short INVALID_REGISTRATION = 3;

    /**
     * The SLP message did not include a scope in its scope-list supported by
     * the SA or DA.
     */
    public static final short SCOPE_NOT_SUPPORTED = 4;

    /**
     * The DA or SA receives a request for an unsupported SLP SPI.
     */
    public static final short AUTHENTICATION_UNKNOWN = 5;

    /**
     * The DA expected URL and ATTR authentication in the SrvReg and did not
     * receive it.
     */
    public static final short AUTHENTICATION_ABSENT = 6;

    /**
     * Unsupported version number in message header. INTERNAL_ERROR = 10: The DA
     * (or SA) is too sick to respond.
     */
    public static final short AUTHENTICATION_FAILED = 7;

    /**
     * The DA received a SrvReg without FRESH set, for an unregistered service
     * or with inconsistent Service Types.
     */
    public static final short INVALID_UPDATE = 13;

    /**
     * The DA rejected the update because it was within the minimal update
     * intervall.
     */
    public static final short REFRESH_REJECTED = 15;

    /**
     * The feature or extension is not implemented.
     */
    public static final short NOT_IMPLEMENTED = 16;

    /**
     * The initialization of the framework failed.
     */
    public static final short NETWORK_INIT_FAILED = 17;

    /**
     * The network timed out while the framework tried to send a message.
     */
    public static final short NETWORK_TIMED_OUT = 18;

    /**
     * The network encountered an error.
     */
    public static final short NETWORK_ERROR = 19;

    /**
     * The framework encountered an internal system error.
     */
    public static final short INTERNAL_SYSTEM_ERROR = 20;

    /**
     * The type was not well formed.
     */
    public static final short TYPE_ERROR = 21;

    /**
     * The framework encountered a buffer overflow.
     */
    public static final short BUFFER_OVERFLOW = 22;

    /**
     * the error code of this exception instance.
     */
    private short errorCode;

    /**
     * hidden constructor.
     */
    private ServiceLocationException() {
    }

    /**
     * Create a new ServiceLocation instance.
     *
     * @param errcode
     *            the error code, one of the statically defined.
     * @param message
     *            the message of the exception.
     */
    public ServiceLocationException(final short errcode, final String message) {
        super(message);
        errorCode = errcode;
    }

    /**
     * Get the error code of the exception.
     *
     * @return the error code.
     */
    public final short getErrorCode() {
        return errorCode;
    }
}
