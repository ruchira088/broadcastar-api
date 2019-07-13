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

data "aws_vpc" "vpc" {
  tags = {
    Name = "ruchira-vpc"
  }
}

data "aws_subnet_ids" "public_subnets" {
  vpc_id = data.aws_vpc.vpc.id

  tags = {
    Type = "public"
  }
}

resource "aws_s3_bucket" "s3_bucket" {
  bucket = "${var.branch_name}-chirper-user.ruchij.com"
}

resource "random_string" "db_username" {
  length = 16
  special = false
  number = false
}

resource "random_string" "db_password" {
  length = 32
  special = false
}

resource "aws_security_group" "db_security_group" {
  vpc_id = data.aws_vpc.vpc.id

  ingress {
    from_port = 5432
    to_port = 5432
    protocol = "tcp"
    cidr_blocks = [ "0.0.0.0/0" ]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = [ "0.0.0.0/0" ]
  }
}

resource "aws_db_subnet_group" "db_subnet_group" {
  subnet_ids = data.aws_subnet_ids.public_subnets.ids
}

resource "aws_db_instance" "user_service_database" {
  instance_class = "db.t3.micro"
  engine = "postgres"
  engine_version = "11.4"
  name = "user_service"
  final_snapshot_identifier = "user-service-${var.branch_name}"
  identifier = "user-service-${var.branch_name}"
  allocated_storage = "20"
  username = random_string.db_username.result
  password = random_string.db_password.result
  vpc_security_group_ids = [ aws_security_group.db_security_group.id ]
  db_subnet_group_name = aws_db_subnet_group.db_subnet_group.name
  publicly_accessible = true

  tags = {
    Service = "user-service"
  }
}

resource "aws_ssm_parameter" "db_username" {
  name = "/chirper-api/user-service/${var.branch_name}/db-username"
  type = "SecureString"
  value = aws_db_instance.user_service_database.username
}

resource "aws_ssm_parameter" "db_password" {
  name = "/chirper-api/user-service/${var.branch_name}/db-password"
  type = "SecureString"
  value = aws_db_instance.user_service_database.password
}

output "s3_bucket" {
  value = aws_s3_bucket.s3_bucket.bucket
}

output "db_endpoint" {
  value = aws_db_instance.user_service_database.endpoint
}

output "db_name" {
  value = aws_db_instance.user_service_database.name
}
