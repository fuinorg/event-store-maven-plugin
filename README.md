# event-store-maven-plugin
This [Maven](https://maven.apache.org/) plugin provides goals that start/stop the [event store](https://github.com/EventStore/EventStore).

An example how to use it can be found here: [test-project](https://github.com/fuinorg/event-store-maven-plugin/tree/master/es-maven-test/src/test/resources/test-project)

Remember to name your test class according to [Maven Failsafe Plugin](http://maven.apache.org/surefire/maven-failsafe-plugin/)'s naming [pattern](http://maven.apache.org/surefire/maven-failsafe-plugin/examples/inclusion-exclusion.html).

To execute your integration test use:
```mvn clean verify``` or ```mvn verify```

