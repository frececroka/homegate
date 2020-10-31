terraform {
  required_providers {
    google = {
      source = "hashicorp/google"
    }
  }
}

provider "google" {
  version = "3.5.0"

  credentials = file("credentials.json")

  project = var.project
  region = var.region
  zone = var.zone
}

module "environment" {
  source = "../environment"
  name = "prod"
  telegram_token = var.telegram_token
  chat_id = var.chat_id
}
