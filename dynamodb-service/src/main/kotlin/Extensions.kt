package com.xilef7.db

import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import com.xilef7.Key
import com.xilef7.PriceTrackerResources.Companion.COUNT
import com.xilef7.PriceTrackerResources.Companion.EXPIRY
import com.xilef7.PriceTrackerResources.Companion.INSTANT
import com.xilef7.PriceTrackerResources.Companion.MAX
import com.xilef7.PriceTrackerResources.Companion.MEAN
import com.xilef7.PriceTrackerResources.Companion.MIN
import com.xilef7.PriceTrackerResources.Companion.SKU_ID
import com.xilef7.PriceTrackerResources.Companion.VERSION
import com.xilef7.StoredStatistics
import java.math.BigInteger
import kotlin.time.Duration
import kotlin.time.Instant

val Map<String, AttributeValue>.key
    get() = Key(
        getValue(SKU_ID).asN(),
        getValue(INSTANT).asN().toLong().let(Instant::fromEpochSeconds),
    )

val Map<String, AttributeValue>.storedStatistics
    get() = StoredStatistics(
        mean = getValue(MEAN).asN().toFloat(),
        count = getValue(COUNT).asN().toShort(),
        max = getValue(MAX).asN().toInt(),
        min = getValue(MIN).asN().toInt(),
        version = getValue(VERSION).asN().toBigInteger(),
    )

fun Key.toAttributeMap() = mapOf(
    SKU_ID to AttributeValue.N(skuId),
    INSTANT to AttributeValue.N(instant.epochSeconds.toString()),
)

fun Pair<Key, StoredStatistics>.toAttributeMap(
    newVersion: BigInteger? = null,
    expiry: Duration? = null
) = buildMap {
    val (key, value) = this@toAttributeMap
    val (skuId, instant) = key
    val (mean, count, max, min, version) = value

    put(SKU_ID, AttributeValue.N(skuId))
    put(INSTANT, AttributeValue.N(instant.epochSeconds.toString()))
    put(MEAN, AttributeValue.N(mean.toString()))
    put(COUNT, AttributeValue.N(count.toString()))
    put(MAX, AttributeValue.N(max.toString()))
    put(MIN, AttributeValue.N(min.toString()))
    put(VERSION, AttributeValue.N((newVersion ?: version).toString()))
    expiry?.let {
        put(EXPIRY, AttributeValue.N(instant.plus(it).epochSeconds.toString()))
    }
}
