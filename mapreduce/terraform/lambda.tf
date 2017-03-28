provider "aws" {
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
  region = "${var.region}"
}

resource "aws_s3_bucket" "mapperOutputBucket" {
  bucket = "${var.mapperOutputBucket}"
}

resource "aws_s3_bucket" "reducerOutputBucket" {
  bucket = "${var.reducerOutputBucket}"
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
  function_name = "mapper"
  role = "${aws_iam_role.iamForLambda.arn}"
  handler = "mapper_wrapper.MapperWrapper"
  source_code_hash = "${base64sha256(file("../target/mapreduce.jar"))}"
  runtime = "java8"
  memory_size = "${var.mapperMemory}"
  timeout = "300"
}

resource "aws_lambda_function" "reducer" {
  filename = "../target/mapreduce.jar"
  function_name = "reducer"
  role = "${aws_iam_role.iamForLambda.arn}"
  handler = "reducer_wrapper.ReducerWrapper"
  source_code_hash = "${base64sha256(file("../target/mapreduce.jar"))}"
  runtime = "java8"
  memory_size = "${var.reducerMemory}"
  timeout = "300"
}

resource "aws_lambda_permission" "allowCoordinatorReducerBucket" {
  statement_id = "AllowExecutionFromReducerBucket"
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.coordinator.arn}"
  principal = "s3.amazonaws.com"
  source_arn = "${aws_s3_bucket.reducerOutputBucket.arn}"
}

resource "aws_lambda_permission" "allowCoordinatorMapperBucket" {
  statement_id = "AllowExecutionFromMapperBucket"
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.coordinator.arn}"
  principal = "s3.amazonaws.com"
  source_arn = "${aws_s3_bucket.mapperOutputBucket.arn}"
}

resource "aws_s3_bucket_notification" "mapperNotifCoordinator" {
  bucket = "${aws_s3_bucket.mapperOutputBucket.id}"
  lambda_function {
    lambda_function_arn = "${aws_lambda_function.coordinator.arn}"
    events = [
      "s3:ObjectCreated:*"]
  }
}

resource "aws_s3_bucket_notification" "reducerNotifCoordinator" {
  bucket = "${aws_s3_bucket.reducerOutputBucket.id}"
  lambda_function {
    lambda_function_arn = "${aws_lambda_function.coordinator.arn}"
    events = [
      "s3:ObjectCreated:*"]
  }
}

resource "aws_dynamodb_table" "statusTable" {
  name = "${var.statusTable}"
  read_capacity = 5
  write_capacity = 5
  hash_key = "job"
  range_key = "step"
  attribute {
    name = "job"
    type = "S"
  }
  attribute {
    name = "step"
    type = "N"
  }
}