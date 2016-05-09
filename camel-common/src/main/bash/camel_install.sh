#!/usr/bin/env bash

git clone https://github.com/apache/camel.git /tmp/apache-camel
./.travis-mvn.sh -f /tmp/apache-camel/pom.xml install -Dinvoker.debug=false -Dtest=false | grep -v "Downloaded: "
