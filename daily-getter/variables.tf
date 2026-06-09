variable "daily_dynamodb_table_arn" {
  description = "DynamoDB table arn for daily table."
  type        = string
}

variable "api_gateway_id" {
  description = "API Gateway id for lambda."
  type        = string
}

variable "api_gateway_execution_arn" {
  description = "API Gateway execution arn for lambda."
  type        = string
}

variable "resource_path" {
  description = "Path for daily resource."
  type        = string
  default     = "/daily"
}
