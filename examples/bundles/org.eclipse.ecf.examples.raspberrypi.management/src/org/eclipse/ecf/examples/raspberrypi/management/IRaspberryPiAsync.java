package org.eclipse.ecf.examples.raspberrypi.management;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IRaspberryPiAsync {

	public CompletableFuture<Map<String,String>> getSystemPropertiesAsync();
	
}
