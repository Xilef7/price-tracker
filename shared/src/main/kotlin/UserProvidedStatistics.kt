package com.xilef7

import kotlinx.serialization.Serializable

@Serializable
data class UserProvidedStatistics(
    val initial: Int,
    val max: Int,
    val min: Int,
)
