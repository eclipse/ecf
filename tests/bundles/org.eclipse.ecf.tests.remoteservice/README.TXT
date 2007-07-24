Readme for Test Plugin:  org.eclipse.ecf.tests.remoteservice.

For some of the test cases in this bundle, it's necessary to provide
as system properties the usernames and passwords for two users
and the name of a chat room available on given server.  If these
names are not given, then the tests will fail because they
won't be able to login to accounts to run the test cases.

The System Properties to specify are:

username0
password0
username1
password1
chat.room.name

So, for example, if slewis@ecf.eclipse.or and fliwatuet@ecf.eclipse.org are the two
accounts that should be used to run the tests, and the chat room ins "ecf" the
properties would look like this:

-Dusername0=slewis@ecf.eclipse.org \
-Dpassword0=username0password \
-Dusername1=fliwatuet@ecf.eclipse.org \
-Dpassword1=username1password \
-Dchat.room.name=ecf

Note that username0password and username1password would be the values of the 
respective account's passwords.

