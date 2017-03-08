provider "aws" {
  access_key = "${var.access_key}"
  secret_key = "${var.secret_key}"
  region = "${var.region}"
}

resource "aws_s3_bucket" "input_bucket" {
  bucket = "${var.input_bucket}"
}

resource "aws_s3_bucket" "output_bucket" {
  bucket = "${var.output_bucket}"
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

resource "aws_iam_policy_attachment" "execute-lambda-attachment" {
  name = "execute-lambda-attachment"
  roles = [
    "${aws_iam_role.iam_for_lambda.name}"]
  policy_arn = "arn:aws:iam::aws:policy/AWSLambdaExecute"
}

resource "aws_lambda_function" "unzipFunction" {
  filename = "../target/lambda-s3.jar"
  function_name = "unzipFunction"
  role = "${aws_iam_role.iam_for_lambda.arn}"
  handler = "bzipDecompressionLambda.DecompressZipedFile"
  source_code_hash = "${base64sha256(file("../target/lambda-s3.jar"))}"
  runtime = "java8"
  memory_size = "512"
  timeout = "5"
}

resource "aws_lambda_permission" "allow_bucket" {
  statement_id = "AllowExecutionFromS3Bucket"
  action = "lambda:InvokeFunction"
  function_name = "${aws_lambda_function.unzipFunction.arn}"
  principal = "s3.amazonaws.com"
  source_arn = "${aws_s3_bucket.input_bucket.arn}"
}

resource "aws_s3_bucket_notification" "bucket_notification" {
  bucket = "${aws_s3_bucket.input_bucket.id}"
  lambda_function {
    lambda_function_arn = "${aws_lambda_function.unzipFunction.arn}"
    events = [
      "s3:ObjectCreated:*"]
    filter_suffix = ".bz2"
  }
}