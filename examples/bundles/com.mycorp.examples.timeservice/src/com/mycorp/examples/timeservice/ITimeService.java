package com.mycorp.examples.timeservice;

/**
 * Example OSGi service for retrieving current time in milliseconds from January
 * 1, 1970.
 * 
 */
public interface ITimeService {

	/**
	 * Get current time.
	 * 
	 * @return Long current time in milliseconds since Jan 1, 1970. Will not
	 *         return <code>null</code>.
	 */
	public Long getCurrentTime();

}
