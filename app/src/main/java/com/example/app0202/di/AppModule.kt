package com.example.app0202.di

import android.content.Context
import androidx.room.Room
import com.example.app0202.Constants
import com.example.app0202.data.api.AuthApi
import com.example.app0202.data.api.RootiCareApi
import com.example.app0202.data.auth.AuthInterceptor
import com.example.app0202.data.auth.TokenManager
import com.example.app0202.data.local.AppDatabase
import com.example.app0202.data.repository.RootiCareRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Simple service locator for dependency injection (no Hilt needed)
 */
object ServiceLocator {

    private var _instance: ServiceLocator? = null
    private lateinit var appContext: Context

    lateinit var tokenManager: TokenManager
        private set
    lateinit var moshi: Moshi
        private set
    lateinit var authApi: AuthApi
        private set
    lateinit var rootiCareApi: RootiCareApi
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var repository: RootiCareRepository
        private set

    fun init(context: Context) {
        if (_instance != null) return
        _instance = this
        appContext = context.applicationContext

        tokenManager = TokenManager(appContext)

        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        // Auth OkHttp (no bearer token)
        val authClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        authApi = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(authClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AuthApi::class.java)

        // Main OkHttp (with bearer token)
        val mainClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor { tokenManager.accessToken })
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        rootiCareApi = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(mainClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RootiCareApi::class.java)

        database = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "rooticare_db"
        ).fallbackToDestructiveMigration().build()

        repository = RootiCareRepository(
            authApi = authApi,
            rootiCareApi = rootiCareApi,
            tokenManager = tokenManager,
            database = database,
            moshi = moshi
        )
    }
}
