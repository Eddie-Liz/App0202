package com.rootilabs.wmeCardiac.data.model

import com.squareup.moshi.Json

data class TokenResponse(
    @Json(name = "access_token")
    val accessToken: String?
)
