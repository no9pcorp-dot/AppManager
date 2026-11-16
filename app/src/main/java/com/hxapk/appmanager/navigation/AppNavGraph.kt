package com.hxapk.appmanager.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hxapk.appmanager.BottomNavItem
import com.hxapk.appmanager.ui.apps.AppsScreen
import com.hxapk.appmanager.ui.detail.AppDetailScreen

object Routes {
    const val APPS = "apps"
    const val APP_DETAIL = "app_detail/{packageName}"
    fun appDetail(packageName: String) = "app_detail/$packageName"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.APPS
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.APPS) {
            AppsScreen(
                onAppClick = { pkg ->
                    navController.navigate(Routes.appDetail(pkg))
                }
            )
        }
        composable(
            route = Routes.APP_DETAIL,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            AppDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(BottomNavItem.Installer.route) { Placeholder(BottomNavItem.Installer.label) }
        composable(BottomNavItem.Stats.route) { Placeholder(BottomNavItem.Stats.label) }
        composable(BottomNavItem.Settings.route) { Placeholder(BottomNavItem.Settings.label) }
    }
}

@Composable
private fun Placeholder(name: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, style = MaterialTheme.typography.titleLarge)
            Text("Tính năng đang phát triển", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
