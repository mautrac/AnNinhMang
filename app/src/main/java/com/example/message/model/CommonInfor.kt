package com.example.message.model

import java.math.BigInteger

data class CommonInfor constructor(
    val title: String? = null,
    val senderID: String? = null,
    val retrieverID: String? = null,
    var key: BigIntegerPair? = null
)

