//load job infrastructure info from json file
data "external" "jobInfo" {
  program = [
    "python3",
    "${path.module}/../provide_job_info.py"]

  query = {
    path = "../src/main/resources/jobInfo.json"
  }
}

provider "aws" {
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
  region = "${var.region}"
}

resource "aws_s3_bucket" "mapperOutputBucket" {
  bucket = "${data.external.jobInfo.result.mapperOutputBucket}"
}

resource "aws_s3_bucket" "reducerOutputBucket" {
  bucket = "${data.external.jobInfo.result.reducerOutputBucket}"
}

resource "aws_iam_role" "iamForLambda" {
  name = "iamForLambda"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

resource "aws_iam_policy_attachment" "lambdaAccessAttachment" {
  name = "lambdaAccessAttachment"
  roles = [
    "${aws_iam_role.iamForLambda.name}"]
  policy_arn = "arn:aws:iam::aws:policy/AWSLambdaFullAccess"
}

resource "aws_iam_policy_attachment" "s3AccessAttachment" {
  name = "s3AccessAttachment"
  roles = [
    "${aws_iam_role.iamForLambda.name}"]
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_lambda_function" "driver" {
  filename = "../target/mapreduce.jar"
  function_name = "driver"
  role = "${aws_iam_role.iamForLambda.arn}"
  handler = "driver.Driver"
  source_code_hash = "${base64sha256(file("../target/mapreduce.jar"))}"
  runtime = "java8"
  memory_size = "1536"
  timeout = "300"
}

resource "aws_lambda_function" "coordinator" {
  filename = "../target/mapreduce.jar"
  function_name = "coordinator"
  role = "${aws_iam_role.iamForLambda.arn}"
  handler = "coordinator.Coordinator"
  source_code_hash = "${base64sha256(file("../target/mapreduce.jar"))}"
  runtime = "java8"
  memory_size = "1536"
  timeout = "300"
}

resource "aws_lambda_function" "mapper" {
  filename = "../target/mapreduce.jar"
  function_name = "${data.external.jobInfo.result.mapperFunctionName}"
  role = "${aws_iam_role.iamForLambda.arn}"
  handler = "mapper_wrapper.MapperWrapper"
  source_code_hash = "${base64sha256(file("../target/mapreduce.jar"))}"
  runtime = "java8"
  memory_size = "${data.external.jobInfo.result.mapperMemory}"
  timeout = "300"
}

resource "aws_lambda_function" "reducer" {
  filename = "../target/mapreduce.jar"
  function_name = "${data.external.jobInfo.result.reducerFunctionName}"
  role = "${aws_iam_role.iamForLambda.arn}"
  handler = "reducer_wrapper.ReducerWrapper"
  source_code_hash = "${base64sha256(file("../target/mapreduce.jar"))}"
  runtime = "java8"
  memory_size = "${data.external.jobInfo.result.reducerMemory}"
  timeout = "300"
}

resource "aws_dynamodb_table" "statusTable" {
  name = "${data.external.jobInfo.result.statusTable}"
  read_capacity = 5
  write_capacity = 5
  hash_key = "job"
  attribute {
    name = "job"
    type = "S"
  }
}