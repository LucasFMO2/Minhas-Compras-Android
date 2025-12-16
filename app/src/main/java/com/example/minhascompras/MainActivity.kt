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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.Manifest
import android.os.Build
import com.example.minhascompras.data.AppDatabase
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.data.ShoppingListRepository
import com.example.minhascompras.data.ShoppingListPreferencesManager
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
import com.example.minhascompras.ui.viewmodel.ShoppingListViewModel
import com.example.minhascompras.ui.viewmodel.ShoppingListViewModelFactory
import com.example.minhascompras.ui.viewmodel.ThemeViewModel
import com.example.minhascompras.ui.viewmodel.ThemeViewModelFactory
import com.example.minhascompras.ui.viewmodel.UpdateViewModel
import com.example.minhascompras.ui.viewmodel.UpdateViewModelFactory
import com.example.minhascompras.ui.viewmodel.StatisticsViewModel
import com.example.minhascompras.ui.viewmodel.StatisticsViewModelFactory
import com.example.minhascompras.ui.screens.StatisticsScreen
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object ListaCompras : Screen("lista_compras")
    object Settings : Screen("settings")
    object History : Screen("history")
    object Statistics : Screen("statistics")
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
            ItemCompraRepository(database.itemCompraDao(), database.historyDao(), applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao criar repository", e)
            throw RuntimeException("Erro crítico: não foi possível criar o repository", e)
        }
        
        val shoppingListRepository = try {
            ShoppingListRepository(database.shoppingListDao())
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao criar ShoppingListRepository", e)
            throw RuntimeException("Erro crítico: não foi possível criar o ShoppingListRepository", e)
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
        
        val shoppingListPreferencesManager = try {
            ShoppingListPreferencesManager(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao criar ShoppingListPreferencesManager", e)
            throw RuntimeException("Erro crítico: não foi possível inicializar preferências de lista", e)
        }
        
        val historyViewModelFactory = HistoryViewModelFactory(repository, shoppingListPreferencesManager)
        val shoppingListViewModelFactory = ShoppingListViewModelFactory(shoppingListRepository, shoppingListPreferencesManager)
        val updateViewModelFactory = UpdateViewModelFactory(applicationContext)
        val statisticsViewModelFactory = com.example.minhascompras.ui.viewmodel.StatisticsViewModelFactory(repository)
        
        val notificationPreferencesManager = try {
            com.example.minhascompras.data.NotificationPreferencesManager(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao criar NotificationPreferencesManager", e)
            throw RuntimeException("Erro crítico: não foi possível inicializar preferências de notificações", e)
        }
        
        val notificationViewModelFactory = com.example.minhascompras.ui.viewmodel.NotificationViewModelFactory(
            notificationPreferencesManager,
            applicationContext
        )
        
        // Rastrear uso do app quando vai para primeiro plano
        lifecycleScope.launch {
            try {
                val updatePreferencesManager = UpdatePreferencesManager(applicationContext)
                updatePreferencesManager.updateLastAppUse()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Erro ao atualizar timestamp de uso", e)
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
                    val context = LocalContext.current
                    
                    // Solicitar permissão de notificações para Android 13+ (API 33+)
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            android.util.Log.d("MainActivity", "Permissão de notificações concedida")
                        } else {
                            android.util.Log.w("MainActivity", "Permissão de notificações negada")
                        }
                    }
                    
                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            
                            if (!hasPermission) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                    
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        // Criar ShoppingListViewModel primeiro
                        val shoppingListViewModel: ShoppingListViewModel = viewModel(factory = shoppingListViewModelFactory)
                        // Criar Factory do ListaComprasViewModel com o ShoppingListViewModel
                        val viewModelFactory = remember(shoppingListViewModel) {
                            ListaComprasViewModelFactory(repository, userPreferencesManager, shoppingListPreferencesManager, shoppingListRepository, shoppingListViewModel, context)
                        }
                        val viewModel: ListaComprasViewModel = viewModel(factory = viewModelFactory)
                        val updateViewModel: UpdateViewModel = viewModel(factory = updateViewModelFactory)
                        
                        // Verificar atualizações automaticamente ao abrir o app
                        LaunchedEffect(Unit) {
                            delay(500) // Aguardar 500ms após abrir o app (reduzido para resposta mais rápida)
                            updateViewModel.checkForUpdate(showNotification = false, force = false)
                        }
                        
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
                                    shoppingListViewModel = shoppingListViewModel,
                                    updateViewModel = updateViewModel,
                                    initialShowDialog = shouldOpenAddDialog,
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
                                    },
                                    onNavigateToStatistics = {
                                        try {
                                            navController.navigate(Screen.Statistics.route)
                                        } catch (e: Exception) {
                                            android.util.Log.e("MainActivity", "Erro ao navegar", e)
                                        }
                                    }
                                )
                            }
                            composable(Screen.Settings.route) {
                                val notificationViewModel: com.example.minhascompras.ui.viewmodel.NotificationViewModel = viewModel(
                                    factory = notificationViewModelFactory
                                )
                                SettingsScreen(
                                    viewModel = viewModel,
                                    themeViewModel = themeViewModel,
                                    updateViewModel = updateViewModel,
                                    notificationViewModel = notificationViewModel,
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
                            composable(Screen.Statistics.route) {
                                val statisticsViewModel: StatisticsViewModel = viewModel(
                                    factory = statisticsViewModelFactory
                                )
                                StatisticsScreen(
                                    viewModel = statisticsViewModel,
                                    onNavigateBack = {
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