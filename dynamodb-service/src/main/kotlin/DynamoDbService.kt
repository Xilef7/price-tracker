package com.xilef7.db

import com.xilef7.*
import com.xilef7.DynamoDbConstants.Companion.BATCH_GET_ITEM_MAX_ITEMS
import com.xilef7.DynamoDbConstants.Companion.BATCH_WRITE_ITEM_MAX_ITEMS
import com.xilef7.DynamoDbConstants.Companion.CONDITIONAL_CHECK_FAILED_EXCEPTION
import com.xilef7.DynamoDbConstants.Companion.PROVISIONED_THROUGHPUT_EXCEEDED_EXCEPTION
import com.xilef7.DynamoDbConstants.Companion.READ_UNIT_SIZE_IN_BYTES
import com.xilef7.PriceTrackerResources.Companion.DAILY_TABLE_ITEM_SIZE_IN_BYTES
import com.xilef7.PriceTrackerResources.Companion.HOURLY_TABLE_EXPIRY_DURATION
import com.xilef7.PriceTrackerResources.Companion.HOURLY_TABLE_ITEM_SIZE_IN_BYTES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.http4k.client.URLConnectionHttpClient
import org.http4k.connect.amazon.dynamodb.*
import org.http4k.connect.amazon.dynamodb.action.BatchGetItems
import org.http4k.connect.amazon.dynamodb.action.BatchWriteItems
import org.http4k.connect.amazon.dynamodb.model.ReqGetItem
import org.http4k.connect.amazon.dynamodb.model.ReqWriteItem
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.connect.model.Timestamp
import org.http4k.connect.orThrow
import java.math.BigInteger
import kotlin.time.Duration
import kotlin.time.Instant

class DynamoDbService {
    val client = DynamoDb.Http(http = URLConnectionHttpClient())

    suspend fun updateHourlyPrices(keyToStatistics: Map<Key, UserProvidedStatistics>) =
        addUserProvidedStatistics(tableHourly, keyToStatistics, HOURLY_TABLE_EXPIRY_DURATION)

    private suspend fun addUserProvidedStatistics(
        tableName: TableName,
        keyToStatistics: Map<Key, UserProvidedStatistics>,
        expiry: Duration? = null,
    ) = withContext(Dispatchers.Default) {
        suspend fun putCombinedHourlyStatistics(
            key: Key,
            storedStatistics: StoredStatistics? = null,
        ) = keyToStatistics.getValue(key).let { userProvidedStatistics ->
            retry(
                storedStatistics,
                CONDITIONAL_CHECK_FAILED_EXCEPTION to {
                    val meanNameAlias = "#mean"
                    val countNameAlias = "#count"
                    val maxNameAlias = "#max"
                    val minNameAlias = "#min"
                    val versionNameAlias = "#version"

                    retry(PROVISIONED_THROUGHPUT_EXCEEDED_EXCEPTION) {
                        client.getItem(
                            tableName,
                            key.toAttributeMap(),
                            ProjectionExpression = listOf(
                                meanNameAlias,
                                countNameAlias,
                                maxNameAlias,
                                minNameAlias,
                                versionNameAlias,
                            ).joinToString(","),
                            ExpressionAttributeNames = mapOf(
                                meanNameAlias to attrMean.name,
                                countNameAlias to attrCount.name,
                                maxNameAlias to attrMax.name,
                                minNameAlias to attrMin.name,
                                versionNameAlias to attrVersion.name,
                            ),
                            ConsistentRead = true,
                        )
                    }.item?.storedStatistics
                }
            ) {
                (it + userProvidedStatistics).let { combinedStatistics ->
                    val skuIdNameAlias = "#skuId"
                    val versionNameAlias = "#version"
                    val versionValueAlias = ":version"

                    client.putItem(
                        tableName,
                        (key to combinedStatistics).toAttributeMap(
                            newVersion = combinedStatistics.version + BigInteger.ONE,
                            expiry = expiry,
                        ),
                        ConditionExpression =
                            "attribute_not_exists($skuIdNameAlias) OR $versionNameAlias = $versionValueAlias",
                        ExpressionAttributeNames = mapOf(
                            skuIdNameAlias to attrSkuId.name,
                            versionNameAlias to attrVersion.name,
                        ),
                        ExpressionAttributeValues =
                            mapOf(versionValueAlias to attrVersion.asValue(combinedStatistics.version)),
                    )
                }
            }
        }

        keyToStatistics.keys.chunked(BATCH_GET_ITEM_MAX_ITEMS).forEach { keys ->
            launch {
                val nonExistentItemKeys = keys.toMutableSet()

                retry(
                    mapOf(
                        tableName to ReqGetItem.Get(
                            Keys = keys.map(Key::toAttributeMap),
                            ConsistentRead = true
                        )
                    ),
                    BatchGetItems::getUnprocessedKeys,
                ) {
                    retry(PROVISIONED_THROUGHPUT_EXCEEDED_EXCEPTION) {
                        client.batchGetItem(it)
                    }
                }.collect { response ->
                    response.Responses!!.getValue(tableName.value).forEach {
                        val key = it.key
                        nonExistentItemKeys.remove(key)

                        launch { putCombinedHourlyStatistics(key, it.storedStatistics) }
                    }
                }

                nonExistentItemKeys.forEach { key -> launch { putCombinedHourlyStatistics(key) } }
            }
        }
    }

    suspend fun updateDailyPrices(keyToStatistics: Map<Key, DeltaStatistics>) =
        addDeltaStatistics(tableDaily, keyToStatistics)

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun addDeltaStatistics(
        tableName: TableName,
        keyToStatistics: Map<Key, DeltaStatistics>,
        expiry: Duration? = null,
    ) = withContext(Dispatchers.Default) {
        flow {
            val nonExistentItemKeys = keyToStatistics.keys.toMutableSet()

            channelFlow {
                keyToStatistics.keys.chunked(BATCH_GET_ITEM_MAX_ITEMS).forEach { keys ->
                    launch {
                        retry(
                            mapOf(
                                tableName to ReqGetItem.Get(
                                    Keys = keys.map(Key::toAttributeMap),
                                    ConsistentRead = true,
                                )
                            ),
                            BatchGetItems::getUnprocessedKeys,
                            delayProvider = ExponentialBackoffWithJitter(jitter = 0.0),
                        ) {
                            retry(PROVISIONED_THROUGHPUT_EXCEEDED_EXCEPTION) {
                                client.batchGetItem(it)
                            }
                        }.collect { response ->
                            response.Responses!!.getValue(tableName.value).forEach { send(it) }
                        }
                    }
                }
            }.collect {
                val key = it.key
                nonExistentItemKeys.remove(key)

                val storedStatistics = it.storedStatistics
                val deltaStatistics = keyToStatistics.getValue(key)
                if (storedStatistics.version < deltaStatistics.earliestSequenceNumber)
                    emit(key to storedStatistics + deltaStatistics)
            }

            nonExistentItemKeys.forEach { key ->
                emit(key to null as StoredStatistics? + keyToStatistics.getValue(key))
            }
        }.chunked(BATCH_WRITE_ITEM_MAX_ITEMS).collect { pairs ->
            launch {
                retry(
                    mapOf(tableName to pairs.map {
                        ReqWriteItem.Put(it.toAttributeMap(expiry = expiry))
                    }),
                    BatchWriteItems::getUnprocessedItems,
                    delayProvider = ExponentialBackoffWithJitter(jitter = 0.0),
                ) {
                    retry(PROVISIONED_THROUGHPUT_EXCEEDED_EXCEPTION) {
                        client.batchWriteItem(it)
                    }
                }.collect()
            }
        }
    }

    fun getPastHourlyPrices(
        skuId: String,
        latestInstant: Instant,
        duration: Duration,
        lastEvaluatedKey: Key? = null,
    ) = getPrices(
        tableHourly,
        READ_UNIT_SIZE_IN_BYTES / HOURLY_TABLE_ITEM_SIZE_IN_BYTES,
        skuId,
        latestInstant,
        duration,
        lastEvaluatedKey,
    )

    fun getPastDailyPrices(
        skuId: String,
        latestInstant: Instant,
        duration: Duration,
        lastEvaluatedKey: Key? = null,
    ) = getPrices(
        tableDaily,
        READ_UNIT_SIZE_IN_BYTES / DAILY_TABLE_ITEM_SIZE_IN_BYTES,
        skuId,
        latestInstant,
        duration,
        lastEvaluatedKey,
    )

    private fun getPrices(
        tableName: TableName,
        limit: Int,
        skuId: String,
        latestInstant: Instant,
        duration: Duration,
        lastEvaluatedKey: Key? = null,
    ): List<ItemPriceAggregation> {
        val skuIdNameAlias = "#skuId"
        val skuIdValueAlias = ":skuId"
        val instantNameAlias = "#instant"
        val earliestInstantValueAlias = ":earliestInstant"
        val latestInstantValueAlias = ":latestInstant"
        val meanNameAlias = "#mean"
        val maxNameAlias = "#max"
        val minNameAlias = "#min"

        return client.queryPaginated(
            tableName,
            KeyConditionExpression =
                "$skuIdNameAlias = $skuIdValueAlias and $instantNameAlias BETWEEN $earliestInstantValueAlias AND $latestInstantValueAlias",
            ProjectionExpression = listOf(
                instantNameAlias,
                meanNameAlias,
                maxNameAlias,
                minNameAlias,
            ).joinToString(","),
            ExpressionAttributeNames = mapOf(
                skuIdNameAlias to attrSkuId.name,
                instantNameAlias to attrInstant.name,
                meanNameAlias to attrMean.name,
                maxNameAlias to attrMax.name,
                minNameAlias to attrMin.name,
            ),
            ExpressionAttributeValues = mapOf(
                skuIdValueAlias to attrSkuId.asValue(skuId.toLong()),
                earliestInstantValueAlias to attrInstant.asValue(
                    latestInstant.minus(duration).epochSeconds.let(
                        Timestamp::of
                    )
                ),
                latestInstantValueAlias to attrInstant.asValue(latestInstant.epochSeconds.let(Timestamp::of))
            ),
            ExclusiveStartKey = lastEvaluatedKey?.toAttributeMap(),
            Limit = limit,
            ScanIndexForward = false,
        ).flatMap { result ->
            result.orThrow().map {
                ItemPriceAggregation(
                    skuId,
                    attrInstant(it).value.let(Instant::fromEpochSeconds),
                    attrMean(it),
                    attrMax(it),
                    attrMin(it),
                )
            }
        }.toList()
    }
}
