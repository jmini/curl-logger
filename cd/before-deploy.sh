#!/usr/bin/env bash
set -x
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_6b751c30f51d_key -iv $encrypted_6b751c30f51d_iv -in codesigning.asc.enc -out codesigning.asc -d
    gpg --fast-import cd/codesigning.asc
fi