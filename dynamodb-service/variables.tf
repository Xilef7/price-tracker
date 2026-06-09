variable "hourly_table_read_capacity" {
  description = "Read capacity for hourly table."
  type        = number
  default     = 13
}

variable "hourly_table_write_capacity" {
  description = "Write capacity for hourly table."
  type        = number
  default     = 13
}

variable "daily_table_read_capacity" {
  description = "Read capacity for daily table."
  type        = number
  default     = 12
}

variable "daily_table_write_capacity" {
  description = "Write capacity for daily table."
  type        = number
  default     = 12
}
