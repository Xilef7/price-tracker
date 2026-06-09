package com.xilef7.db

import com.xilef7.PriceTrackerResources.Companion.DAILY_TABLE
import com.xilef7.PriceTrackerResources.Companion.HOURLY_TABLE
import org.http4k.connect.amazon.dynamodb.model.TableName

val tableHourly = TableName.of(HOURLY_TABLE)
val tableDaily = TableName.of(DAILY_TABLE)
