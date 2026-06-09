package com.xilef7.hourlygetter

import com.xilef7.db.DynamoDbService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import org.http4k.serverless.ApiGatewayV1LambdaFunction
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

val corsHeaderMap = listOf("Access-Control-Allow-Origin" to "*")
val dbService = DynamoDbService()

@Suppress("unused")
class Handler : ApiGatewayV1LambdaFunction({ request: Request ->
    runBlocking {
        dbService.getPastHourlyPrices(
            request.path("sku_id")!!,
            Clock.System.now(),
            7.days,
            null,
        ).let {
            Response(Status.OK)
                .headers(corsHeaderMap)
                .body(Json.encodeToString(it))
        }
    }
})
