#!/usr/bin/env bash

git clone https://github.com/OrbitzWorldwide/consul-client.git /tmp/consul-client
./mvnw -f /tmp/consul-client/pom.xml clean install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true
