terraform {
  backend "s3" {
    bucket = "terraform.ruchij.com"
    key = "chirper-api.tfstate"
    region = "ap-southeast-2"
  }
}

provider "aws" {
  region = "ap-southeast-2"
  version = "~> 2.0"
}

resource "aws_ecr_repository" "ecr" {
  name = "chirper-api"
}

output "chirper_api_ecr" {
  value = "${aws_ecr_repository.ecr.repository_url}"
}
