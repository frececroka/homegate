#!/usr/bin/env sh

gcloud alpha logging tail "resource.type=cloud_function" \
  --format "value(timestamp, labels['execution_id'], textPayload)" \
  --buffer-window 0s
