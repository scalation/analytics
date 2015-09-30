#!/usr/bin/env bash

SCALATION_URL="http://cobweb.cs.uga.edu/~jam/scalation_1.2.tar.gz"
PWD="$(pwd)"

# setup directory
mkdir -p scalation
cd scalation

# download and untar scalation
wget "$SCALATION_URL"
tar zxvf *.tar.gz

# find build.sbt
BUILD_SBT="$(find . -name "build.sbt")"
PROJ_DIR="$(dirname "$BUILD_SBT")"

# build and publish locally
cd "$PROJ_DIR"
sbt compile publish-local

# remove the directory
cd "$PWD"
rm -rf scalation

