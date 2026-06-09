package com.xilef7

class DynamoDbConstants {
    companion object {
        const val BATCH_GET_ITEM_MAX_ITEMS = 100
        const val BATCH_WRITE_ITEM_MAX_ITEMS = 25
        const val WRITE_UNIT_SIZE_IN_BYTES = 1 * 1024
        const val READ_UNIT_SIZE_IN_BYTES = 4 * 1024

        const val EVENT_NAME_INSERT = "INSERT"
        const val EVENT_NAME_MODIFY = "MODIFY"
        const val EVENT_NAME_REMOVE = "REMOVE"

        const val IDENTITY_TYPE_SERVICE = "Service"
        const val IDENTITY_PRINCIPAL_ID_DYNAMODB = "dynamodb.amazonaws.com"
    }
}
