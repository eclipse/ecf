README for TimeServiceConsumer.rosgi.ds.wss.noreg.product

The successful use of this provider for this example or 
otherwise depends upon

Having a built, packaged, and installed version of these two 
additional bundles:
   a) java_websocket
   b) ch.ethz.iks.r_osgi.transport.http
   
The source for these two bundles is available via the ECF github
Websockets repository located here:  https://github.com/ECF/Websockets
Note that there is also a feature for building these two bundles via 
eclipse via the feature project located in 
features/org.eclipse.ecf.remoteservices.rosgi.ws.feature

Using EDEF Filediscovery to trigger TimeServiceConsumer discovery

When using the EDEF product configurations to trigger the discovery of the remote service, 
it's necessary to manually start (via the console) the bundle with symbolic 
ID=com.mycorp.examples.timeservice.consumer.filediscovery.rosgi.ws

To run the example within Eclipse

1) Launch the TimeService host (e.g. TimeServiceHost.ds.generic.noreg.product, from Eclipse product configuration editor Overview tab)
2) Launch the TimeService consumer (TimeServiceConsumer.ds.rosgi.wss.noreg.product, from Eclipse product configuration editor Overview tab)
3) In the consumer console, start the filediscovery/EDEF bundle..e.g.:

osgi> start com.mycorp.examples.timeservice.consumer.filediscovery.rosgi.ws

This should result in the discover and use of the TimeService...with output on the consumer similar to the following:

osgi> start com.mycorp.examples.timeservice.consumer.filediscovery.rosgi.ws
osgi> Discovered ITimeService via DS
Current time is: 1425262293841
