provider "aws" {
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
  region = "${var.region}"
}

resource "aws_kinesis_stream" "test_stream" {
  name = "${var.source_stream}"
  shard_count = 1
  retention_period = 24
  shard_level_metrics = [
    "IncomingBytes",
    "OutgoingBytes"
  ]
}

resource "aws_iam_policy" "kinesis-consumer-producer-policy" {
  name = "kinesis-consumer-producer-policy"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "kinesis:DescribeStream",
        "kinesis:PutRecord",
        "kinesis:PutRecords",
        "kinesis:GetShardIterator",
        "kinesis:GetRecords"
      ],
      "Resource": [
        "${aws_kinesis_stream.test_stream.arn}"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "cloudwatch:PutMetricData"
      ],
      "Resource": [
        "*"
      ]
    }
  ]
}
EOF
}

resource "aws_iam_role" "iam_for_lambda" {
  name = "iam-for-lambda"
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

resource "aws_lambda_function" "processNewRecordFunction" {
  filename = "../target/lambda-kinesis.jar"
  function_name = "processNewRecordFunction"
  role = "${aws_iam_role.iam_for_lambda.arn}"
  handler = "lambda_kinesis.ProcessNewRecord"
  source_code_hash = "${base64sha256(file("../target/lambda-kinesis.jar"))}"
  runtime = "java8"
  memory_size = "512"
  timeout = "5"
}

resource "aws_iam_policy_attachment" "kinesis-consumer-producer-policy-attachement" {
  name = "kinesis-consumer-producer-policy-attachement"
  users = [
    "othmane"]
  policy_arn = "${aws_iam_policy.kinesis-consumer-producer-policy.arn}"
}

resource "aws_iam_policy_attachment" "kinesis-lambda-policy-attachement" {
  name = "kinesis-lambda-policy-attachement"
  roles = ["${aws_iam_role.iam_for_lambda.name}"]
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaKinesisExecutionRole"
}

resource "aws_lambda_event_source_mapping" "event_source_mapping" {
  batch_size = 10
  event_source_arn = "${aws_kinesis_stream.test_stream.arn}"
  enabled = true
  function_name = "${aws_lambda_function.processNewRecordFunction.arn}"
  starting_position = "TRIM_HORIZON"
}