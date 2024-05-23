package com.example.recuperacionpgl.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.recuperacionpgl.MainScreen
import com.example.recuperacionpgl.LoginScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = "main") {
        composable("login") { LoginScreen(navController) }
        composable("main") { MainScreen(navController) }
    }
}
