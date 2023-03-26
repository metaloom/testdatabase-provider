#!/bin/bash

IMAGE=metaloom/testdatabase-provider
TAG=0.0.1-SNAPSHOT

docker build -t $IMAGE:$TAG .
