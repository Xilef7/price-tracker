package com.xilef7

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class Key(val skuId: String, val instant: Instant)
