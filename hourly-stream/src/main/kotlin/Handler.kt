package com.xilef7.hourlystream

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.DynamodbTimeWindowEvent
import com.amazonaws.services.lambda.runtime.events.TimeWindowEventResponse
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.Identity
import com.xilef7.DeltaStatistics
import com.xilef7.DynamoDbConstants.Companion.EVENT_NAME_INSERT
import com.xilef7.DynamoDbConstants.Companion.EVENT_NAME_MODIFY
import com.xilef7.DynamoDbConstants.Companion.EVENT_NAME_REMOVE
import com.xilef7.DynamoDbConstants.Companion.IDENTITY_PRINCIPAL_ID_DYNAMODB
import com.xilef7.DynamoDbConstants.Companion.IDENTITY_TYPE_SERVICE
import com.xilef7.Key
import com.xilef7.db.DynamoDbService
import com.xilef7.plus
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.http4k.serverless.AwsLambdaRuntime
import org.http4k.serverless.FnHandler
import org.http4k.serverless.FnLoader
import org.http4k.serverless.asServer

val dbService = DynamoDbService()

fun main() {
    FnLoader {
        FnHandler { event: DynamodbTimeWindowEvent, _: Context ->
            runBlocking {
                mutableMapOf<Key, DeltaStatistics>().apply {
                    event.state?.forEach { (key, value) ->
                        this[Json.decodeFromString(key!!)] = Json.decodeFromString(value!!)
                    }
                }.let { keyToStatistics ->
                    if (event.isFinalInvokeForWindow) {
                        if (keyToStatistics.isNotEmpty()) dbService.updateDailyPrices(keyToStatistics)
                        TimeWindowEventResponse()
                    } else keyToStatistics.apply {
                        event.records
                            .asSequence()
                            .filter {
                                when (it.eventName) {
                                    EVENT_NAME_INSERT -> true
                                    EVENT_NAME_MODIFY -> true
                                    EVENT_NAME_REMOVE -> it.userIdentity != Identity()
                                        .withType(IDENTITY_TYPE_SERVICE)
                                        .withPrincipalId(IDENTITY_PRINCIPAL_ID_DYNAMODB)

                                    else -> throw IllegalStateException("Invalid event name: ${it.eventName}")
                                }
                            }
                            .forEach { this[it.key] += it.deltaStatistics }
                    }.let {
                        TimeWindowEventResponse
                            .builder()
                            .withState(
                                it.map { (key, value) ->
                                    Json.encodeToString(key) to Json.encodeToString(value)
                                }.toMap()
                            )
                            .build()!!
                    }
                }
            }
        }
    }.asServer(AwsLambdaRuntime()).start()
}
