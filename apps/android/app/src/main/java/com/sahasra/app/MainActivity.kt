package com.sahasra.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sahasra.app.navigation.SahasraNavHost
import com.sahasra.app.navigation.Screen
import com.sahasra.app.ui.components.BottomNavBar
import com.sahasra.app.ui.theme.SahasraBg
import com.sahasra.app.ui.theme.SahasraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SahasraTheme {
                SahasraMainApp()
            }
        }
    }
}

@Composable
fun SahasraMainApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show BottomNavBar on root tab destinations
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Explore.route,
        Screen.Search.route,
        Screen.Schedule.route,
        Screen.Library.route,
        Screen.Settings.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SahasraBg,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        SahasraNavHost(
            navController = navController,
            paddingValues = innerPadding
        )
    }
}
