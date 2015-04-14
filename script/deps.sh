#!/usr/bin/env bash

SCALATION_URL=http://cobweb.cs.uga.edu/~jam/scalation_1.1.1.tar.gz
PWD=$(pwd)

# setup directory
mkdir -p scalation
cd scalation

# download and untar scalation
wget -o scalation.tar.gz "$SCALATION_URL"
tar zxvf scalation.tar.gz

# find build.sbt
BUILD_SBT=$(find . -name "build.sbt")
PROJ_DIR=$(dirname "$BUILD_SBT")

# build and publish locally
cd $PROJ_DIR
sbt ++$TRAVIS_SCALA_VERSION compile publish-local

# remove the directory
cd $PWD
rm -rf scalation

