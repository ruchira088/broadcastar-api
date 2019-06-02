terraform {
  backend "s3" {
    bucket = "terraform.ruchij.com"
    key = "chirper/{{{ branchName }}}/user-service.tfstate"
    region = "ap-southeast-2"
  }
}

provider "aws" {
  version = "~> 2.0"
  region = "ap-southeast-2"
}

resource "aws_s3_bucket" "s3_bucket" {
  bucket = "{{{ branchName }}}-chirper-user.ruchij.com"
}
