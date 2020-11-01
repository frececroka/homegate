# Homegate Crawler

## Test Locally

To configure the bot for local testing, set the `TELEGRAM_TOKEN` and `CHAT_ID` environment variables accordingly. The former is the access token of your Telegram bot, and the latter is the ID of the Telegram chat that updates are sent to.

### Crawler

The crawler checks for new listings and then sends Telegram messages for each one. You can run it locally with `./gradlew runCrawler`. The entry point is the function `ch.homegate.crawler.main` in this case.

### Responder
 
The responder receives updates from Telegram and sends appropriate responses, like changing the reply keyboard. You can run it locally with `./gradlew runResponder`. The entry point is the function `ch.homegate.responder.main` in this case. 

## Deployment

The bot can be deployed to the Google Cloud Platform.

### Setup

Create GCP service accounts for the development and production environments with roles "Project/Editor" and "IAM/Security Admin" and save the JSON file with the credentials to `terraform/dev/credentials.json` and `terraform/prod/credentials.json`, respectively. You may use the same service account for both.

Configure the development and production environments by placing `terraform.tfvars` files into the `terraform/dev` and `terraform/prod` directories. These files look like this:

```
project = "..."
region = "europe-west6"
zone = "europe-west6-a"

telegram_token = "..."
chat_id = "..."
```

Assign to `project` the project ID of the GCP project that resources will be created in, to `telegram_token` the access token for your Telegram bot, and to `chat_id` the ID of the Telegram chat that updates are sent to. 

Now initialize Terraform by running `terraform init` once in the `terraform/dev` directory and once in the `terraform/prod` directory.

### Deploy

To deploy a new version, begin with building the JAR by running `./gradlew shadowJar`. Then change to the `terraform/dev` directory (to deploy to the development environment) or to the `terraform/prod` directory (to deploy to the production environment) and run `terraform apply`. Double check the prosed changes and accept the plan by entering `yes`.
