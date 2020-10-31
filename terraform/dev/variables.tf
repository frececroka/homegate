variable "project" {
  description = "The GCP project."
  type = string
}

variable "region" {
  description = "The GCP region."
  type = string
}

variable "zone" {
  description = "The GCP zone."
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
