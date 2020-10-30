#!/usr/bin/env bash

./gradlew shadowJar

gcloud functions deploy crawler \
  --runtime java11 \
  --entry-point ch.homegate.crawler.Main \
  --source build/libs \
  --trigger-topic crawl \
  --env-vars-file gcp-env.yml \
  --region europe-west6
