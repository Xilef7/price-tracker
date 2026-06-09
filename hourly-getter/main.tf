data "local_file" "handler_zip" {
  filename = "${path.module}/build/distributions/hourly-getter.zip"
}

resource "aws_lambda_function" "hourly_getter" {
  function_name = "HourlyGetter"

  filename = data.local_file.handler_zip.filename

  runtime = "java25"
  handler = "com.xilef7.hourlygetter.Handler"

  source_code_hash = data.local_file.handler_zip.content_base64sha256

  timeout = 60

  role = aws_iam_role.lambda.arn

  environment {
    variables = {
      AWS_RETRY_MODE = "adaptive"
    }
  }
}

resource "aws_cloudwatch_log_group" "hourly_getter" {
  name = "/aws/lambda/${aws_lambda_function.hourly_getter.function_name}"

  retention_in_days = 30
}

resource "aws_iam_role" "lambda" {
  name = "hourly_getter_lambda"

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

resource "aws_iam_role_policy_attachment" "lambda_basic_policy" {
  role       = aws_iam_role.lambda.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

data "aws_iam_policy_document" "dynamodb_hourly_query" {
  statement {
    effect = "Allow"
    actions = [
      "dynamodb:Query"
    ]
    resources = [
      var.hourly_dynamodb_table_arn
    ]
  }
}

resource "aws_iam_policy" "dynamodb_hourly_query" {
  name   = "dynamodb-hourly-query-policy"
  policy = data.aws_iam_policy_document.dynamodb_hourly_query.json
}

resource "aws_iam_role_policy_attachment" "lambda_dynamodb_hourly_query_policy" {
  role       = aws_iam_role.lambda.name
  policy_arn = aws_iam_policy.dynamodb_hourly_query.arn
}

resource "aws_apigatewayv2_integration" "hourly_getter" {
  api_id = var.api_gateway_id

  integration_uri    = aws_lambda_function.hourly_getter.invoke_arn
  integration_type   = "AWS_PROXY"
  integration_method = "POST"
}

resource "aws_apigatewayv2_route" "hourly_getter" {
  api_id = var.api_gateway_id

  route_key = "GET ${var.resource_path}/{sku_id}"
  target    = "integrations/${aws_apigatewayv2_integration.hourly_getter.id}"
}

resource "aws_lambda_permission" "api_gw" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.hourly_getter.function_name
  principal     = "apigateway.amazonaws.com"

  source_arn = "${var.api_gateway_execution_arn}/*/*"
}
