package com.example.recuperacionpgl.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.recuperacionpgl.LoginScreen
import com.example.recuperacionpgl.MainScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("main") { MainScreen(navController) }
    }
}
