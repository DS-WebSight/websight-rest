## Description

WebSight Rest Framework for creating (back-end) actions on WebSight.

## How to implement action

### Action specification details (see java-doc)
https://github.com/DS-WebSight/websight-rest/blob/master/websight-rest-framework/src/main/java/pl/ds/websight/rest/framework/annotations/SlingAction.java

### Example actions
https://github.com/DS-WebSight/websight-rest/tree/master/websight-rest-framework/src/test/java/pl/ds/websight/rest/framework/impl

### Details about accessing actions via URL
https://github.com/DS-WebSight/websight-rest/blob/master/websight-rest-framework/src/test/java/pl/ds/websight/rest/framework/impl/RestActionSetupServiceTest.java

## How to build

Build
```
mvn clean install
```

Build with local Sling deployment
```
mvn clean install -P autoInstallBundle
```

## Copyrights

Dynamic Solutions WebSight (Rest Framework) - Rest Framework
Copyright (C) 2013-2021 Dynamic Solutions

This module is part of WebSight Admin, which is released under license
GNU AFFERO GENERAL PUBLIC LICENSE Version 3.

WebSight Admin is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

WebSight Admin is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with WebSight Admin.  If not, see <http://www.gnu.org/licenses/>.