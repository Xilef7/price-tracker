resource "aws_dynamodb_table" "hourly" {
  name         = "Hourly"
  billing_mode = "PROVISIONED"

  read_capacity  = var.hourly_table_read_capacity
  write_capacity = var.hourly_table_write_capacity

  ttl {
    enabled        = true
    attribute_name = "e"
  }

  stream_enabled   = true
  stream_view_type = "NEW_AND_OLD_IMAGES"

  hash_key  = "i"
  range_key = "t"

  attribute {
    name = "i"
    type = "N"
  }

  attribute {
    name = "t"
    type = "N"
  }
}

resource "aws_dynamodb_table" "daily" {
  name         = "Daily"
  billing_mode = "PROVISIONED"

  read_capacity  = var.daily_table_read_capacity
  write_capacity = var.daily_table_write_capacity

  hash_key  = "i"
  range_key = "t"

  attribute {
    name = "i"
    type = "N"
  }

  attribute {
    name = "t"
    type = "N"
  }
}
