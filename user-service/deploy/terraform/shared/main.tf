terraform {
  backend "s3" {
    bucket = "terraform.ruchij.com"
    key = "chirper/user-service.tfstate"
    region = "ap-southeast-2"
  }
}

provider "aws" {
  region = "ap-southeast-2"
  version = "~> 2.0"
}

resource "aws_ecr_repository" "ecr" {
  name = "chirper-user-service"
}

output "ecr_url" {
  value = "${aws_ecr_repository.ecr.repository_url}"
}
