Contributing to ECF
===================

Contributions to the Eclipse Communication Project (ECF) are most welcome. 
There are many ways to contribute, from entering high quality bug reports, 
to contributing code or documentation changes. For a complete guide, see 
the Eclipse Contributor guide [1] and read the Gerrit wiki page [2].

GitHub tracker and pull requests are currently not supported by our
contribution process.

[1]: http://eclipse.org/contribute/
[2]: http://wiki.eclipse.org/Gerrit

Building the SDK from Source
----------------------------

In order to build the ECF SDK on the command line, you need Apache Maven at
least version 3.1.1.

To build against the default target platform (usually based on the newest
release of Eclipse) invoke the following from the root directory of the git
repository (i.e. the directory this file is in):

    $ mvn clean verify

To build ECF against a specific target platform:

    $ mvn clean verify -Dtarget-platform=neon

If the build is successful, a valid p2 repository will be generated in the
"releng/org.eclipse.ecf.releng.repository/target/repository" directory. This
may be used directly with Eclipse, for example, to install the newly built
ECF SDK directly into your Eclipse installation.

To build the p2 repository containing packed and signed bundles, necessary for
a release:

    $ mvn clean verify -Ppack-and-sign

It is possible to install the ECF components into your local Maven repository
for consumption by other Maven-based projects:

    $ mvn clean install

Tests are disabled by default, so to enable the execution of the test suite 
during the build:

    $ mvn clean verify -DskipTests=false

All the above options may be combined with one another.

Adding New Bundles/Features
---------------------------

OSGi bundles and Eclipse features can be thought of as Maven modules. So to
add a new bundle or feature to the build can easily be done by adding a new
module line to the master pom.xml, which is located at the root of this git
repository. The new bundle or feature just needs its own pom.xml in its
directory. In most cases, the new pom.xml file can be trivially simple, for
example:

```
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.eclipse.ecf</groupId>
    <artifactId>ecf-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../../../</relativePath>
  </parent>
  <groupId>org.eclipse.ecf</groupId>
  <!-- Artifact ID must be same as bundle or feature ID -->
  <artifactId>org.eclipse.ecf.core.feature</artifactId>
  <!-- Version must be same as bundle or feature version, but suffixed with "-SNAPSHOT" instead of ".qualifier" -->
  <version>1.3.0-SNAPSHOT</version>
  <!-- Packaging must be "eclipse-feature", "eclipse-plugin" or "eclipse-test-plugin" -->
  <packaging>eclipse-feature</packaging>
</project>
```

Bundle Version Maintenance
--------------------------

When a version number is increased, either in a bundle's "MANIFEST.MF" file
or in a feature's "feature.xml" file, the version in the corresponding
pom.xml file must also be changed to match. This can be done manually, or
by invoking a Tycho utility to synchronise your pom.xml versions:

    $ mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:update-pom

And the changes to the pom.xml files should be committed with the rest of
the change.

