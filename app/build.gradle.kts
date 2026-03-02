import com.google.devtools.ksp.gradle.KspExtension
import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.appdistribution)
    id("com.google.devtools.ksp") version "2.3.2"
}

android {
    namespace = "com.rootilabs.wmeCardiac"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rootilabs.wmeCardiac"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Firebase App Distribution configuration
            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotes = """
                    [v1.0.1 Update / 更新說明]
                    - EN: Added background sync to prevent 409 login conflicts.
                    - CH: 新增背景同步功能，解決登入 409 衝突問題。
                    - EN: Enabled offline tagging; buttons remain active without network.
                    - CH: 支援離線標記，斷網時按鈕仍可正常點擊。
                    - EN: Optimized logout dialog with unsynced data warnings.
                    - CH: 優化登出提醒，包含未上傳資料預警功能。
                    - EN: Enhanced UI aesthetics and localized dialog strings.
                    - CH: 優化介面視覺與多國語言對話框內容。
                    - EN: Updated Asia-Pacific 1 server to dev environment.
                    - CH: 更新 Asia-Pacific 1 伺服器為測試開發環境。
                """.trimIndent()
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-analytics")


    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Retrofit + Moshi (reflection, no codegen)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")

    // OkHttp
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Room
    implementation("androidx.room:room-runtime:2.7.0-rc01")
    implementation("androidx.room:room-ktx:2.7.0-rc01")
    ksp("androidx.room:room-compiler:2.7.0-rc01")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // CameraX
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // WorkManager (Background processing)
    implementation("androidx.work:work-runtime-ktx:2.10.0")
}