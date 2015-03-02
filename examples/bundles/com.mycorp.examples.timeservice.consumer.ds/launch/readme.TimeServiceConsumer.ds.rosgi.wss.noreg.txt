README for TimeServiceConsumer.rosgi.ds.wss.noreg.product

This product config uses websockets with SSL, and so to use requires the configuration of an SSLContext.
As part of the VM arguments in the product config are these:

-Djavax.net.ssl.keyStore=security/server.ks
-Djavax.net.ssl.keyStorePassword=server
-Djavax.net.ssl.trustStore=security/server.ks
-Djavax.net.debug=all

These arguments specify the location of a java keystore/trustStore, a password for 
runtime access to that keystore and the javax.net.debug=all turns on full debugging
to console of the SSLContext configuration.

The referenced keystore must be present at the given path, have an appropriate certificate, 
and the password must be correct for accessing that keystore in order for these example 
product configs to work when Run/Debugged.

Note that when Eclipse generates launch configs from product configs the working directory used is
the Eclipse working directory (e.g. c:\eclipsehome), meaning that the keyStore/trustStore paths given
above would refer to a file location of (e.g.):

c:\eclipsehome\security\server.ks






