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
Discovered ITimeServiceAsync via DS
Current time via future.get is: 1425262293906

The OSGi console 'start' command can also use the bundleId rather than the symbolic id, but in that case
the bundleId has to be found first.  The easiest way to find the bundleId in the console is to use the 'short status'/ss command...e.g.

osgi> ss
"Framework is launched."


id	State       Bundle
0	ACTIVE      org.eclipse.osgi_3.10.0.v20140407-2102
1	ACTIVE      org.eclipse.ecf.identity_3.4.0.qualifier
2	ACTIVE      org.eclipse.equinox.concurrent_1.1.0.v20130327-1442
5	ACTIVE      org.eclipse.core.contenttype_3.4.200.v20140207-1251
6	ACTIVE      org.eclipse.ecf.remoteservice_8.5.0.qualifier
7	ACTIVE      org.apache.felix.gogo.runtime_0.10.0.v201209301036
8	ACTIVE      com.mycorp.examples.timeservice.consumer.ds_1.0.0.qualifier
9	ACTIVE      org.eclipse.equinox.registry_3.5.400.v20140324-1548
10	ACTIVE      javax.xml_1.3.4.v201005080400
11	ACTIVE      org.eclipse.ecf.osgi.services.remoteserviceadmin_4.2.0.qualifier
12	ACTIVE      org.eclipse.ecf.console_1.0.0.qualifier
13	ACTIVE      org.eclipse.ecf.discovery_5.0.0.qualifier
14	ACTIVE      org.eclipse.equinox.common_3.6.200.v20130402-1505
15	ACTIVE      org.apache.felix.gogo.shell_0.10.0.v201212101605
16	ACTIVE      org.eclipse.equinox.preferences_3.5.200.v20140224-1527
18	ACTIVE      org.eclipse.core.jobs_3.6.0.v20140407-1602
19	ACTIVE      org.eclipse.ecf.remoteservice.asyncproxy_2.0.0.qualifier
20	ACTIVE      org.eclipse.equinox.ds_1.4.200.v20131126-2331
21	ACTIVE      org.eclipse.ecf_3.4.0.qualifier
22	ACTIVE      org.eclipse.equinox.console_1.1.0.v20140131-1639
23	ACTIVE      org.eclipse.ecf.osgi.services.distribution_2.1.0.qualifier
24	ACTIVE      org.eclipse.osgi.services_3.4.0.v20140312-2051
25	ACTIVE      org.eclipse.equinox.util_1.0.500.v20130404-1337
26	ACTIVE      com.mycorp.examples.timeservice_1.1.0.qualifier
27	ACTIVE      org.eclipse.ecf.sharedobject_2.5.0.qualifier
28	ACTIVE      org.eclipse.ecf.provider.remoteservice_4.1.0.qualifier
29	ACTIVE      org.eclipse.osgi.services.remoteserviceadmin_1.6.0.qualifier
30	ACTIVE      org.eclipse.equinox.event_1.3.100.v20140115-1647
31	ACTIVE      org.eclipse.ecf.osgi.services.remoteserviceadmin.proxy_1.0.0.qualifier
32	ACTIVE      org.apache.felix.gogo.command_0.10.0.v201209301215
33	ACTIVE      org.eclipse.ecf.provider_4.5.0.qualifier
34	RESOLVED    com.mycorp.examples.timeservice.consumer.filediscovery_1.2.0.qualifier

Then the start command can be given with for the com.mycorp.examples.timeservice.consumer.filediscovery bundleId
given in the first column.  For example:

osgi> start 34


