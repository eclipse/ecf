package org.eclipse.ecf.tests.sync;

import junit.framework.TestCase;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.internal.tests.sync.Activator;
import org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategyFactory;
import org.eclipse.jface.text.Document;

public class SimulatedClientTests extends TestCase {

	private static final String COLA = "cola";

	public void testSimulatedShare() throws Exception {

		IDocumentSynchronizationStrategyFactory factory = Activator
				.getDefault().getColaSynchronizationStrategyFactory();

		assertNotNull(factory);

		ID channelId = IDFactory.getDefault().createStringID(COLA);

		// TODO working in progress
		// Simulate a typing in a one-way. Initiator -> Receiver

		// Document to control
		String text = "package TestPackage;\n" + "/*\n" + "* comment\n"
				+ "*/\n" + "	public class Class {\n" + "		// comment1\n"
				+ "		public void method1() {\n" + "		}\n" + "		// comment2\n"
				+ "		public void method2() {\n" + "		}\n" + "	}\n";

		Document doc = new Document(text);

		Initiator initiator = new Initiator(factory
				.createDocumentSynchronizationStrategy(channelId, true), doc);

		Receiver receiver = new Receiver(factory
				.createDocumentSynchronizationStrategy(channelId, false), doc);

		initiator.setReceiverQueue(receiver.getQueue());

		// receiver.setInitiatorQueue(initiator.getQueue());

		initiator.start();
		receiver.start();

		// FIXME
		Thread.sleep(2000);

		String initiatorText = initiator.getDocument().get();
		String receiverText = receiver.getDocument().get();
		assertEquals(receiverText, initiatorText);

	}

	public void testSharedDocClient() throws Exception {
		IDocumentSynchronizationStrategyFactory factory = Activator
				.getDefault().getColaSynchronizationStrategyFactory();

		assertNotNull(factory);
		ID channelId = IDFactory.getDefault().createStringID(COLA);

		String text = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		SharedDocClient client1 = new SharedDocClient("client1",factory
				.createDocumentSynchronizationStrategy(channelId, true),text);
		
		SharedDocClient client2 = new SharedDocClient("client2",factory
				.createDocumentSynchronizationStrategy(channelId, false),text);
		
		client1.setOtherQueue(client2.getQueue());
		client2.setOtherQueue(client1.getQueue());
		
		client1.start();
		client2.start();
		
		Thread.sleep(20000);
		
		client1.close();
		client2.close();
		
		String client1Result = client1.getDocumentText();
		System.out.println("client1Result="+client1Result);
		String client2Result = client2.getDocumentText();
		System.out.println("client2Result="+client2Result);

		assertEquals(client1Result, client2Result);
	}
}
