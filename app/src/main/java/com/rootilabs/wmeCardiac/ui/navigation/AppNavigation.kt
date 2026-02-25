package com.rootilabs.wmeCardiac.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rootilabs.wmeCardiac.ui.history.HistoryScreen
import com.rootilabs.wmeCardiac.ui.login.LoginScreen
import com.rootilabs.wmeCardiac.ui.main.MainScreen
import com.rootilabs.wmeCardiac.ui.theme.TagGoGreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(startDestination: String = "login") {
    val navController = rememberNavController()

    // 永久綠色底色，防止狀態列反白
    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            // 頁面與綠色框框一起滑動（純滑動，不透明）
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500, easing = FastOutSlowInEasing))
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(500, easing = FastOutSlowInEasing))
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(500, easing = FastOutSlowInEasing))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500, easing = FastOutSlowInEasing))
            }
        ) {
            composable("login") { LoginScreen(onLoginSuccess = { navController.navigate("main") { popUpTo("login") { inclusive = true } } }) }
            composable("main") {
                MainScreen(
                    onLogout = { navController.navigate("login") { popUpTo("main") { inclusive = true } } },
                    onViewHistory = { navController.navigate("history") },
                    onViewProfile = {}
                )
            }
            composable("history") { HistoryScreen(onBack = { navController.popBackStack() }) }
            composable("profile") {
                com.rootilabs.wmeCardiac.ui.profile.ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogoutSuccess = { navController.navigate("login") { popUpTo("main") { inclusive = true } } }
                )
            }
        }
    }
}
