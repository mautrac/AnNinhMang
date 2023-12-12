package com.example.message.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User constructor(
    val uid: String? = null,
    val email: String? = null,
    val publicKey: BigIntegerPair? = null
)

data class BigIntegerPair(
    val first: String? = null,
    val second: String? = null
)