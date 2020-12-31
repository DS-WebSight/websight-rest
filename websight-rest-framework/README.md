### How to implement action

#### Action specification details (see java-doc)
https://bitbucket.ds.pl/projects/WS/repos/websight-rest/browse/websight-rest-framework/src/main/java/pl/ds/websight/rest/framework/annotations/SlingAction.java

#### Example actions
https://bitbucket.ds.pl/projects/WS/repos/websight-rest/browse/websight-rest-framework/src/test/java/pl/ds/websight/rest/framework/impl

#### Details about accessing actions via URL
https://bitbucket.ds.pl/projects/WS/repos/websight-rest/browse/websight-rest-framework/src/test/java/pl/ds/websight/rest/framework/impl/RestActionSetupServiceTest.java

### Usage:

Build
```
mvn clean install
```

Build with local Sling deployment
```
mvn clean install -P autoInstallBundle
```