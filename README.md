## Eclipse Communication Framework
ECF is a set of APIs/frameworks/types of inter-process communication

Current version: 3.15.5

### Download/Install into Bndtools or Eclipse
NEW: Feature for Remote Services tooling that enhances [Bndtools](https://bndtools.org/) 7.1+.  Theses tools use bndtools project, workspace, service templates, along with OSGi services wizards for building OSGi remote services.  The feature requires that Bndtools 7.1+ be [installed](https://bndtools.org/installation.html) into a recent version of Eclipse.

<b>Name</b>:  ECF 3.15.5

<b>Update Site URL</b>:  [https://download.eclipse.org/rt/ecf/latest/site.p2](https://download.eclipse.org/rt/ecf/latest/site.p2)

Update Site as Zip:  [org.eclipse.ecf.sdk_3.15.5.v20250124-1843.zip](https://www.eclipse.org/downloads/download.php?file=/rt/ecf/3.15.5/org.eclipse.ecf.sdk_3.15.5.v20250124-1843.zip)

[Javadocs](https://download.eclipse.org/rt/ecf/snapshot/javadoc/)

### Install into [Bndtools 7.1](https://bndtools.org/)

There are now [Oomph](https://projects.eclipse.org/projects/tools.oomph) Setups that will automatically install and configure Eclipse, Bndtools 7.1 and ECF 3.15.5 [see here](https://github.com/bndtools/bndtools.p2.repo/tree/master/setup/ecf)

### Download/Install into [Apache Karaf 4.4+](https://karaf.apache.org/)
https://download.eclipse.org/rt/ecf/latest/karaf-features.xml

## Key ECF APIs

### OSGi Remote Services
ECF provides a fully-compliant and multi-provider implementation of the [OSGi Remote Services](https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.remoteservices.html) and [Remote Services Admin/RSA](https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.remoteserviceadmin.html).  A number of providers are available in this repo, but there are also many providers available at the [ECF github organization](https://github.com/ECF).

ECF is the OSGi R8 RS/RSA implementation in the [OSGi Test Compatibilty Kit (TCK)](https://github.com/osgi/osgi)

### Eclipse Install/Update File Transfer
ECF filetransfer is used by the [Eclipse IDE](https://github.com/eclipse-platform)

## ECF Github Organization
ECF  has an [organization with a number of other repos](https://github.com/ECF) containing Remote Services distribution and discovery providers (e.g. grpc, etcd discovery, hazelcast, JMS, JGroups, xmlrpc-based distribution providers, examples, others). Most of these repos provide distribution or discovery providers that depend upon the core remote services/RSA implementation provided by this repo.  

## Wiki
See the [ECF Wiki](https://wiki.eclipse.org/Eclipse_Communication_Framework_Project) for examples, tutorials, other documentation, as well as plans for future releases.

To contribute or find out what's going on right now, please join the [ecf-dev mailing list](https://accounts.eclipse.org/mailing-list/ecf-dev) or contact project lead Scott Lewis at github username: scottslewis

## Services, Training and Support
For Remote Services training, support, or expert OSGi or Eclipse development please contact slewis at composent.com, alt: scottslewis at gmail.com via email or bring it up on the [ecf-dev mailing list](https://accounts.eclipse.org/mailing-list/ecf-dev)  

### Contributing to ECF
Contributions are always welcome!
See [CONTRIBUTING.md](CONTRIBUTING.md)
