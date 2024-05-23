package com.example.recuperacionpgl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.recuperacionpgl.navigation.AppNavigation
import com.example.recuperacionpgl.ui.theme.RecuperacionPGLTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecuperacionPGLTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}