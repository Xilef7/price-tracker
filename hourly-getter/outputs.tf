output "resource_path" {
  description = "Path for getting hourly resource."

  value = aws_apigatewayv2_route.hourly_getter.route_key
}
