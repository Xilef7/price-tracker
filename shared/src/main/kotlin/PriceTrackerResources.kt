package com.xilef7

import kotlin.time.Duration.Companion.days

class PriceTrackerResources {
    companion object {
        const val HOURLY_TABLE = "Hourly"
        const val DAILY_TABLE = "Daily"
        const val SKU_ID = "i"
        const val INSTANT = "t"
        const val EXPIRY = "e"
        const val MIN = "l"
        const val MAX = "h"
        const val MEAN = "m"
        const val COUNT = "n"
        const val VERSION = "v"
        const val HOURLY_TABLE_ITEM_SIZE_IN_BYTES = 35
        const val DAILY_TABLE_ITEM_SIZE_IN_BYTES = 42
        val HOURLY_TABLE_EXPIRY_DURATION = 7.days
    }
}
