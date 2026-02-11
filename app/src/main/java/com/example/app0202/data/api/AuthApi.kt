package com.example.app0202.data.api

import com.example.app0202.data.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("/oauth/token")
    suspend fun getToken(
        @Header("Authorization") basicAuth: String,
        @Body body: Map<String, String>
    ): Response<TokenResponse>
}
