package com.xilef7

import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

fun Instant.truncateTo(durationValue: Long, durationUnit: DurationUnit): Instant {
    require(durationValue > 0) { "durationValue must be positive: $durationValue" }
    require(durationUnit >= DurationUnit.SECONDS) { "durationUnit must be >= SECONDS: $durationUnit" }
    return this - (epochSeconds % durationValue.toDuration(durationUnit).inWholeSeconds).absoluteValue.seconds - nanosecondsOfSecond.nanoseconds
}
