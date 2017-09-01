#!/usr/bin/env bash
set -x
if [ "$TRAVIS_BRANCH" = 'release' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_0705285cb685_key -iv $encrypted_0705285cb685_iv -in cd/codesigning.asc.enc -out cd/codesigning.asc -d
    gpg --fast-import cd/codesigning.asc
fi
