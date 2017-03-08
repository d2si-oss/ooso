#!/usr/bin/env bash
cd ..
mvn clean
mvn package

cd terraform
terraform apply