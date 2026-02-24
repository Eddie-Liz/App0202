package com.rootilabs.wmeCardiac.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rootilabs.wmeCardiac.ui.history.HistoryScreen
import com.rootilabs.wmeCardiac.ui.login.LoginScreen
import com.rootilabs.wmeCardiac.ui.main.MainScreen

@Composable
fun AppNavigation(startDestination: String = "login") {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onViewHistory = {
                    navController.navigate("history")
                },
                onViewProfile = {
                    navController.navigate("profile")
                }
            )
        }
        composable("history") {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("profile") {
            com.rootilabs.wmeCardiac.ui.profile.ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogoutSuccess = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}
