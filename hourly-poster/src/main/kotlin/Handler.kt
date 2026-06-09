package com.xilef7.hourlyposter

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.xilef7.Key
import com.xilef7.UserProvidedStatistics
import com.xilef7.db.DynamoDbService
import com.xilef7.truncateTo
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.time.DurationUnit.HOURS
import kotlin.time.Instant

@Suppress("unused")
class Handler : RequestHandler<APIGatewayProxyRequestEvent?, APIGatewayProxyResponseEvent?> {
    val corsHeaderMap = mapOf("Access-Control-Allow-Origin" to "*")
    val dbService = DynamoDbService()

    override fun handleRequest(input: APIGatewayProxyRequestEvent?, context: Context): APIGatewayProxyResponseEvent =
        runBlocking {
            dbService.updateHourlyPrices(buildMap {
                Json.decodeFromString<Map<Instant, Map<String, UserProvidedStatistics>>>(input!!.body)
                    .forEach { (instant, skuIdToStatistics) ->
                        skuIdToStatistics.forEach { (skuId, statistics) ->
                            put(Key(skuId, instant.truncateTo(1, HOURS)), statistics)
                        }
                    }
            }).let {
                APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(corsHeaderMap)
                    .withIsBase64Encoded(false)
            }
        }
}
