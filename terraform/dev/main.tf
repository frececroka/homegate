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
  name = "dev"
  telegram_token = var.telegram_token
  airtable_api_key = var.airtable_api_key
  airtable_app_id = var.airtable_app_id
}
