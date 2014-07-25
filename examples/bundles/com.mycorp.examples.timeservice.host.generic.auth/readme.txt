This examples shows how to configure a generic provider...both the host/server and the consumer/client so that authentication credentials can be sent as part of the connection formed to access and use a remote service.

There are multiple ways to configure an ECF container instance (server or client) prior to use for remote services.   In order to show this wrt configuring authentication, in this example I did it in different ways for the host/server and client/consumer respectively.

For the host/server, I created the generic server container instance via the IContainerManager, and then configured it with an IConnectHandlerPolicy in the host Activator start [1].  This is all done *prior* to the registration and export of the remote service that occurs on line 43 of [1].

For the consumer/client, I registered a new instance of IConsumerContainerSelector in the Activator [2], and this consumer container selector's createContainer method gets called *when the remote service is discovered for the first time*.   The createContainer method [2] not only creates the ecf.generic.client container (in super class), but it also sets an instance of IConnectInitiatorPolicy, which gets called to create the connectData holding the appropriate credentials.

