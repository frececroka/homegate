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
