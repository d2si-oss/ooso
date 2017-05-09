#!/usr/bin/env bash

set -e

./package.sh
cd terraform
terraform apply