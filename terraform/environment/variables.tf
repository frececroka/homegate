variable "name" {
  description = "Name of the environment."
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

variable "airtable_api_key" {
  description = "The Airtable API key."
  type = string
}

variable "airtable_app_id" {
  description = "The Airtable app id."
  type = string
}
