package org.eclipse.ecf.tests.filetransfer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.ecf.filetransfer.urlservice.IURLConnectionFactory;

public class URLConnectionFactory implements IURLConnectionFactory {

	public URLConnectionFactory() {
	}

	public URLConnection createURLConnection(URL url) throws IOException {
		return new URLConnection(url) {
			public void connect() throws IOException {
				throw new IOException("can't connect");
			}};
	}

}
