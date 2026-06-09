package com.xilef7

import kotlinx.serialization.Serializable
import java.math.BigInteger

@Serializable
data class DeltaStatistics(
    val delta: Float,
    val mean: Float,
    val count: Short,
    val max: Int,
    val min: Int,
    @Serializable(with = BigIntegerSerializer::class)
    val earliestSequenceNumber: BigInteger,
    @Serializable(with = BigIntegerSerializer::class)
    val latestSequenceNumber: BigInteger
)

operator fun (DeltaStatistics?).plus(other: DeltaStatistics): DeltaStatistics =
    if (this == null) other else {
        require(setOf(other.earliestSequenceNumber, other.latestSequenceNumber).all {
            it !in (earliestSequenceNumber..latestSequenceNumber)
        }) {
            "other.earliestSequenceNumber and other.latestSequenceNumber all must not be in earliestSequenceNumber..latestSequenceNumber: " +
                    "earliestSequenceNumber=${earliestSequenceNumber}, " +
                    "latestSequenceNumber=${latestSequenceNumber}, " +
                    "other.earliestSequenceNumber=${other.earliestSequenceNumber}, " +
                    "other.latestSequenceNumber=${other.latestSequenceNumber}"
        }

        val nextCount = count + other.count

        DeltaStatistics(
            delta = delta + other.delta,
            mean = if (count == 0.toShort() && other.count == 0.toShort()) 0f else (mean * count + other.mean * other.count) / nextCount,
            count = nextCount.toShort(),
            max = maxOf(max, other.max),
            min = minOf(min, other.min),
            earliestSequenceNumber = minOf(earliestSequenceNumber, other.earliestSequenceNumber),
            latestSequenceNumber = maxOf(latestSequenceNumber, other.latestSequenceNumber),
        )
    }
