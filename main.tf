provider "aws" {
  region = var.aws_region
}

resource "aws_apigatewayv2_api" "lambda" {
  name          = "lambda_gw"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_stage" "lambda" {
  api_id = aws_apigatewayv2_api.lambda.id

  name        = "lambda_stage"
  auto_deploy = true

  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.api_gw.arn

    format = jsonencode({
      requestId               = "$context.requestId"
      sourceIp                = "$context.identity.sourceIp"
      requestTime             = "$context.requestTime"
      protocol                = "$context.protocol"
      httpMethod              = "$context.httpMethod"
      resourcePath            = "$context.resourcePath"
      routeKey                = "$context.routeKey"
      status                  = "$context.status"
      responseLength          = "$context.responseLength"
      integrationErrorMessage = "$context.integrationErrorMessage"
    })
  }
}

resource "aws_cloudwatch_log_group" "api_gw" {
  name = "/aws/api_gw/${aws_apigatewayv2_api.lambda.name}"

  retention_in_days = 30
}

module "dynamodb_service" {
  source = "./dynamodb-service"
}

module "hourly_getter" {
  source                    = "./hourly-getter"
  hourly_dynamodb_table_arn = module.dynamodb_service.hourly_table_arn
  api_gateway_id            = aws_apigatewayv2_api.lambda.id
  api_gateway_execution_arn = aws_apigatewayv2_api.lambda.execution_arn
}

module "hourly_poster" {
  source                    = "./hourly-poster"
  hourly_dynamodb_table_arn = module.dynamodb_service.hourly_table_arn
  api_gateway_id            = aws_apigatewayv2_api.lambda.id
  api_gateway_execution_arn = aws_apigatewayv2_api.lambda.execution_arn
}

module "hourly_stream" {
  source                           = "./hourly-stream"
  hourly_dynamodb_table_stream_arn = module.dynamodb_service.hourly_table_stream_arn
  daily_dynamodb_table_arn         = module.dynamodb_service.daily_table_arn
}

module "daily_getter" {
  source                    = "./daily-getter"
  daily_dynamodb_table_arn  = module.dynamodb_service.daily_table_arn
  api_gateway_id            = aws_apigatewayv2_api.lambda.id
  api_gateway_execution_arn = aws_apigatewayv2_api.lambda.execution_arn
}
