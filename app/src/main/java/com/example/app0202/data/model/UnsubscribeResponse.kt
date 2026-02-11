package com.example.app0202.data.model

data class UnsubscribeResponse(
    val status: String? = null,
    val message: String? = null,
    val unsubscribeTime: String? = null
)

data class UnsubscribeRequest(
    val deviceToken: String? = null,
    val deviceType: String = "2" // Trying "2" as it's common for Android
)
