#!/usr/bin/env bash
set -x
if [ "$TRAVIS_BRANCH" = 'release' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    mvn deploy  -P release --settings cd/mvnsettings.xml
fi