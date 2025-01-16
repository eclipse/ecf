## Eclipse Communication Framework
ECF is a set of frameworks supporting multiple types of inter-process communication

Current version: 3.15.3

### Download/Install into Bndtools/Eclipse
NEW in 3.15.3: Feature for Remote Services tooling that depends upon on [Bndtools](https://bndtools.org/) 7.1+.  The ECF tools use bndtools project, workspace, service templates, along with wizards for remote services project creation and configuration.  The feature requires that Bndtools be [installed](https://bndtools.org/installation.html) into a recent version of Eclipse before installing the ECF Bndtools SDK feature.

<b>Name</b>:  ECF 3.15.2

<b>Update Site URL</b>:  [https://download.eclipse.org/rt/ecf/latest/site.p2](https://download.eclipse.org/rt/ecf/latest/site.p2)

Update Site as Zip:  [org.eclipse.ecf.sdk_3.15.3.v20250115-0413.zip](https://www.eclipse.org/downloads/download.php?file=/rt/ecf/3.15.3/org.eclipse.ecf.sdk_3.15.3.v20250115-0413.zip)

### Download/Install into [Apache Karaf 4.4+](https://karaf.apache.org/)
https://download.eclipse.org/rt/ecf/latest/karaf-features.xml

## Key ECF APIs

### OSGi Remote Services
ECF provides a fully-compliant and multi-provider implementation of the [OSGi Remote Services](https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.remoteservices.html) and [Remote Services Admin/RSA](https://docs.osgi.org/specification/osgi.cmpn/7.0.0/service.remoteserviceadmin.html).  A number of providers are available in this repo, but there are also many providers available at the [ECF github organization](https://github.com/ECF).

ECF is currently the RS/RSA implementation used by the [OSGi Test Compatibilty Kit (TCK)](https://github.com/osgi/osgi)

### Eclipse Install/Update File Transfer
ECF has an API called 'filetransfer' that is depended upon by the [Eclipse platform](https://github.com/eclipse-platform)

## ECF Github Organization
In addition to this repo, ECF also has an [ECF Organization with a number of other repos](https://github.com/ECF) containing Remote Services distribution and discovery providers (e.g. grpc, etcd discovery, hazelcast, others). Most of these repos provide distribution or discovery providers that depend upon the core implementation provided by this repo.  

## Wiki
See the [ECF Wiki](https://wiki.eclipse.org/Eclipse_Communication_Framework_Project) for examples, tutorials, other documentation, as well as plans for future releases.

To contribute or find out what's going on right now, please join the [ecf-dev mailing list](https://accounts.eclipse.org/mailing-list/ecf-dev) or contact project lead Scott Lewis at github username: scottslewis

## Services, Training and Support
For Remote Services training, support, or expert OSGi or Eclipse development please contact slewis at composent.com, github: scottslewis via email or bring it up on the ECF dev mailing list. 

### Contributing to ECF
Contributions are always welcome!
See [CONTRIBUTING.md](CONTRIBUTING.md)
