package com.example.app0202

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.app0202.ui.navigation.AppNavigation
import com.example.app0202.ui.theme.App0202Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App0202Theme {
                AppNavigation()
            }
        }
    }
}
