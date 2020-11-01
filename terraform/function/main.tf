resource "google_storage_bucket_object" "source" {
  name   = "${var.function_name}.${filesha256(var.archive)}.zip"
  bucket = var.bucket
  source = var.archive
}

resource "google_cloudfunctions_function" "function" {
  name = var.function_name
  entry_point = "ch.homegate.crawler.CrawlerFunction"
  runtime = "java11"

  source_archive_bucket = var.bucket
  source_archive_object = google_storage_bucket_object.source.name

  max_instances = 1
  timeout = 540

  environment_variables = {
    TELEGRAM_TOKEN = var.telegram_token
    CHAT_ID = var.chat_id
    FIRESTORE_COLLECTION = var.collection
  }

  event_trigger {
    event_type = "google.pubsub.topic.publish"
    resource = var.trigger_topic
  }
}
