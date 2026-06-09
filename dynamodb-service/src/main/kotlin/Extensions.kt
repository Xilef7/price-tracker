package com.xilef7.db

import com.xilef7.Key
import com.xilef7.StoredStatistics
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.peekFailure
import org.http4k.connect.RemoteFailure
import org.http4k.connect.amazon.JsonError
import org.http4k.connect.amazon.dynamodb.DynamoDbMoshi
import org.http4k.connect.amazon.dynamodb.action.BatchGetItems
import org.http4k.connect.amazon.dynamodb.action.BatchWriteItems
import org.http4k.connect.amazon.dynamodb.model.Item
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.connect.amazon.dynamodb.model.with
import org.http4k.connect.model.Timestamp
import java.math.BigInteger
import kotlin.time.Duration
import kotlin.time.Instant

val Item.key
    get() = Key(
        attrSkuId(this).toString(),
        attrInstant(this).value.let(Instant::fromEpochSeconds),
    )

val Item.storedStatistics
    get() = StoredStatistics(
        attrMean(this),
        attrCount(this).toShort(),
        attrMax(this),
        attrMin(this),
        attrVersion(this),
    )

fun Key.toAttributeMap() = org.http4k.connect.amazon.dynamodb.model.Key(
    attrSkuId of skuId.toLong(),
    attrInstant of instant.epochSeconds.let(Timestamp::of),
)

fun Pair<Key, StoredStatistics>.toAttributeMap(
    newVersion: BigInteger? = null,
    expiry: Duration? = null
): Item {
    val (key, value) = this
    val (skuId, instant) = key
    val (mean, count, max, min, version) = value

    return Item(
        attrSkuId of skuId.toLong(),
        attrInstant of instant.epochSeconds.let(Timestamp::of),
        attrMean of mean,
        attrCount of count.toInt(),
        attrMax of max,
        attrMin of min,
        attrVersion of (newVersion ?: version),
    ).let { item ->
        if (expiry != null)
            item.with(
                attrExpiry of instant.plus(expiry).epochSeconds.let(Timestamp::of)
            )
        else
            item
    }
}

fun BatchGetItems.getUnprocessedKeys() = this.UnprocessedKeys!!.mapKeys { (key, _) -> TableName.of(key) }
fun BatchWriteItems.getUnprocessedItems() = this.UnprocessedItems!!.asSequence().associate { (key, value) ->
    TableName.of(key) to listOf(value) // TODO Bug from http4k
}

inline fun <R> Result<R, RemoteFailure>.peekJsonError(f: (JsonError) -> Unit) = this.peekFailure { reason ->
    reason.message?.let {
        DynamoDbMoshi.asA<JsonError>(it).let(f)
    }
}
