terraform {
  backend "s3" {}
}

variable "branch_name" {
  type = "string"
}

provider "aws" {
  version = "~> 2.0"
  region = "ap-southeast-2"
}

resource "aws_s3_bucket" "s3_bucket" {
  bucket = "${var.branch_name}-chirper-user.ruchij.com"
}

output "s3_bucket" {
  value = aws_s3_bucket.s3_bucket.bucket
}
