package com.xilef7.db

import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ExponentialBackoffWithJitter(
    val initialDelay: Duration = 1.seconds,
    val scaleFactor: Double = 2.0,
    val jitter: Double = 1.0,
    val maxBackoff: Duration = 20.seconds,
) {
    init {
        require(initialDelay.isPositive()) { "initialDelay must be positive: $initialDelay" }
        require(scaleFactor >= 1) { "scaleFactor must be >= 1: $scaleFactor" }
        require(jitter in 0.0..1.0) { "jitter must be in 0..1: $jitter" }
        require(maxBackoff > initialDelay) {
            "maxBackoff must be > initialDelay: " +
                listOf(
                    "maxBackoff=$maxBackoff",
                    "initialDelay=$initialDelay",
                ).joinToString(", ")
        }
    }

    suspend fun backoff(attempt: Int) {
        require(attempt > 0) { "attempt must be > 0: $attempt" }
        delay(
            minOf(
                initialDelay * scaleFactor.pow(attempt - 1),
                maxBackoff,
            ) * Random.nextDouble(1 - jitter, 1.0)
        )
    }
}
