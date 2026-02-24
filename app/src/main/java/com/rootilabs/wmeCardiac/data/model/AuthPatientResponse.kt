package com.rootilabs.wmeCardiac.data.model

import com.squareup.moshi.Json

data class AuthPatientResponse(
    val vendorName: String? = null,
    val subscribeTime: String? = null,
    val subscribedBefore: Any? = null // Can be Boolean or String per API inconsistencies
)
