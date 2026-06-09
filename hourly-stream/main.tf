data "local_file" "handler_zip" {
  filename = "${path.module}/build/distributions/hourly-stream.zip"
}

resource "aws_lambda_function" "hourly_stream" {
  function_name = "HourlyStream"

  filename = data.local_file.handler_zip.filename

  runtime = "java25"
  handler = "com.xilef7.hourlystream.Handler"

  source_code_hash = data.local_file.handler_zip.content_base64sha256

  timeout = 100

  role = aws_iam_role.lambda.arn

  environment {
    variables = {
      AWS_RETRY_MODE = "adaptive"
    }
  }
}

resource "aws_cloudwatch_log_group" "hourly_stream" {
  name = "/aws/lambda/${aws_lambda_function.hourly_stream.function_name}"

  retention_in_days = 30
}

resource "aws_iam_role" "lambda" {
  name = "hourly_stream_lambda"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Sid    = ""
      Principal = {
        Service = "lambda.amazonaws.com"
      }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_dynamodb_policy" {
  role       = aws_iam_role.lambda.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaDynamoDBExecutionRole"
}

data "aws_iam_policy_document" "dynamodb_daily_batch_get_and_batch_write_item" {
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem"
    ]
    resources = [
      var.daily_dynamodb_table_arn
    ]
  }
}

resource "aws_iam_policy" "dynamodb_daily_batch_get_and_batch_write_item" {
  name   = "dynamodb-daily-batch-get-and-batch-write-item-policy"
  policy = data.aws_iam_policy_document.dynamodb_daily_batch_get_and_batch_write_item.json
}

resource "aws_iam_role_policy_attachment" "lambda_dynamodb_daily_batch_get_and_batch_write_item_policy" {
  role       = aws_iam_role.lambda.name
  policy_arn = aws_iam_policy.dynamodb_daily_batch_get_and_batch_write_item.arn
}

resource "aws_lambda_event_source_mapping" "dynamodb_stream" {
  function_name                      = aws_lambda_function.hourly_stream.function_name
  event_source_arn                   = var.hourly_dynamodb_table_stream_arn
  batch_size                         = 10000
  maximum_batching_window_in_seconds = 300
  tumbling_window_in_seconds         = 900
  starting_position                  = "TRIM_HORIZON"
  bisect_batch_on_function_error     = true
}
