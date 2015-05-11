# event-store-maven-plugin
This [Maven](https://maven.apache.org/) plugin provides goals that start/stop the [event store](https://github.com/EventStore/EventStore).

An example how to use it can be found here: [test-project](https://github.com/fuinorg/event-store-maven-plugin/tree/master/es-maven-test/src/test/resources/test-project)

Remember to name your test class according to [Maven Failsafe Plugin](http://maven.apache.org/surefire/maven-failsafe-plugin/)'s naming [pattern](http://maven.apache.org/surefire/maven-failsafe-plugin/examples/inclusion-exclusion.html).

To execute your integration test use:
```mvn clean verify``` or ```mvn verify```

###Snapshots

Snapshots can be found on the [OSS Sonatype Snapshots Repository](http://oss.sonatype.org/content/repositories/snapshots/org/fuin "Snapshot Repository"). 

Add the following to your .m2/settings.xml to enable snapshots in your Maven build:

```xml
<repository>
    <id>sonatype.oss.snapshots</id>
    <name>Sonatype OSS Snapshot Repository</name>
    <url>http://oss.sonatype.org/content/repositories/snapshots</url>
    <releases>
        <enabled>false</enabled>
    </releases>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```
