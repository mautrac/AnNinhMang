package com.example.message.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Message constructor(
    val senderID: String? = null,
    val retrieverID: String? = null,
    var text: String? = null
)