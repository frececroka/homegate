# Homegate Crawler

## Setup

Create GCP service accounts for the development and production environments with role Project/Editor and save the JSON file with the credentials to `terraform/dev/credentials.json` and `terraform/prod/credentials.json`, respectively. You may use the same service account for both.

Configure the development and production environments by placing `terraform.tfvars` files into the `terraform/dev` and `terraform/prod` directories. These files look like this:

```
project = "homegate-294112"
region = "europe-west6"
zone = "europe-west6-a"

telegram_token = "..."
chat_id = "..."
```

Assign to `telegram_token` the access token for your Telegram bot and to `chat_id` the ID of the Telegram chat that updates are sent to. 

Now initialize Terraform by running `terraform init` once in the `terraform/dev` directory and once in the `terraform/prod` directory.

## Deployment

To deploy a new version, begin with building the JAR by running `./gradlew shadowJar`. Then change to the `terraform/dev` directory (to deploy to the development environment) or to the `terraform/prod` directory (to deploy to the production environment) and run `terraform apply`. Double check the prosed changes and accept the plan by entering `yes`.
