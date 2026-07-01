package com.santimattius.kvs.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.santimattius.kvs.ui.screens.DocumentScreen
import com.santimattius.kvs.ui.screens.EncryptDocumentScreen
import com.santimattius.kvs.ui.screens.HomeScreen
import com.santimattius.kvs.ui.screens.InMemoryScreen
import com.santimattius.kvs.ui.screens.KvsLightEncryptScreen
import com.santimattius.kvs.ui.screens.KvsLightScreen
import com.santimattius.kvs.ui.screens.KvsOptimizedScreen

/** Route names for the 6 [com.santimattius.kvs.Storage] factory demo screens. */
object AppRoutes {
    const val HOME = "home"
    const val IN_MEMORY = "in_memory"
    const val KVS_LIGHT = "kvs_light"
    const val KVS_LIGHT_ENCRYPT = "kvs_light_encrypt"
    const val KVS_OPTIMIZED = "kvs_optimized"
    const val DOCUMENT = "document"
    const val ENCRYPT_DOCUMENT = "encrypt_document"
}

/** Single-Activity Compose NavHost — one route per [com.santimattius.kvs.Storage] factory. */
@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = AppRoutes.HOME) {
        composable(AppRoutes.HOME) {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable(AppRoutes.IN_MEMORY) { InMemoryScreen() }
        composable(AppRoutes.KVS_LIGHT) { KvsLightScreen() }
        composable(AppRoutes.KVS_LIGHT_ENCRYPT) { KvsLightEncryptScreen() }
        composable(AppRoutes.KVS_OPTIMIZED) { KvsOptimizedScreen() }
        composable(AppRoutes.DOCUMENT) { DocumentScreen() }
        composable(AppRoutes.ENCRYPT_DOCUMENT) { EncryptDocumentScreen() }
    }
}
