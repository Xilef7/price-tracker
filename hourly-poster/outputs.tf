output "resource_path" {
  description = "Path for posting hourly resource."

  value = aws_apigatewayv2_route.hourly_poster.route_key
}
