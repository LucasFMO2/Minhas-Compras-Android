package com.example.minhascompras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.minhascompras.data.AppDatabase
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.UpdatePreferencesManager
import com.example.minhascompras.data.ThemeMode
import com.example.minhascompras.data.ThemePreferencesManager
import com.example.minhascompras.data.UserPreferencesManager
import com.example.minhascompras.ui.screens.HistoryScreen
import com.example.minhascompras.ui.screens.ListaComprasScreen
import com.example.minhascompras.ui.screens.SettingsScreen
import com.example.minhascompras.ui.theme.MinhasComprasTheme
import com.example.minhascompras.ui.viewmodel.HistoryViewModel
import com.example.minhascompras.ui.viewmodel.HistoryViewModelFactory
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
    object History : Screen("history")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            // Ignorar erro se enableEdgeToEdge falhar
            android.util.Log.e("MainActivity", "Erro ao habilitar EdgeToEdge", e)
        }
        
        // Inicializar componentes com tratamento de erro
        val database = try {
            AppDatabase.getDatabase(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao inicializar banco de dados", e)
            // Tentar criar uma instância básica ou falhar graciosamente
            throw RuntimeException("Erro crítico: não foi possível inicializar o banco de dados", e)
        }
        
        val repository = try {
            ItemCompraRepository(database.itemCompraDao(), database.historyDao())
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao criar repository", e)
            throw RuntimeException("Erro crítico: não foi possível criar o repository", e)
        }
        
        val themePreferencesManager = try {
            ThemePreferencesManager(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao criar ThemePreferencesManager", e)
            throw RuntimeException("Erro crítico: não foi possível inicializar preferências de tema", e)
        }
        
        val themeViewModelFactory = ThemeViewModelFactory(themePreferencesManager)
        
        val userPreferencesManager = try {
            UserPreferencesManager(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao criar UserPreferencesManager", e)
            throw RuntimeException("Erro crítico: não foi possível inicializar preferências do usuário", e)
        }
        
        val viewModelFactory = ListaComprasViewModelFactory(repository, userPreferencesManager)
        val historyViewModelFactory = HistoryViewModelFactory(repository)
        
        // Rastrear uso do app e verificar atualizações automaticamente
        lifecycleScope.launch {
            try {
                // Atualizar timestamp de uso do app
                val updatePreferencesManager = UpdatePreferencesManager(applicationContext)
                updatePreferencesManager.updateLastAppUse()
                
                delay(3000) // Aguardar 3 segundos após abrir o app
                val updateViewModel = UpdateViewModel(applicationContext)
                updateViewModel.checkForUpdateInBackground()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Erro ao verificar atualizações", e)
                // Não falhar a inicialização se a verificação de atualização falhar
            }
        }
        
        try {
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
                                try {
                                    navController.navigate(Screen.Settings.route)
                                } catch (e: Exception) {
                                    android.util.Log.e("MainActivity", "Erro ao navegar para settings", e)
                                }
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
                                        try {
                                            navController.navigate(Screen.Settings.route)
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Erro ao navegar", e)
                                        }
                                    },
                                    onNavigateToHistory = {
                                        try {
                                            navController.navigate(Screen.History.route)
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Erro ao navegar", e)
                                        }
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
                                        try {
                                            navController.popBackStack()
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Erro ao voltar", e)
                                        }
                                    }
                                )
                            }
                            composable(Screen.History.route) {
                                val historyViewModel: HistoryViewModel = viewModel(
                                    factory = historyViewModelFactory
                                )
                                HistoryScreen(
                                    viewModel = historyViewModel,
                                    onNavigateBack = {
                                        try {
                                            navController.popBackStack()
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Erro ao voltar", e)
                                        }
                                    },
                                    onReuseList = {
                                        try {
                                            navController.popBackStack()
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Erro ao voltar", e)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro fatal no setContent", e)
            e.printStackTrace()
            // Se tudo falhar, pelo menos logar o erro
            finish()
        }
    }
}