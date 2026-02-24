package com.rootilabs.wmeCardiac.data.model

import com.squareup.moshi.Json

data class ApiError(
    val error: String? = null,
    @Json(name = "error_description")
    val errorDescription: String? = null
)
