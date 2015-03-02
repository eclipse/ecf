README for TimeServiceConsumer.ds.*.edef product configurations

For all the EDEF product configurations (those with .edef. in their name, for example TimeServiceConsumer.ds.generic.edef.product)
to actually trigger the discovery of the remote service, it's necessary to manually start (via the console) 
the bundle with symbolic ID=com.mycorp.examples.timeservice.consumer.filediscovery.  

To run the example within Eclipse

1) Launch the TimeService host (e.g. TimeServiceHost.ds.generic.noreg.product, from Eclipse product configuration editor Overview tab)
2) Launch the TimeService consumer (e.g. TimeServiceConsumer.ds.generic.noreg.product, from Eclipse product configuration editor Overview tab)
3) In the consumer console, start the filediscovery/EDEF bundle..e.g.:

osgi> start com.mycorp.examples.timeservice.consumer.filediscovery

This should result in the discover and use of the TimeService...with output on the consumer similar to the following:
osgi> start com.mycorp.examples.timeservice.consumer.filediscovery
osgi> Discovered ITimeService via DS
Current time is: 1425262293841
