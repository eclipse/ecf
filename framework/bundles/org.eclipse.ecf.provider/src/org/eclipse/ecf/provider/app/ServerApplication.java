package org.eclipse.ecf.provider.app;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.provider.generic.SOContainerConfig;
import org.eclipse.ecf.provider.generic.TCPServerSOContainer;
import org.eclipse.ecf.provider.generic.TCPServerSOContainerGroup;

/**
 * An ECF server container implementation that runs as an application.
 * <p>
 * Usage: java org.eclipse.ecf.provider.app.ServerApplication &lt;serverid&gt
 * <p>
 * If &lt;serverid&gt; is omitted or "-" is specified,
 * ecftcp://localhost:3282/server" is used.  The &lt;serverid&gt; must correspond to URI syntax as 
 * defined by <a href="http://www.ietf.org/rfc/rfc2396.txt"><i>RFC&nbsp;2396: Uniform
 * Resource Identifiers (URI): Generic Syntax</i></a>, amended by <a href="http://www.ietf.org/rfc/rfc2732.txt"><i>RFC&nbsp;2732: 
 * Format for Literal IPv6 Addresses in URLs</i></a>
 *  
 */
public class ServerApplication {
    public static final int DEFAULT_KEEPALIVE = TCPServerSOContainer.DEFAULT_KEEPALIVE;
    static TCPServerSOContainerGroup serverGroup = null;
    static TCPServerSOContainer server = null;

    public static void main(String args[]) throws Exception {
        // Get server identity
        String serverName = null;
        if (args.length > 0) {
            if (!args[0].equals("-"))
                serverName = args[0];
        }
        if (serverName == null) {
            serverName = TCPServerSOContainer.getDefaultServerURL();
        }
        java.net.URI anURL = new java.net.URI(serverName);
        int port = anURL.getPort();
        if (port == -1) {
            port = TCPServerSOContainer.DEFAULT_PORT;
        }
        String name = anURL.getPath();
        if (name == null) {
            name = TCPServerSOContainer.DEFAULT_NAME;
        }
        // Setup server group
        serverGroup = new TCPServerSOContainerGroup(anURL.getPort());
        // Create identity for server
        ID id = IDFactory.makeStringID(serverName);
        // Create server config object with identity and default timeout
        SOContainerConfig config = new SOContainerConfig(id);
        // Make server instance
        System.out.print("Creating ECF server container...");
        server = new TCPServerSOContainer(config, serverGroup, name,
                TCPServerSOContainer.DEFAULT_KEEPALIVE);
        serverGroup.putOnTheAir();
        System.out.println("success!");
        System.out
                .println("Waiting for client connections at '" + id.getName() + "'...");
        System.out.println("<ctrl>-c to stop server");
    }
}