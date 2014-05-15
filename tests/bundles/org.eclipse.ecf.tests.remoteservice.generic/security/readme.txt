In this directory is a java keystore file 'server.ks' that has a self-signed certificate.
The password for accessing this keystore is 'server'.  To use with the default
SSLServerSocketFactory and SSLSocketFactory you may use the following Java System
Properties

-Djavax.net.ssl.keyStore=security/server.ks
-Djavax.net.ssl.keyStorePassword=server
-Djavax.net.ssl.trustStore=security/server.ks

To debug the ssl connection process add this Java System Property

-Djavax.net.debug=all 
