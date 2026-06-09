package com.xilef7.db

import com.xilef7.PriceTrackerResources.Companion.COUNT
import com.xilef7.PriceTrackerResources.Companion.EXPIRY
import com.xilef7.PriceTrackerResources.Companion.INSTANT
import com.xilef7.PriceTrackerResources.Companion.MAX
import com.xilef7.PriceTrackerResources.Companion.MEAN
import com.xilef7.PriceTrackerResources.Companion.MIN
import com.xilef7.PriceTrackerResources.Companion.SKU_ID
import com.xilef7.PriceTrackerResources.Companion.VERSION
import org.http4k.connect.amazon.dynamodb.model.Attribute

val attrSkuId = Attribute.long().required(SKU_ID)
val attrInstant = Attribute.timestamp().required(INSTANT)
val attrMean = Attribute.float().required(MEAN)
val attrCount = Attribute.int().required(COUNT)
val attrMax = Attribute.int().required(MAX)
val attrMin = Attribute.int().required(MIN)
val attrVersion = Attribute.bigInteger().required(VERSION)
val attrExpiry = Attribute.timestamp().optional(EXPIRY)
