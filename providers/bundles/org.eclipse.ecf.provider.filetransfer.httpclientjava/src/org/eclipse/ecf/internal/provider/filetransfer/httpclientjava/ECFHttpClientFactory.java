/****************************************************************************
 * Copyright (c) 2019, 2022 Yatta Solutions and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *   Yatta Solutions - initial API and implementation
 *   Christoph Läubrich - adapt for java http client
 *
 * SPDX-License-Identifier: EPL-2.0
 *****************************************************************************/
package org.eclipse.ecf.internal.provider.filetransfer.httpclientjava;

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ecf.core.util.Trace;
import org.eclipse.ecf.internal.provider.filetransfer.DebugOptions;
import org.eclipse.ecf.provider.filetransfer.httpclientjava.HttpClientOptions;
import org.eclipse.ecf.provider.filetransfer.httpclientjava.HttpClientRetrieveFileTransfer;
import org.osgi.service.component.annotations.Component;

@SuppressWarnings({ "restriction" })
@Component
public class ECFHttpClientFactory implements IHttpClientFactory {
	private static final List<String> DEFAULT_PREFERRED_AUTH_SCHEMES_NO_NTLM = Arrays.asList("Basic","Digest");
	private static final List<String> DEFAULT_PREFERRED_AUTH_SCHEMES = Arrays.asList("Basic", "Digest", "NTLM");
	public static final int DEFAULT_CONNECTION_TIMEOUT = HttpClientOptions.RETRIEVE_DEFAULT_CONNECTION_TIMEOUT;
	public static final int DEFAULT_CONNECTION_TTL = HttpClientOptions.RETRIEVE_DEFAULT_CONNECTION_TTL;
	public static final int DEFAULT_READ_TIMEOUT = HttpClientOptions.RETRIEVE_DEFAULT_READ_TIMEOUT;

	public static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = HttpClientOptions.RETRIEVE_DEFAULT_CONNECTION_TIMEOUT;

	public static final String NTLM_PROXY_HANDLER_ATTR = INTLMProxyHandler.class.getName();

//	private static final Registry<AuthSchemeFactory> DEFAULT_AUTH_SCHEME_REGISTRY = RegistryBuilder.<AuthSchemeFactory> create().register("Basic", BasicSchemeFactory.INSTANCE).register("Digest", DigestSchemeFactory.INSTANCE).register("NTLM", NTLMSchemeFactory.INSTANCE).build();
//
//	private static final SocketConfig DEFAULT_SOCKET_CONFIG = SocketConfig.copy(SocketConfig.DEFAULT).setSoTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS).setTcpNoDelay(true)// Disable Nagle - see
//			// https://en.wikipedia.org/wiki/Nagle%27s_algorithm#Negative_effect_on_larger_writes
//			// TODO is it safe to set this to 0? This will forcefully terminate sockets on
//			// close instead of waiting for graceful close
//			// See
//			// http://docs.oracle.com/javase/6/docs/api/java/net/SocketOptions.html?is-external=true#SO_LINGER
//			// and https://issues.apache.org/jira/browse/HTTPCLIENT-1497
//			// .setSoLinger(0)
//			.build();

	@Override
	public HttpClient.Builder newClient() {

		HttpClient.Builder builder = HttpClient.newBuilder().followRedirects(Redirect.NORMAL);
//        .connectTimeout(Duration.ofSeconds(20))
//        .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 80)))
//        .authenticator(Authenticator.getDefault())
		// TODO
//		HttpClientBuilder builder = createHttpClientBuilder();
//		builder.setDefaultCredentialsProvider(new HttpClientProxyCredentialProvider());
//		builder.setDefaultRequestConfig(newRequestConfig(null, System.getProperties()).build());
//		PoolingHttpClientConnectionManagerBuilder cmBuilder = PoolingHttpClientConnectionManagerBuilder.create()
//				.setMaxConnPerRoute(100)
//				.setMaxConnTotal(300)
//				.setConnectionTimeToLive(TimeValue.ofMilliseconds(DEFAULT_CONNECTION_TTL))
//				.setDefaultSocketConfig(DEFAULT_SOCKET_CONFIG)
//		        .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
//		        .setConnPoolPolicy(PoolReusePolicy.LIFO);
//		configureSSLSocketFactory(cmBuilder);
//		builder.setConnectionManager(cmBuilder.build());
//		builder.setDefaultAuthSchemeRegistry(DEFAULT_AUTH_SCHEME_REGISTRY);
		builder = Activator.getDefault().runModifiers(builder, new ModifierRunner<HttpClient.Builder>() {
			@Override
			public HttpClient.Builder run(IHttpClientModifier modifier, HttpClient.Builder value) {
				return modifier.modifyClient(value);
			}

		});

		return builder;
	}

	@Override
	public IHttpClientContext newClientContext() {
		IHttpClientContext context = new IHttpClientContext() {

			private Map<String, Object> values = new HashMap<>();
			private Authenticator authenticator;
			private HttpHost httpHost;

			@Override
			public void setAttribute(String key, Object value) {
				values.put(key, value);
			}

			@Override
			public void setCredentialsProvider(Authenticator contextCredentialsProvider) {
				this.authenticator = contextCredentialsProvider;
			}

			@Override
			public Object getAttribute(String key) {
				return values.get(key);
			}

			@Override
			public void setProxy(HttpHost httpHost) {
				this.httpHost = httpHost;
			}
		};
		INTLMProxyHandler ntlmProxyHandler = Activator.getDefault().getNTLMProxyHandler();
		context.setAttribute(NTLM_PROXY_HANDLER_ATTR, ntlmProxyHandler);
//		context.setAuthSchemeRegistry(DEFAULT_AUTH_SCHEME_REGISTRY);

		context = Activator.getDefault().runModifiers(context, new ModifierRunner<IHttpClientContext>() {

			@Override
			public IHttpClientContext run(IHttpClientModifier modifier, IHttpClientContext value) {
				return modifier.modifyContext(value);
			}

		});

		return context;
	}

	@Override
	public HttpRequest.Builder newRequestConfig(final IHttpClientContext context, final Map<?, ?> localOptions) {
		HttpRequest.Builder builder = HttpRequest.newBuilder();
//		        .uri(URI.create("https://foo.com/"))
//		        .timeout(Duration.ofMinutes(2))
//		        .header("Content-Type", "application/json")
//		        .POST(BodyPublishers.ofFile(Paths.get("file.json")))
		// TODO
//		int connectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT;
//		int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
//		int readTimeout = DEFAULT_READ_TIMEOUT;
//		if (localOptions != null) {
//			connectionRequestTimeout = getIntOption(HttpClientOptions.RETRIEVE_CONNECTION_TIMEOUT_PROP, localOptions, connectionRequestTimeout);
//			connectionTimeout = getIntOption(HttpClientOptions.RETRIEVE_CONNECTION_TIMEOUT_PROP, localOptions, connectionTimeout);
//			readTimeout = getIntOption(HttpClientOptions.RETRIEVE_READ_TIMEOUT_PROP, localOptions, readTimeout);
//		}
//		builder.setConnectionRequestTimeout(connectionRequestTimeout,TimeUnit.MILLISECONDS).setConnectTimeout(connectionTimeout,TimeUnit.MILLISECONDS);
//
//		boolean allowNTLMAuthentication = getNTLMProxyHandler(context).allowNTLMAuthentication(localOptions);
//		Collection<String> preferredAuthSchemes = allowNTLMAuthentication ? DEFAULT_PREFERRED_AUTH_SCHEMES : DEFAULT_PREFERRED_AUTH_SCHEMES_NO_NTLM;
//		builder.setProxyPreferredAuthSchemes(preferredAuthSchemes);
//		builder.setTargetPreferredAuthSchemes(preferredAuthSchemes);

		builder = Activator.getDefault().runModifiers(builder, new ModifierRunner<HttpRequest.Builder>() {

			@Override
			public HttpRequest.Builder run(IHttpClientModifier modifier, HttpRequest.Builder value) {
				return modifier.modifyRequestConfig(value, context, localOptions);
			}

		});
		return builder;
	}

	private int getIntOption(String key, final Map<?, ?> localOptions, int defaultValue) {
		Object option = localOptions.get(key);
		if (option != null) {
			int value = defaultValue;
			if (option instanceof Number) {
				value = ((Number) option).intValue();
			} else if (option instanceof String) {
				try {
					value = Integer.parseInt((String) option);
				} catch (NumberFormatException e) {
					Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING, HttpClientRetrieveFileTransfer.class, "getDnsHostName", e); //$NON-NLS-1$
				}
			}
			return value;
		}
		return defaultValue;
	}

//	private static void configureSSLSocketFactory(PoolingHttpClientConnectionManagerBuilder cm) {
//		SSLSocketFactory sslSocketFactory = Activator.getDefault().getSSLSocketFactory();
//		if (sslSocketFactory == null) {
//			try {
//				sslSocketFactory = new HttpClientDefaultSSLSocketFactoryModifier().getSSLSocketFactory();
//			} catch (IOException e) {
//				Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING, HttpClientDefaultSSLSocketFactoryModifier.class, "getSSLSocketFactory()", e); //$NON-NLS-1$
//				throw new ECFRuntimeException("Unable to instantiate schemes for HttpClient.", e); //$NON-NLS-1$
//			}
//		}
//		if (sslSocketFactory == SSLSocketFactory.getDefault()) {
//			sslSocketFactory = null;
//		}
//		if (sslSocketFactory != null) {
//			PublicSuffixMatcher publicSuffixMatcherCopy = PublicSuffixMatcherLoader.getDefault();
//			String systemHttpsProtocols = System.getProperty("https.protocols");
//			String systemCipherSuites = System.getProperty("https.cipherSuites");
//			String[] supportedProtocols = split(systemHttpsProtocols);
//			String[] supportedCipherSuites = split(systemCipherSuites);
//			HostnameVerifier hostnameVerifierCopy = new DefaultHostnameVerifier(publicSuffixMatcherCopy);
//			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslSocketFactory, supportedProtocols, supportedCipherSuites, hostnameVerifierCopy);
//			cm.setSSLSocketFactory(sslConnectionSocketFactory);
//		}
//	}
//
//	private synchronized void track(CloseableHttpClient client) {
//		clearCollectedClients();
//		if (collectedClients == null) {
//			collectedClients = new ReferenceQueue<CloseableHttpClient>();
//			trackedClients = new LinkedList<WeakReference<CloseableHttpClient>>();
//		}
//		trackedClients.add(new WeakReference<CloseableHttpClient>(client, collectedClients));
//	}
//
//	private synchronized void clearCollectedClients() {
//		if (collectedClients == null) {
//			return;
//		}
//		for (Reference<? extends CloseableHttpClient> collectedReference = collectedClients.poll(); collectedReference != null; collectedReference = collectedClients.poll()) {
//			trackedClients.remove(collectedReference);
//		}
//	}
//
//	public synchronized void close() {
//		if (trackedClients != null) {
//			for (WeakReference<CloseableHttpClient> clientRef : trackedClients) {
//				CloseableHttpClient client = clientRef.get();
//				if (client == null) {
//					continue;
//				}
//				try {
//					client.close();
//				} catch (IOException ex) {
//					Trace.catching(Activator.PLUGIN_ID, DebugOptions.EXCEPTIONS_CATCHING, ECFHttpClientFactory.class, "close", ex); //$NON-NLS-1$
//				}
//			}
//			trackedClients = null;
//			collectedClients = null;
//		}
//	}

	private static String[] split(final String s) {
		if (s == null || s.isBlank()) {
			return null;
		}
		return s.split(" *, *");
	}

	public static Authenticator modifyCredentialsProvider(Authenticator credentialsProvider) {
		return Activator.getDefault().runModifiers(credentialsProvider, new ModifierRunner<Authenticator>() {

			@Override
			public Authenticator run(IHttpClientModifier modifier, Authenticator value) {
				return modifier.modifyCredentialsProvider(value);
			}

		});
	}

	public static INTLMProxyHandler getNTLMProxyHandler(IHttpClientContext httpContext) {
		Object value = httpContext == null ? null : httpContext.getAttribute(ECFHttpClientFactory.NTLM_PROXY_HANDLER_ATTR);
		if (value instanceof INTLMProxyHandler) {
			return (INTLMProxyHandler) value;
		}
		INTLMProxyHandler handler = Activator.getDefault().getNTLMProxyHandler();
		if (handler != null && httpContext != null && value == null) {
			httpContext.setAttribute(ECFHttpClientFactory.NTLM_PROXY_HANDLER_ATTR, handler);
		}
		return handler;
	}

	protected static interface ModifierRunner<T> {
		T run(IHttpClientModifier modifier, T value);
	}
}
