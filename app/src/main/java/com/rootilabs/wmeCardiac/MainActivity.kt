package com.rootilabs.wmeCardiac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rootilabs.wmeCardiac.di.ServiceLocator
import com.rootilabs.wmeCardiac.ui.navigation.AppNavigation
import com.rootilabs.wmeCardiac.ui.theme.TagGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Only skip login if both logged in flag is true AND we have a token
        val hasSession = ServiceLocator.tokenManager.isLoggedIn && 
                        !ServiceLocator.tokenManager.accessToken.isNullOrBlank()
        
        val startDest = if (hasSession) "main" else "login"
        
        setContent {
            TagGoTheme {
                AppNavigation(startDestination = startDest)
            }
        }
    }
}
