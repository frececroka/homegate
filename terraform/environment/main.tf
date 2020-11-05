resource "google_pubsub_topic" "crawler" {
  name = "${var.name}.crawler"
}

resource "google_cloud_scheduler_job" "crawler" {
  name = "crawler-${var.name}"
  schedule = "0 */1 * * *"

  pubsub_target {
    topic_name = google_pubsub_topic.crawler.id
    data = base64encode("crawl")
  }
}

resource "google_storage_bucket" "functions" {
  name = "${var.name}-functions-homegate-294112"
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

resource "google_cloudfunctions_function" "crawler" {
  name = "crawler-${var.name}"
  entry_point = "ch.homegate.crawler.Function"
  runtime = "java11"

  source_archive_bucket = google_storage_bucket.functions.name
  source_archive_object = google_storage_bucket_object.source.name

  max_instances = 1
  timeout = 540

  environment_variables = {
    TELEGRAM_TOKEN = var.telegram_token
    CHAT_ID = var.chat_id
    FIRESTORE_COLLECTION = "${var.name}.listings"
    AIRTABLE_API_KEY = var.airtable_api_key
    AIRTABLE_APP_ID = var.airtable_app_id
  }

  event_trigger {
    event_type = "google.pubsub.topic.publish"
    resource = google_pubsub_topic.crawler.id
  }
}

resource "google_cloudfunctions_function" "responder" {
  name = "responder-${var.name}"
  entry_point = "ch.homegate.responder.Function"
  runtime = "java11"

  source_archive_bucket = google_storage_bucket.functions.name
  source_archive_object = google_storage_bucket_object.source.name

  environment_variables = {
    TELEGRAM_TOKEN = var.telegram_token
    CHAT_ID = var.chat_id
    FIRESTORE_COLLECTION = "${var.name}.listings"
    AIRTABLE_API_KEY = var.airtable_api_key
    AIRTABLE_APP_ID = var.airtable_app_id
  }

  trigger_http = true
}

resource "google_cloudfunctions_function_iam_member" "invoker" {
  project = google_cloudfunctions_function.responder.project
  region = google_cloudfunctions_function.responder.region
  cloud_function = google_cloudfunctions_function.responder.name
  role = "roles/cloudfunctions.invoker"
  member = "allUsers"
}
