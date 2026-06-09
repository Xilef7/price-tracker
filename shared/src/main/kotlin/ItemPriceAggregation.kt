package com.xilef7

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ItemPriceAggregation(
    val skuId: String,
    val instant: Instant,
    val mean: Float,
    val max: Int,
    val min: Int
)
