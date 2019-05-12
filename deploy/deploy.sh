#!/usr/bin/env bash

buildProdDockerImage() {
    sbt dist

    unzip target/universal/*.zip
    rm -rf chirper-api
    mv chirper-api* chirper-api

    docker build -t chirper-api -f deploy/Dockerfile-prod .

    rm -rf chirper-api
}
