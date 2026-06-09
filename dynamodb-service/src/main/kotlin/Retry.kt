package com.xilef7.db

import dev.forkhandles.result4k.Result
import kotlinx.coroutines.flow.flow
import org.http4k.connect.RemoteFailure
import org.http4k.connect.orThrow

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

internal suspend inline fun <P, R> retry(
    initialParameter: P,
    errorTypeAndGetUpdatedParameter: Pair<String, suspend (String) -> P>,
    delayProvider: ExponentialBackoffWithJitter = ExponentialBackoffWithJitter(),
    maxAttempts: Int = 4,
    crossinline fn: suspend (P) -> Result<R, RemoteFailure>,
): R = retry(initialParameter, delayProvider, maxAttempts) {
    return fn(it).peekJsonError { error ->
        errorTypeAndGetUpdatedParameter.let { (errorType, getUpdatedParameter) ->
            if (error.__type.contains(errorType)) return@retry getUpdatedParameter(error.Message)
        }
    }.orThrow()
}

internal suspend inline fun <R> retry(
    errorType: String,
    delayProvider: ExponentialBackoffWithJitter = ExponentialBackoffWithJitter(),
    maxAttempts: Int = 4,
    crossinline fn: suspend () -> Result<R, RemoteFailure>,
): R = retry(delayProvider, maxAttempts) {
    return fn().peekJsonError {
        if (it.__type.contains(errorType)) return@retry
    }.orThrow()
}

private suspend inline fun <P> retry(
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
