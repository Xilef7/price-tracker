package com.xilef7.hourlygetter

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.xilef7.db.DynamoDbService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Suppress("unused")
class Handler : RequestHandler<APIGatewayProxyRequestEvent?, APIGatewayProxyResponseEvent?> {
    val corsHeaderMap = mapOf("Access-Control-Allow-Origin" to "*")
    val dbService = DynamoDbService()

    override fun handleRequest(input: APIGatewayProxyRequestEvent?, context: Context): APIGatewayProxyResponseEvent =
        runBlocking {
            dbService.getPastHourlyPrices(
                input!!.pathParameters.getValue("sku_id"),
                Clock.System.now(),
                7.days,
                null,
            ).let {
                APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(corsHeaderMap)
                    .withBody(Json.encodeToString(it))
                    .withIsBase64Encoded(false)
            }
        }
}
