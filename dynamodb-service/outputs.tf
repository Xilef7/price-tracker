output "hourly_table_arn" {
  description = "ARN of the hourly DynamoDB table"
  value       = aws_dynamodb_table.hourly.arn
}

output "hourly_table_stream_arn" {
  description = "ARN of the hourly DynamoDB table stream"
  value       = aws_dynamodb_table.hourly.stream_arn
}

output "daily_table_arn" {
  description = "ARN of the daily DynamoDB table"
  value       = aws_dynamodb_table.daily.arn
}
