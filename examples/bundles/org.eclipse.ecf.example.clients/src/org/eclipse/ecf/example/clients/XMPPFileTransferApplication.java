package org.eclipse.ecf.example.clients;

import java.io.File;

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.ecf.filetransfer.IOutgoingFileTransfer;

public class XMPPFileTransferApplication implements IPlatformRunnable{

	public Object run(Object args) throws Exception {
		if (args instanceof Object[]) {
			Object[] arguments = (Object[]) args;
			while (arguments.length > 0 && arguments[0] instanceof String
					&& ((String) arguments[0]).startsWith("-")) {
				System.arraycopy(arguments, 1,
						arguments = new Object[arguments.length - 1], 0,
						arguments.length);
			}
			if (arguments.length == 4) {
				if (arguments[0] instanceof String
						&& arguments[1] instanceof String
						&& arguments[2] instanceof String
						&& arguments[3] instanceof String) {
					String userAtHost = (String) arguments[0];
					String password = (String) arguments[1];
					String targetUserAtHost = (String) arguments[2];
					String file = (String) arguments[3];
					runRobot(userAtHost, password, targetUserAtHost, file);
					return new Integer(0);
				}
			}
		}
		System.out
				.println("Usage: pass in four arguments (username@hostname, password, targetUser@hostname, file)");
		return new Integer(-1);
	}

	protected void runRobot(String userAtHost, String password, String targetUserAtHost, String file) throws Exception {
		XMPPClient xmppClient = new XMPPClient();
		xmppClient.connect(userAtHost, password);
		IOutgoingFileTransfer transfer = xmppClient.getFileTransferContainer().createOutgoingFileTransfer(xmppClient.getID(targetUserAtHost), null);
		transfer.send(new File(file));
		System.out.println("sending file "+file+" to "+targetUserAtHost);
		while (!transfer.isDone()) {
			System.out.println("sent "+transfer.getBytesSent()+" bytes");
			Thread.sleep(1000);
		}
		System.out.println("done sending "+file);
	}
}
