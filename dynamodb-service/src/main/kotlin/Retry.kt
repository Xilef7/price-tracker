package com.xilef7.db

import kotlinx.coroutines.flow.flow

internal inline fun <P : Map<*, *>, R> retry(
    initialParameter: P,
    crossinline getUnprocessed: (R) -> P?,
    delayProvider: ExponentialBackoffWithJitter = ExponentialBackoffWithJitter(),
    crossinline fn: suspend (P) -> R,
) = flow {
    retry(initialParameter, delayProvider, 0) { parameter ->
        fn(parameter)
            .also { emit(it) }
            .let(getUnprocessed)!!
            .also { if (it.isEmpty()) return@flow }
    }
}

internal suspend inline fun <P, R, reified E : Throwable> retry(
    initialParameter: P,
    getUpdatedParameter: suspend (E) -> P,
    delayProvider: ExponentialBackoffWithJitter = ExponentialBackoffWithJitter(),
    maxAttempts: Int = 4,
    crossinline fn: suspend (P) -> R,
): R = retry(initialParameter, delayProvider, maxAttempts) {
    try {
        return fn(it)
    } catch (e: E) {
        getUpdatedParameter(e)
    }
}

internal suspend inline fun <P> retry(
    initialParameter: P,
    delayProvider: ExponentialBackoffWithJitter = ExponentialBackoffWithJitter(),
    maxAttempts: Int = 4,
    fn: suspend (P) -> P,
): Nothing {
    var currentParameter = initialParameter
    retry(delayProvider, maxAttempts) {
        currentParameter = fn(currentParameter)
    }
}

private suspend inline fun retry(
    delayProvider: ExponentialBackoffWithJitter = ExponentialBackoffWithJitter(),
    maxAttempts: Int = 4,
    fn: suspend () -> Unit,
): Nothing {
    var attempt = 0
    while (true) {
        if (attempt > 0) delayProvider.backoff(attempt)
        fn()
        attempt++
        if (maxAttempts in 1..attempt) throw TooManyAttemptsException(maxAttempts)
    }
}
