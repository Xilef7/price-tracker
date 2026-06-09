variable "daily_dynamodb_table_arn" {
  description = "DynamoDB table arn for daily table."
  type        = string
}

variable "hourly_dynamodb_table_stream_arn" {
  description = "DynamoDB stream arn for hourly table."
  type        = string
}
