# event-store-maven-plugin
This [Maven](https://maven.apache.org/) plugin provides goals that start/stop the [event store](https://github.com/EventStore/EventStore).

:warning: No longer under development. Use the [Docker image](https://hub.docker.com/r/eventstore/eventstore/) for testing instead. :warning:

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.fuin.esmp/es-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.fuin.esmp/es-maven-plugin/)
[![LGPLv3 License](http://img.shields.io/badge/license-LGPLv3-blue.svg)](https://www.gnu.org/licenses/lgpl.html)
[![Java Development Kit 1.8](https://img.shields.io/badge/JDK-1.8-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

### Getting started
Just add the plugin to your Maven POM:
```xml
<plugin>	
	<groupId>org.fuin.esmp</groupId>
	<artifactId>es-maven-plugin</artifactId>
	<version>0.5.1</version>
	<executions>
		<execution>
			<goals>
				<goal>download</goal>
				<goal>certificate</goal>
				<goal>start</goal>
				<goal>stop</goal>
			</goals>
		</execution>
	</executions>
    <!-- The default is '--mem-db=TRUE' if you don't include the configuration -->
    <configuration>
    	<!-- Selects the correct OS sub type by name for example 'Ubuntu 18' vs 'Ubuntu 16' -->
        <download-os-qualifier>Ubuntu 18</download-os-qualifier>
    	<!-- Creates a self-signed X509 certificate -->
    	<certificate-file>${project.build.directory}/domain.p12</certificate-file>
    	<!-- Start the event store in-memory with some parameters -->
        <arguments>			         
            <argument>--mem-db=FALSE</argument>
            <argument>--stats-period-sec=3000</argument>
            <argument>--ext-tcp-port=7773</argument>
            <argument>--ext-secure-tcp-port=7779</argument>
            <argument>--certificate-file=${project.build.directory}/domain.p12</argument>
            <argument>--run-projections=All</argument>
            <argument>--log=/tmp/log-eventstore</argument>
        </arguments>
    </configuration>
</plugin>
```
This will download the latest event store version to the 'target' build directory, create a self-signed certificate and start it before the integration tests will run. After execution the event store will be stopped.

A full example how to use it can be found here: [test-project](https://github.com/fuinorg/event-store-maven-plugin/tree/master/es-maven-test/src/test/resources/test-project)

Remember to name your test class according to [Maven Failsafe Plugin](http://maven.apache.org/surefire/maven-failsafe-plugin/)'s naming [pattern](http://maven.apache.org/surefire/maven-failsafe-plugin/examples/inclusion-exclusion.html).

To execute your integration test use:
```mvn clean verify``` or ```mvn verify```

### Using a certain event store version
If you want to use a fix event store version version you can use the `download-url` configuration property to set it in the plugin.
```xml
<plugin>	
	<groupId>org.fuin.esmp</groupId>
	<artifactId>es-maven-plugin</artifactId>
	<version>0.5.1</version>
	<executions>
		<execution>
			<goals>
				<goal>download</goal>
				<goal>start</goal>
				<goal>stop</goal>
			</goals>
		</execution>
	</executions>
    <configuration>
        <download-url>https://eventstore.org/downloads/ubuntu/EventStore-OSS-Linux-Ubuntu-18.04-v5.0.6.tar.gz</download-url>
    </configuration>
</plugin>
```

### Changing the location of the event store version JSON file
In the past the Event Store team moved the JSON file that contains download information for event store versions to another location.
In case this happens again you can quickly use the `version-url` configuration property to change it in the plugin.
```xml
<plugin>	
	<groupId>org.fuin.esmp</groupId>
	<artifactId>es-maven-plugin</artifactId>
	<version>0.5.1</version>
	<executions>
		<execution>
			<goals>
				<goal>download</goal>
				<goal>start</goal>
				<goal>stop</goal>
			</goals>
		</execution>
	</executions>
    <configuration>
        <version-url>https://raw.githubusercontent.com/EventStore/eventstore.org/master/_data/downloads.json</version-url>
    </configuration>
</plugin>
```

### Snapshots

Snapshots can be found on the [OSS Sonatype Snapshots Repository](http://oss.sonatype.org/content/repositories/snapshots/org/fuin "Snapshot Repository"). 

Add the following to your .m2/settings.xml to enable snapshots in your Maven build:

```xml
<pluginRepository>
    <id>sonatype.oss.snapshots</id>
    <name>Sonatype OSS Snapshot Repository</name>
    <url>http://oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</pluginRepository>
```
