#!/bin/bash

docker build -t docker-java:webchecker .
docker run -p 8085:8085 docker-java:webchecker