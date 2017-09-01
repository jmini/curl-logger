#!/usr/bin/env bash
set -x
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    mvn deploy  -P release --settings cd/mvnsettings.xml
fi