#!/bin/bash

for i in $(docker ps | awk '{print $1}') ; do docker rm -f $i ; done
