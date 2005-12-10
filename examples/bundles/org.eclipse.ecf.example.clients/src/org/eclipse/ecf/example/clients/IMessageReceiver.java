package org.eclipse.ecf.example.clients;

public interface IMessageReceiver {
	public void handleMessage(String from, String msg);
}
