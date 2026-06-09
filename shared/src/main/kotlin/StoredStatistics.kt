package com.xilef7

import java.math.BigInteger

data class StoredStatistics(
    val mean: Float,
    val count: Short,
    val max: Int,
    val min: Int,
    val version: BigInteger,
)

operator fun StoredStatistics?.plus(other: DeltaStatistics) =
    if (this == null) {
        require(other.delta == 0f) { "other.delta must be 0" }

        StoredStatistics(
            mean = other.mean,
            count = other.count,
            max = other.max,
            min = other.min,
            version = other.latestSequenceNumber,
        )
    } else {
        require(version < other.earliestSequenceNumber) {
            "version must be < other.earliestSequenceNumber: " + listOf(
                "version=${version}",
                "other.earliestSequenceNumber=${other.earliestSequenceNumber}",
            ).joinToString(", ")
        }

        val totalCount = count + other.count

        StoredStatistics(
            mean = (mean * count + other.mean * other.count + other.delta) / totalCount,
            count = totalCount.toShort(),
            max = maxOf(max, other.max),
            min = minOf(min, other.min),
            version = other.latestSequenceNumber
        )
    }

operator fun StoredStatistics?.plus(other: UserProvidedStatistics) =
    if (this == null)
        StoredStatistics(
            mean = other.initial.toFloat(),
            count = 1,
            max = other.max,
            min = other.min,
            version = BigInteger.ZERO,
        )
    else {
        val totalCount = count + 1

        StoredStatistics(
            mean = mean + (other.initial - mean) / totalCount,
            count = totalCount.toShort(),
            max = maxOf(max, other.max),
            min = minOf(min, other.min),
            version = version
        )
    }
