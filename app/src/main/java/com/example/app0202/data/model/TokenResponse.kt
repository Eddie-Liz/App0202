package com.example.app0202.data.model

import com.squareup.moshi.Json

data class TokenResponse(
    @Json(name = "access_token")
    val accessToken: String?
)
