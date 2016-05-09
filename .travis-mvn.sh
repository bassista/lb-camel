#!/usr/bin/env bash

set -e

export MAVEN_OPTS="-Dmaven.repo.local=$HOME/.m2/repository -Xms2048m -Xmx2048m"
export MAVEN_SKIP_RC="true"

./mvnw -s ./.travis-settings.xml "$@"
