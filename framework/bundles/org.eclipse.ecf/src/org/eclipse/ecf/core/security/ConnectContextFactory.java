package org.eclipse.ecf.core.security;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Helper class for creating instances of IConnectContext
 *
 */
public class ConnectContextFactory {
	private ConnectContextFactory() {
		super();
	}
	public static IConnectContext makeUsernamePasswordConnectContext(final String username, final Object password) {
		return new IConnectContext() {
			public CallbackHandler getCallbackHandler() {
				return new CallbackHandler() {
					public void handle(Callback[] callbacks)
							throws IOException, UnsupportedCallbackException {
						if (callbacks == null)
							return;
						for (int i = 0; i < callbacks.length; i++) {
							if (callbacks[i] instanceof NameCallback) {
								NameCallback ncb = (NameCallback) callbacks[i];
								ncb.setName(username);
							} else if (callbacks[i] instanceof ObjectCallback) {
								ObjectCallback ocb = (ObjectCallback) callbacks[i];
								ocb.setObject(password);
							}
						}
					}
				};
			}
		};
	}
	
	public static IConnectContext makePasswordConnectContext(final String password) {
		return new IConnectContext() {
			public CallbackHandler getCallbackHandler() {
				return new CallbackHandler() {
					public void handle(Callback[] callbacks)
							throws IOException, UnsupportedCallbackException {
						if (callbacks == null)
							return;
						for (int i = 0; i < callbacks.length; i++) {
							if (callbacks[i] instanceof ObjectCallback) {
								ObjectCallback ocb = (ObjectCallback) callbacks[i];
								ocb.setObject(password);
							}
						}
					}
				};
			}
		};
	}
	
}
