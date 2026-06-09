package com.xilef7.hourlystream

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord
import com.xilef7.DeltaStatistics
import com.xilef7.DynamoDbConstants.Companion.EVENT_NAME_INSERT
import com.xilef7.DynamoDbConstants.Companion.EVENT_NAME_MODIFY
import com.xilef7.DynamoDbConstants.Companion.EVENT_NAME_REMOVE
import com.xilef7.Key
import com.xilef7.PriceTrackerResources.Companion.INSTANT
import com.xilef7.PriceTrackerResources.Companion.MAX
import com.xilef7.PriceTrackerResources.Companion.MEAN
import com.xilef7.PriceTrackerResources.Companion.MIN
import com.xilef7.PriceTrackerResources.Companion.SKU_ID
import com.xilef7.truncateTo
import kotlin.time.DurationUnit
import kotlin.time.Instant

val (DynamodbStreamRecord).key
    get() = dynamodb.keys.let {
        Key(
            it.getValue(SKU_ID).n,
            it.getValue(INSTANT).n
                .toLong()
                .let(Instant::fromEpochSeconds)
                .truncateTo(1, DurationUnit.DAYS),
        )
    }

val (DynamodbStreamRecord).deltaStatistics
    get(): DeltaStatistics {
        val sequenceNumber = dynamodb.sequenceNumber.toBigInteger()

        return when (eventName) {
            EVENT_NAME_INSERT -> {
                val newImage = dynamodb.newImage

                DeltaStatistics(
                    delta = 0f,
                    mean = newImage.getValue(MEAN).n.toFloat(),
                    count = 1,
                    max = newImage.getValue(MAX).n.toInt(),
                    min = newImage.getValue(MIN).n.toInt(),
                    earliestSequenceNumber = sequenceNumber,
                    latestSequenceNumber = sequenceNumber,
                )
            }

            EVENT_NAME_MODIFY -> {
                val newImage = dynamodb.newImage
                val oldImage = dynamodb.oldImage

                DeltaStatistics(
                    delta = newImage.getValue(MEAN).n.toFloat() - oldImage.getValue(MEAN).n.toFloat(),
                    mean = 0f,
                    count = 0,
                    max = maxOf(newImage.getValue(MAX).n.toInt(), oldImage.getValue(MAX).n.toInt()),
                    min = minOf(newImage.getValue(MIN).n.toInt(), oldImage.getValue(MIN).n.toInt()),
                    earliestSequenceNumber = sequenceNumber,
                    latestSequenceNumber = sequenceNumber,
                )
            }

            EVENT_NAME_REMOVE -> {
                val oldImage = dynamodb.oldImage

                DeltaStatistics(
                    delta = 0f,
                    mean = oldImage.getValue(MEAN).n.toFloat(),
                    count = -1,
                    max = oldImage.getValue(MAX).n.toInt(),
                    min = oldImage.getValue(MIN).n.toInt(),
                    earliestSequenceNumber = sequenceNumber,
                    latestSequenceNumber = sequenceNumber,
                )
            }

            else -> throw IllegalStateException("Invalid event name: $eventName")
        }
    }
