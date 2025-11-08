package com.example.minhascompras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.minhascompras.data.AppDatabase
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.ThemeMode
import com.example.minhascompras.data.ThemePreferencesManager
import com.example.minhascompras.data.UserPreferencesManager
import com.example.minhascompras.ui.screens.ListaComprasScreen
import com.example.minhascompras.ui.screens.SettingsScreen
import com.example.minhascompras.ui.theme.MinhasComprasTheme
import com.example.minhascompras.ui.viewmodel.ListaComprasViewModel
import com.example.minhascompras.ui.viewmodel.ListaComprasViewModelFactory
import com.example.minhascompras.ui.viewmodel.ThemeViewModel
import com.example.minhascompras.ui.viewmodel.ThemeViewModelFactory
import com.example.minhascompras.ui.viewmodel.UpdateViewModel
import com.example.minhascompras.ui.viewmodel.UpdateViewModelFactory
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object ListaCompras : Screen("lista_compras")
    object Settings : Screen("settings")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ItemCompraRepository(database.itemCompraDao())
        
        val themePreferencesManager = ThemePreferencesManager(applicationContext)
        val themeViewModelFactory = ThemeViewModelFactory(themePreferencesManager)
        
        val userPreferencesManager = UserPreferencesManager(applicationContext)
        val viewModelFactory = ListaComprasViewModelFactory(repository, userPreferencesManager)
        
        // Verificar atualizações automaticamente após um delay
        lifecycleScope.launch {
            delay(3000) // Aguardar 3 segundos após abrir o app
            val updateViewModel = UpdateViewModel(applicationContext)
            updateViewModel.checkForUpdateInBackground()
        }
        
        setContent {
            val themeViewModel: ThemeViewModel = viewModel(factory = themeViewModelFactory)
            val themeMode by themeViewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val systemDarkTheme = isSystemInDarkTheme()
            
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemDarkTheme
            }
            
            MinhasComprasTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: ListaComprasViewModel = viewModel(factory = viewModelFactory)
                    
                    // Verificar se deve abrir configurações (vindo da notificação)
                    val shouldOpenSettings = intent?.getBooleanExtra("open_settings", false) ?: false
                    LaunchedEffect(shouldOpenSettings) {
                        if (shouldOpenSettings) {
                            navController.navigate(Screen.Settings.route)
                        }
                    }
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.ListaCompras.route
                    ) {
                        composable(Screen.ListaCompras.route) {
                            ListaComprasScreen(
                                viewModel = viewModel,
                                onNavigateToSettings = {
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                        }
                        composable(Screen.Settings.route) {
                            val updateViewModel: UpdateViewModel = viewModel(
                                factory = UpdateViewModelFactory(LocalContext.current)
                            )
                            SettingsScreen(
                                viewModel = viewModel,
                                themeViewModel = themeViewModel,
                                updateViewModel = updateViewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}