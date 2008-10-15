package org.eclipse.ecf.tests.sync;

import junit.framework.TestCase;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.internal.tests.sync.Activator;
import org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategyFactory;
import org.eclipse.jface.text.Document;

public class SimulatedClient extends TestCase {

	private static final String COLA = "cola";

	public void testSimulatedShare() throws Exception {

		IDocumentSynchronizationStrategyFactory factory = Activator
				.getDefault().getColaSynchronizationStrategyFactory();

		assertNotNull(factory);

		ID channelId = IDFactory.getDefault().createStringID(COLA);

		// TODO working in progress
		//Simulate a typing in a one-way. Initiator -> Receiver

		//Document to control
		String text=
			"package TestPackage;\n" +
			"/*\n" +
			"* comment\n" +
			"*/\n" +
			"	public class Class {\n" +
			"		// comment1\n" +
			"		public void method1() {\n" +
			"		}\n" +
			"		// comment2\n" +
			"		public void method2() {\n" +
			"		}\n" +
			"	}\n";

		Document doc = new Document(text);
		
		Initiator initiator = new Initiator(factory
				.createDocumentSynchronizationStrategy(channelId, true), doc);

		Receiver receiver = new Receiver(factory
				.createDocumentSynchronizationStrategy(channelId, false), doc);
		

		initiator.setReceiverQueue(receiver.getQueue());

		// receiver.setInitiatorQueue(initiator.getQueue());

		initiator.start();
		receiver.start();

		//FIXME
		Thread.sleep(2000);

		String initiatorText = initiator.getDocument().get();
		String receiverText = receiver.getDocument().get();
		assertEquals(receiverText, initiatorText);


	}

}
