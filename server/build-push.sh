#!/bin/bash

IMAGE=metaloom/postgresql-testdatabase-provider
TAG=0.0.1-SNAPSHOT

docker build -t $IMAGE:$TAG .
docker push $IMAGE:$TAG
