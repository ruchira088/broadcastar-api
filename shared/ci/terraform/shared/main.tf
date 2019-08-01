provider "aws" {
  version = "~> 2.0"
  region = "ap-southeast-2"
}

terraform {
  backend "s3" {}
}

locals {
  icon = "chirper-logo.ico"

  svg = "chirper-logo.svg"

  png_images = [
    "chirper-logo-48x48.png",
    "chirper-logo-100x100.png",
    "chirper-logo-240x240.png",
    "chirper-logo-480x480.png",
    "chirper-logo-800x800.png"
  ]
}

resource "aws_s3_bucket" "public_resource" {
  bucket = "public.chirper.ruchij.com"
}

resource "aws_s3_bucket_object" "logo" {
  bucket = aws_s3_bucket.public_resource.bucket
  key = local.svg
  source = "${path.module}/../../../assets/${local.svg}"
  content_type = "image/svg+xml"
  acl = "public-read"
}

resource "aws_s3_bucket_object" "icon" {
  bucket = aws_s3_bucket.public_resource.bucket
  key = local.icon
  source = "${path.module}/../../../assets/${local.icon}"
  content_type = "image/x-icon"
  acl = "public-read"
}

resource "aws_s3_bucket_object" "png_logos" {
  count = length(local.png_images)
  bucket = aws_s3_bucket.public_resource.bucket
  key = local.png_images[count.index]
  source = "${path.module}/../../../assets/${local.png_images[count.index]}"
  content_type = "image/png"
  acl = "public-read"
}
