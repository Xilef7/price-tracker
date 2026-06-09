package com.xilef7.hourlyposter

import com.xilef7.Key
import com.xilef7.UserProvidedStatistics
import com.xilef7.db.DynamoDbService
import com.xilef7.truncateTo
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.serverless.ApiGatewayV1FnLoader
import org.http4k.serverless.AwsLambdaRuntime
import org.http4k.serverless.asServer
import kotlin.time.DurationUnit.HOURS
import kotlin.time.Instant

val dbService = DynamoDbService()
val corsHeaderMap = listOf("Access-Control-Allow-Origin" to "*")

fun main() {
    ApiGatewayV1FnLoader { request: Request ->
        runBlocking {
            dbService.updateHourlyPrices(buildMap {
                Json.decodeFromString<Map<Instant, Map<String, UserProvidedStatistics>>>(request.body.toString())
                    .forEach { (instant, skuIdToStatistics) ->
                        skuIdToStatistics.forEach { (skuId, statistics) ->
                            put(Key(skuId, instant.truncateTo(1, HOURS)), statistics)
                        }
                    }
            }).let {
                Response(Status.OK)
                    .headers(corsHeaderMap)
            }
        }
    }.asServer(AwsLambdaRuntime()).start()
}
