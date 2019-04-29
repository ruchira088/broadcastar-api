#!/usr/bin/env bash

buildProdDockerImage() {
    sbt dist

    unzip target/universal/*.zip
    rm -rf broadcastar-api
    mv broadcastar-api* broadcastar-api

    docker build -t broadcastar-api -f deploy/Dockerfile-prod .

    rm -rf broadcastar-api
}