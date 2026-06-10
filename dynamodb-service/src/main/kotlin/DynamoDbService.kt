package com.xilef7.db

import aws.sdk.kotlin.runtime.auth.credentials.EnvironmentCredentialsProvider
import aws.sdk.kotlin.runtime.region.EnvironmentRegionProvider
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.batchGetItem
import aws.sdk.kotlin.services.dynamodb.getItem
import aws.sdk.kotlin.services.dynamodb.model.*
import aws.sdk.kotlin.services.dynamodb.paginators.queryPaginated
import com.xilef7.*
import com.xilef7.DynamoDbConstants.Companion.BATCH_GET_ITEM_MAX_ITEMS
import com.xilef7.DynamoDbConstants.Companion.BATCH_WRITE_ITEM_MAX_ITEMS
import com.xilef7.DynamoDbConstants.Companion.READ_UNIT_SIZE_IN_BYTES
import com.xilef7.PriceTrackerResources.Companion.COUNT
import com.xilef7.PriceTrackerResources.Companion.DAILY_TABLE
import com.xilef7.PriceTrackerResources.Companion.DAILY_TABLE_ITEM_SIZE_IN_BYTES
import com.xilef7.PriceTrackerResources.Companion.HOURLY_TABLE
import com.xilef7.PriceTrackerResources.Companion.HOURLY_TABLE_EXPIRY_DURATION
import com.xilef7.PriceTrackerResources.Companion.HOURLY_TABLE_ITEM_SIZE_IN_BYTES
import com.xilef7.PriceTrackerResources.Companion.INSTANT
import com.xilef7.PriceTrackerResources.Companion.MAX
import com.xilef7.PriceTrackerResources.Companion.MEAN
import com.xilef7.PriceTrackerResources.Companion.MIN
import com.xilef7.PriceTrackerResources.Companion.SKU_ID
import com.xilef7.PriceTrackerResources.Companion.VERSION
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.math.BigInteger
import kotlin.time.Duration
import kotlin.time.Instant

class DynamoDbService : AutoCloseable {
    val client = runBlocking {
        DynamoDbClient.fromEnvironment {
            regionProvider = EnvironmentRegionProvider()
            credentialsProvider = EnvironmentCredentialsProvider()
        }
    }

    suspend fun updateHourlyPrices(keyToStatistics: Map<Key, UserProvidedStatistics>) =
        addUserProvidedStatistics(HOURLY_TABLE, keyToStatistics, HOURLY_TABLE_EXPIRY_DURATION)

    private suspend fun addUserProvidedStatistics(
        tableName: String,
        keyToStatistics: Map<Key, UserProvidedStatistics>,
        expiry: Duration? = null,
    ) = withContext(Dispatchers.Default) {
        suspend fun putCombinedHourlyStatistics(
            key: Key,
            storedStatistics: StoredStatistics? = null,
        ) = keyToStatistics.getValue(key).let { userProvidedStatistics ->
            retry(
                storedStatistics,
                { _: ConditionalCheckFailedException ->
                    client.getItem {
                        val meanNameAlias = "#mean"
                        val countNameAlias = "#count"
                        val maxNameAlias = "#max"
                        val minNameAlias = "#min"
                        val versionNameAlias = "#version"

                        this.tableName = tableName
                        this.key = key.toAttributeMap()
                        projectionExpression = listOf(
                            meanNameAlias,
                            countNameAlias,
                            maxNameAlias,
                            minNameAlias,
                            versionNameAlias,
                        ).joinToString(",")
                        consistentRead = true
                        expressionAttributeNames = mapOf(
                            meanNameAlias to MEAN,
                            countNameAlias to COUNT,
                            maxNameAlias to MAX,
                            minNameAlias to MIN,
                            versionNameAlias to VERSION,
                        )
                    }.item?.storedStatistics
                }
            ) {
                (it + userProvidedStatistics).let { combinedStatistics ->
                    client.putItem(PutItemRequest {
                        val skuIdNameAlias = "#skuId"
                        val versionNameAlias = "#version"
                        val versionValueAlias = ":version"

                        this.tableName = tableName
                        item = (key to combinedStatistics).toAttributeMap(
                            newVersion = combinedStatistics.version + BigInteger.ONE,
                            expiry = expiry,
                        )
                        conditionExpression =
                            "attribute_not_exists($skuIdNameAlias) OR $versionNameAlias = $versionValueAlias"
                        expressionAttributeNames = mapOf(
                            skuIdNameAlias to SKU_ID,
                            versionNameAlias to VERSION,
                        )
                        expressionAttributeValues = mapOf(
                            versionValueAlias to AttributeValue.N(combinedStatistics.version.toString()),
                        )
                    })
                }
            }
        }

        keyToStatistics.keys.chunked(BATCH_GET_ITEM_MAX_ITEMS).forEach { keys ->
            launch {
                val nonExistentItemKeys = keys.toMutableSet()

                retry(
                    mapOf(tableName to KeysAndAttributes {
                        this.keys = keys.map(Key::toAttributeMap)
                        consistentRead = true
                    }),
                    BatchGetItemResponse::unprocessedKeys,
                ) { client.batchGetItem { requestItems = it } }.collect { response ->
                    response.responses!!.getValue(tableName).forEach {
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
        addDeltaStatistics(DAILY_TABLE, keyToStatistics)

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun addDeltaStatistics(
        tableName: String,
        keyToStatistics: Map<Key, DeltaStatistics>,
        expiry: Duration? = null,
    ) = withContext(Dispatchers.Default) {
        flow {
            val nonExistentItemKeys = keyToStatistics.keys.toMutableSet()

            channelFlow {
                keyToStatistics.keys.chunked(BATCH_GET_ITEM_MAX_ITEMS).forEach { keys ->
                    launch {
                        retry(
                            mapOf(tableName to KeysAndAttributes {
                                this.keys = keys.map(Key::toAttributeMap)
                                consistentRead = true
                            }),
                            BatchGetItemResponse::unprocessedKeys,
                            delayProvider = ExponentialBackoffWithJitter(jitter = 0.0),
                        ) { client.batchGetItem { requestItems = it } }.collect { response ->
                            response.responses!!.getValue(tableName).forEach { send(it) }
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
                        WriteRequest { putRequest = PutRequest { item = it.toAttributeMap(expiry = expiry) } }
                    }),
                    BatchWriteItemResponse::unprocessedItems,
                    delayProvider = ExponentialBackoffWithJitter(jitter = 0.0),
                ) { client.batchWriteItem(BatchWriteItemRequest { requestItems = it }) }.collect()
            }
        }
    }

    suspend fun getPastHourlyPrices(
        skuId: String,
        latestInstant: Instant,
        duration: Duration,
        lastEvaluatedKey: Key? = null,
    ) = getPrices(
        HOURLY_TABLE,
        READ_UNIT_SIZE_IN_BYTES / HOURLY_TABLE_ITEM_SIZE_IN_BYTES,
        skuId,
        latestInstant,
        duration,
        lastEvaluatedKey,
    )

    suspend fun getPastDailyPrices(
        skuId: String,
        latestInstant: Instant,
        duration: Duration,
        lastEvaluatedKey: Key? = null,
    ) = getPrices(
        DAILY_TABLE,
        READ_UNIT_SIZE_IN_BYTES / DAILY_TABLE_ITEM_SIZE_IN_BYTES,
        skuId,
        latestInstant,
        duration,
        lastEvaluatedKey,
    )

    private suspend fun getPrices(
        tableName: String,
        limit: Int,
        skuId: String,
        latestInstant: Instant,
        duration: Duration,
        lastEvaluatedKey: Key? = null,
    ) = client.queryPaginated {
        val skuIdNameAlias = "#skuId"
        val skuIdValueAlias = ":skuId"
        val instantNameAlias = "#instant"
        val earliestInstantValueAlias = ":earliestInstant"
        val latestInstantValueAlias = ":latestInstant"
        val meanNameAlias = "#mean"
        val maxNameAlias = "#max"
        val minNameAlias = "#min"

        this.tableName = tableName
        keyConditionExpression =
            "$skuIdNameAlias = $skuIdValueAlias and $instantNameAlias BETWEEN $earliestInstantValueAlias AND $latestInstantValueAlias"
        exclusiveStartKey = lastEvaluatedKey?.toAttributeMap()
        projectionExpression = listOf(
            instantNameAlias,
            meanNameAlias,
            maxNameAlias,
            minNameAlias,
        ).joinToString(",")
        scanIndexForward = false
        this.limit = limit
        expressionAttributeNames = mapOf(
            skuIdNameAlias to SKU_ID,
            instantNameAlias to INSTANT,
            meanNameAlias to MEAN,
            maxNameAlias to MAX,
            minNameAlias to MIN,
        )
        expressionAttributeValues = mapOf(
            skuIdValueAlias to AttributeValue.N(skuId),
            earliestInstantValueAlias to AttributeValue.N(latestInstant.minus(duration).epochSeconds.toString()),
            latestInstantValueAlias to AttributeValue.N(latestInstant.epochSeconds.toString()),
        )
    }.transform { response ->
        response.items!!.forEach {
            emit(
                ItemPriceAggregation(
                    skuId,
                    it.getValue(INSTANT).asN().toLong().let(Instant::fromEpochSeconds),
                    it.getValue(MEAN).asN().toFloat(),
                    it.getValue(MAX).asN().toInt(),
                    it.getValue(MIN).asN().toInt(),
                )
            )
        }
    }.toList()

    override fun close() {
        client.close()
    }
}
