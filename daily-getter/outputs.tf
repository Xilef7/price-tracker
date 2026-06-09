output "resource_path" {
  description = "Path for getting daily resource."

  value = aws_apigatewayv2_route.daily_getter.route_key
}
