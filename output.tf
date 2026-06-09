output "hourly_getter_url" {
  description = "URL for hourly getter."

  value = "${aws_apigatewayv2_stage.lambda.invoke_url}${module.hourly_getter.resource_path}"
}

output "hourly_poster_url" {
  description = "URL for hourly poster."

  value = "${aws_apigatewayv2_stage.lambda.invoke_url}${module.hourly_poster.resource_path}"
}

output "daily_getter_url" {
  description = "URL for daily getter."

  value = "${aws_apigatewayv2_stage.lambda.invoke_url}${module.daily_getter.resource_path}"
}
