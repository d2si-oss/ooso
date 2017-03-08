provider "aws" {
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
  region = "${var.region}"
}

resource "aws_dynamodb_table" "source_table" {
  name = "${var.source_table}"
  read_capacity = 5
  write_capacity = 5
  hash_key = "pkey"
  range_key = "skey"
  attribute {
    name = "pkey"
    type = "S"
  }
  attribute {
    name = "skey"
    type = "S"
  }
  stream_enabled = true
  stream_view_type = "NEW_IMAGE"
}

resource "aws_iam_role" "iam_for_lambda" {
  name = "iam-for-lambda-dynamodb"
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

resource "aws_iam_policy" "lambda-dynamodb-policy" {
  name = "lambda-dynamodb-policy"
  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "lambda:InvokeFunction"
      ],
      "Resource": [
        "*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:GetRecords",
        "dynamodb:GetShardIterator",
        "dynamodb:DescribeStream",
        "dynamodb:ListStreams",
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "*"
    }
  ]
}
EOF
}

resource "aws_iam_policy_attachment" "lambda-dynamodb-policy-attachment" {
  name = "lambda-dynamodb-policy-attachment"
  roles = [
    "${aws_iam_role.iam_for_lambda.name}"]
  policy_arn = "${aws_iam_policy.lambda-dynamodb-policy.arn}"
}

resource "aws_iam_policy_attachment" "execute-lambda-attachment" {
  name = "execute-lambda-attachment"
  roles = ["${aws_iam_role.iam_for_lambda.name}"]
  policy_arn = "arn:aws:iam::aws:policy/AWSLambdaExecute"
}

resource "aws_lambda_function" "processNewItemFunction" {
  filename = "../target/lambda-dynamodb.jar"
  function_name = "processNewItemFunction"
  role = "${aws_iam_role.iam_for_lambda.arn}"
  handler = "lambda_dynamodb.ProcessNewItem"
  source_code_hash = "${base64sha256(file("../target/lambda-dynamodb.jar"))}"
  runtime = "java8"
  memory_size = "512"
  timeout = "5"
}

resource "aws_lambda_event_source_mapping" "event_source_mapping" {
  batch_size = 10
  event_source_arn = "${aws_dynamodb_table.source_table.stream_arn}"
  enabled = true
  function_name = "${aws_lambda_function.processNewItemFunction.arn}"
  starting_position = "TRIM_HORIZON"
}