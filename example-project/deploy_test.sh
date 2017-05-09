#!/usr/bin/env bash

set -e

JOB_INPUT_BUCKET=$(cat src/main/resources/jobInfo.json | jq -r '.jobInputBucket' | cut -d'/' -f 1)
MAP_OUTPUT_BUCKET=$(cat src/main/resources/jobInfo.json | jq -r '.mapperOutputBucket')
REDUCE_OUTPUT_BUCKET=$(cat src/main/resources/jobInfo.json | jq -r '.reducerOutputBucket')

aws --endpoint-url http://0.0.0.0:4567 s3api create-bucket --bucket ${JOB_INPUT_BUCKET}
aws --endpoint-url http://0.0.0.0:4567 s3api create-bucket --bucket ${MAP_OUTPUT_BUCKET}
aws --endpoint-url http://0.0.0.0:4567 s3api create-bucket --bucket ${REDUCE_OUTPUT_BUCKET}
aws --endpoint-url http://0.0.0.0:4567 s3 cp ../test-data/ s3://my-dataset/ --recursive