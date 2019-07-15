provider "aws" {
  version = "~> 2.0"
  region = "ap-southeast-2"
}

terraform {
  backend "s3" {}
}

resource "aws_s3_bucket" "public_resource" {
  bucket = "public.chirper.ruchij.com"
}

resource "aws_s3_bucket_object" "logo" {
  bucket = aws_s3_bucket.public_resource.bucket
  key = "chirper-logo.svg"
  source = "${path.module}/../../../assets/chirper-logo.svg"
  content_type = "image/svg+xml"
  acl = "public-read"
}
