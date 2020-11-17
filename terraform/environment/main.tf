locals {
  function_environment_variables = {
    TELEGRAM_TOKEN = var.telegram_token

    FIRESTORE_LISTINGS_COLLECTION = "${var.name}.listings"
    FIRESTORE_PROFILES_COLLECTION = "${var.name}.query-constraints"

    AIRTABLE_API_KEY = var.airtable_api_key
    AIRTABLE_APP_ID = var.airtable_app_id

    CRAWL_REQUEST_TOPIC = google_pubsub_topic.crawler.id

    SPRING_PROFILES_ACTIVE = "gcf"
  }
}

resource "google_storage_bucket" "functions" {
  name = "${var.name}-functions-homegate-294112"

  labels = {
    environment = var.name
  }
}

data "archive_file" "source" {
  type = "zip"
  output_path = "crawler.zip"
  source_file = "${path.module}/../../build/libs/homegate-all.jar"
}

resource "google_storage_bucket_object" "source" {
  name   = "${var.name}.${filesha256(data.archive_file.source.output_path)}.zip"
  bucket = google_storage_bucket.functions.name
  source = data.archive_file.source.output_path
}


resource "google_pubsub_topic" "initiator" {
  name = "${var.name}.initiator"

  labels = {
    environment = var.name
  }
}

resource "google_cloud_scheduler_job" "initiator" {
  name = "initiator-${var.name}"
  schedule = "0 */1 * * *"

  pubsub_target {
    topic_name = google_pubsub_topic.initiator.id
    data = base64encode("initiate")
  }
}


resource "google_cloudfunctions_function" "initiator" {
  name = "initiator-${var.name}"
  entry_point = "ch.homegate.initiator.Function"
  runtime = "java11"

  source_archive_bucket = google_storage_bucket.functions.name
  source_archive_object = google_storage_bucket_object.source.name

  max_instances = 1
  timeout = 540

  environment_variables = local.function_environment_variables

  event_trigger {
    event_type = "google.pubsub.topic.publish"
    resource = google_pubsub_topic.initiator.id
  }

  labels = {
    environment = var.name
  }
}


resource "google_pubsub_topic" "crawler" {
  name = "${var.name}.crawler"

  labels = {
    environment = var.name
  }
}

resource "google_cloudfunctions_function" "crawler" {
  name = "crawler-${var.name}"
  entry_point = "ch.homegate.crawler.Function"
  runtime = "java11"

  source_archive_bucket = google_storage_bucket.functions.name
  source_archive_object = google_storage_bucket_object.source.name

  timeout = 540

  environment_variables = local.function_environment_variables

  event_trigger {
    event_type = "google.pubsub.topic.publish"
    resource = google_pubsub_topic.crawler.id
  }

  labels = {
    environment = var.name
  }
}


resource "google_cloudfunctions_function" "responder" {
  name = "responder-${var.name}"
  entry_point = "ch.homegate.responder.Function"
  runtime = "java11"

  source_archive_bucket = google_storage_bucket.functions.name
  source_archive_object = google_storage_bucket_object.source.name

  environment_variables = local.function_environment_variables

  trigger_http = true

  labels = {
    environment = var.name
  }
}

resource "google_cloudfunctions_function_iam_member" "invoker" {
  project = google_cloudfunctions_function.responder.project
  region = google_cloudfunctions_function.responder.region
  cloud_function = google_cloudfunctions_function.responder.name
  role = "roles/cloudfunctions.invoker"
  member = "allUsers"
}
