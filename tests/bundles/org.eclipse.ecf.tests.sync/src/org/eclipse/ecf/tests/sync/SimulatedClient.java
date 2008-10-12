package org.eclipse.ecf.tests.sync;

import junit.framework.TestCase;

import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.internal.tests.sync.Activator;
import org.eclipse.ecf.sync.doc.IDocumentChange;
import org.eclipse.ecf.sync.doc.IDocumentChangeMessage;
import org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategy;
import org.eclipse.ecf.sync.doc.IDocumentSynchronizationStrategyFactory;
import org.eclipse.ecf.sync.doc.messages.DocumentChangeMessage;

public class SimulatedClient extends TestCase {
	
	private static final String COLA = "cola";

	public void testSimulatedShare() throws Exception{
		
		IDocumentSynchronizationStrategyFactory  factory = Activator.getDefault().getColaSynchronizationStrategyFactory();
		
		assertNotNull(factory);
		
		ID channelId = IDFactory.getDefault().createStringID(COLA);
		
		//TODO working in progress, it is still a "dummy" test
		
		IDocumentSynchronizationStrategy initiator = factory.createDocumentSynchronizationStrategy(channelId, true);
		IDocumentSynchronizationStrategy receiver = factory.createDocumentSynchronizationStrategy(channelId, false);
		
		DocumentChangeMessage message = new DocumentChangeMessage(0, 4, "cola");
		IDocumentChangeMessage messages [] = initiator.registerLocalChange(message);
		
		IDocumentChange[] changes = receiver.transformRemoteChange((IDocumentChange) messages[0]);
		assertEquals("cola", changes[0].getText());
		
		
	}

}
