package com.example.app0202.data.auth

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("rooticare_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_SERVER_DEVICE_ID = "server_device_id"
        private const val KEY_VENDOR_NAME = "vendor_name"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_INSTITUTION_ID = "institution_id"
        private const val KEY_PATIENT_ID = "patient_id"
        private const val KEY_MEASURE_RECORD_ID = "measure_record_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_IS_MEASURING = "is_measuring"
    }

    var serverDeviceId: String?
        get() = prefs.getString(KEY_SERVER_DEVICE_ID, null)
        set(value) = prefs.edit().putString(KEY_SERVER_DEVICE_ID, value).apply()

    var deviceId: String
        get() {
            var id = prefs.getString(KEY_DEVICE_ID, null)
            if (id == null) {
                id = java.util.UUID.randomUUID().toString()
                prefs.edit().putString(KEY_DEVICE_ID, id).apply()
            }
            return id
        }
        set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()

    var vendorName: String?
        get() = prefs.getString(KEY_VENDOR_NAME, null)
        set(value) = prefs.edit().putString(KEY_VENDOR_NAME, value).apply()

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()

    var institutionId: String?
        get() = prefs.getString(KEY_INSTITUTION_ID, null)
        set(value) = prefs.edit().putString(KEY_INSTITUTION_ID, value).apply()

    var patientId: String?
        get() = prefs.getString(KEY_PATIENT_ID, null)
        set(value) = prefs.edit().putString(KEY_PATIENT_ID, value).apply()

    var measureRecordId: String?
        get() = prefs.getString(KEY_MEASURE_RECORD_ID, null)
        set(value) = prefs.edit().putString(KEY_MEASURE_RECORD_ID, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    var loginTime: Long
        get() = prefs.getLong(KEY_LOGIN_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LOGIN_TIME, value).apply()

    var isMeasuring: Boolean
        get() = prefs.getBoolean(KEY_IS_MEASURING, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_MEASURING, value).apply()

    fun clearAll() {
        val deviceId = prefs.getString(KEY_DEVICE_ID, null)
        prefs.edit().clear().apply()
        if (deviceId != null) {
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
    }
}
