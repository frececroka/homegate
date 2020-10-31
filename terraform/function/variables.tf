variable "function_name" {
  description = "Name of the function."
  type = string
}

variable "bucket" {
 description = "Name of the GCS bucket where the function is saved."
  type = string
}

variable "archive" {
  description = "Path to the archive containing the function source."
  type = string
}

variable "trigger_topic" {
  description = "The Pub/Sub topic that triggers this function."
  type = string
}

variable "collection" {
  description = "The Firestore collection where data is stored in."
  type = string
}

variable "telegram_token" {
  description = "The Telegram bot token."
  type = string
}

variable "chat_id" {
  description = "The ID of the Telegram chat that updates are sent to."
  type = string
}
