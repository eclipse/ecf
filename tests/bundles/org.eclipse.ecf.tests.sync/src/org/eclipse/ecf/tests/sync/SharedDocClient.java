package org.eclipse.ecf.tests.sync;

import java.util.Random;

import org.eclipse.ecf.sync.IModelChange;
import org.eclipse.ecf.sync.IModelChangeMessage;
import org.eclipse.ecf.sync.IModelSynchronizationStrategy;
import org.eclipse.ecf.sync.ModelUpdateException;
import org.eclipse.ecf.sync.SerializationException;
import org.eclipse.ecf.sync.doc.IDocumentChange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

public class SharedDocClient extends Thread {

	private String name;
	
	private IDocument document;
	
	private SimpleQueue localQueue;
	private SimpleQueue otherQueue;

	private IModelSynchronizationStrategy syncStrategy;

	private Random random = new Random();
	
	public SharedDocClient(String name, IModelSynchronizationStrategy syncStrategy, String startText) {
		this.name = name;
		this.syncStrategy = syncStrategy;
		this.document = new Document(startText);
		this.localQueue = new SimpleQueue();
	}

	public void start() {
		Thread localThread = new Thread(new Runnable() {
			public void run() {
				while (!localQueue.isStopped()) {
					sleep(500,2000);
					localQueue.enqueue(new Runnable() {
						public void run() {
							processLocalChange(getLocalDocumentChange());
						}});
				}
			}});
		localThread.setDaemon(true);
		localThread.start();
		super.start();
	}
	
	public SimpleQueue getQueue() {
		return this.localQueue;
	}
	
	public void setOtherQueue(SimpleQueue otherQueue) {
		this.otherQueue = otherQueue;
	}

	public String getDocumentText() {
		return document.get();
	}
	private void sleep(int minimum, int max) {
		try {
			Thread.sleep(minimum+random.nextInt(max-minimum));
		} catch (InterruptedException e) {
			// return
		}
	}
	
	public void run() {
		while (true) {
			Object o = localQueue.dequeue();
			if (o == null) return;
			if (o instanceof Runnable) ((Runnable) o).run();
			if (o instanceof byte[]) {
				processRemoteMessage((byte []) o);
			}
		}
	}
	
	private void processLocalChange(IDocumentChange localChange) {
		if (localChange != null) {
			applyChangeToLocalDocument(true,localChange);
			// Then register with local synchronizer
			IModelChangeMessage[] changeMessages = syncStrategy.registerLocalChange(localChange);
			// Then 'send' to other
			deliverChangeToOther(changeMessages);
		}
	}
	
	private void applyChangeToLocalDocument(boolean local, IDocumentChange change) {
		// First apply to local copy of document
		System.out.println();
		System.out.println(name+";doc="+document.get());
		System.out.println(name+(local?";localChange":";remoteChange")+";t="+System.currentTimeMillis()+";offset="+change.getOffset()+";length="+change.getLengthOfReplacedText()+";text="+change.getText());
		try {
			document.replace(change.getOffset(), change.getLengthOfReplacedText(), change.getText());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		System.out.println(name+";doc="+document.get());
	}
	
	private void processRemoteMessage(byte [] msg) {
		if (msg != null) {
			try {
				IModelChange change = syncStrategy.deserializeRemoteChange(msg);
				IDocumentChange[] documentChanges = (IDocumentChange[]) syncStrategy.transformRemoteChange(change);
				for(int i=0; i < documentChanges.length; i++) {
					try {
						documentChanges[i].applyToModel(document);
					} catch (ModelUpdateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (SerializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void deliverChangeToOther(
			IModelChangeMessage[] changeMessages) {
		for(int i=0; i < changeMessages.length; i++) {
			try {
				final byte [] bytes = changeMessages[i].serialize();
				otherQueue.enqueue(bytes);
			} catch (SerializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private IDocumentChange getLocalDocumentChange() {
		final int offset = random.nextInt(document.getLength());
		final int length = random.nextInt(2);
		final String text = (random.nextInt(2)==0)?"":">";
		return new IDocumentChange() {
			public int getLengthOfReplacedText() {
				return length;
			}
			public int getOffset() {
				return offset;
			}
			public String getText() {
				return text;
			}
			public Object getAdapter(Class adapter) {
				return null;
			}
			public void applyToModel(Object model) throws ModelUpdateException {
				IDocument doc = (IDocument) model;
				System.out.println();
				System.out.println(name+";doc="+doc.get());
				System.out.println(name+";remoteChange"+";t="+System.currentTimeMillis()+";offset="+getOffset()+";length="+getLengthOfReplacedText()+";text="+getText());
				try {
					document.replace(getOffset(), getLengthOfReplacedText(), getText());
				} catch (BadLocationException e) {
					throw new ModelUpdateException("Exception in model update",this,model);
				}
				System.out.println(name+";doc="+doc.get());
			}
		};
	}
	
	public void close() {
		localQueue.close();
	}
}
