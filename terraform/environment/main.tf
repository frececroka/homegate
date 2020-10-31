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

data "archive_file" "crawler" {
  type = "zip"
  output_path = "crawler.zip"
  source_file = "${path.module}/../../build/libs/homegate-all.jar"
}

module "crawler" {
  source = "../function"
  function_name = "crawler-${var.name}"
  bucket = google_storage_bucket.functions.name
  archive = data.archive_file.crawler.output_path
  trigger_topic = google_pubsub_topic.crawler.id
  collection = "${var.name}.listings"
  chat_id = var.chat_id
  telegram_token = var.telegram_token
}
