package com.example.minhascompras

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.minhascompras.data.ShoppingListPreferencesManager
import com.example.minhascompras.data.ShoppingListRepository
import com.example.minhascompras.data.UpdatePreferencesManager
import com.example.minhascompras.data.ThemeMode
import com.example.minhascompras.data.ThemePreferencesManager
import com.example.minhascompras.data.UserPreferencesManager
import com.example.minhascompras.data.NotificationPreferencesManager
import com.example.minhascompras.notifications.NotificationScheduler
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
import com.example.minhascompras.utils.DebugLogger
import com.example.minhascompras.ui.viewmodel.ThemeViewModel
import com.example.minhascompras.ui.viewmodel.ThemeViewModelFactory
import com.example.minhascompras.ui.viewmodel.UpdateViewModel
import com.example.minhascompras.ui.viewmodel.UpdateViewModelFactory
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

sealed class Screen(val route: String) {
    object ListaCompras : Screen("lista_compras")
    object Settings : Screen("settings")
    object History : Screen("history")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar DebugLogger
        DebugLogger.init(applicationContext)
        
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
            ItemCompraRepository(database.itemCompraDao(), database.historyDao(), database.shoppingListDao())
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
        
        val shoppingListRepository = try {
            ShoppingListRepository(database.shoppingListDao())
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao criar ShoppingListRepository", e)
            throw RuntimeException("Erro crítico: não foi possível criar o ShoppingListRepository", e)
        }
        
        val shoppingListPreferencesManager = try {
            ShoppingListPreferencesManager(applicationContext)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Erro ao criar ShoppingListPreferencesManager", e)
            throw RuntimeException("Erro crítico: não foi possível inicializar preferências de listas", e)
        }
        
        val viewModelFactory = ListaComprasViewModelFactory(repository, userPreferencesManager, shoppingListPreferencesManager, shoppingListRepository, application)
        val shoppingListViewModelFactory = ShoppingListViewModelFactory(shoppingListRepository, shoppingListPreferencesManager)
        val historyViewModelFactory = HistoryViewModelFactory(repository, shoppingListPreferencesManager, shoppingListRepository)
        val updateViewModelFactory = UpdateViewModelFactory(applicationContext)
        
        // Rastrear uso do app
        lifecycleScope.launch {
            try {
                val updatePreferencesManager = UpdatePreferencesManager(applicationContext)
                updatePreferencesManager.updateLastAppUse()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Erro ao atualizar timestamp de uso", e)
            }
        }
        
        // Inicializar workers de notificação
        lifecycleScope.launch {
            try {
                val notificationPrefsManager = NotificationPreferencesManager(applicationContext)
                
                // Agendar lembrete diário se habilitado
                val dailyReminderEnabled = notificationPrefsManager.isDailyReminderEnabled().first()
                if (dailyReminderEnabled) {
                    val hour = notificationPrefsManager.getDailyReminderHour().first()
                    val minute = notificationPrefsManager.getDailyReminderMinute().first()
                    NotificationScheduler.scheduleDailyReminder(applicationContext, hour, minute, true)
                }
                
                // Agendar verificação de itens pendentes se habilitado
                val pendingItemsEnabled = notificationPrefsManager.isPendingItemsNotificationEnabled().first()
                NotificationScheduler.schedulePendingItemsCheck(applicationContext, pendingItemsEnabled)
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Erro ao inicializar workers de notificação", e)
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
                        // #region agent log
                        com.example.minhascompras.utils.DebugLogger.log(
                            location = "MainActivity.kt:151",
                            message = "Creating ViewModels",
                            data = mapOf("step" to "before"),
                            hypothesisId = "D"
                        )
                        // #endregion
                        val viewModel: ListaComprasViewModel = viewModel(factory = viewModelFactory)
                        val shoppingListViewModel: ShoppingListViewModel = viewModel(factory = shoppingListViewModelFactory)
                        val updateViewModel: UpdateViewModel = viewModel(factory = updateViewModelFactory)
                        // #region agent log
                        com.example.minhascompras.utils.DebugLogger.log(
                            location = "MainActivity.kt:157",
                            message = "ViewModels created successfully",
                            data = mapOf(
                                "shoppingListViewModelNotNull" to (shoppingListViewModel != null)
                            ),
                            hypothesisId = "D"
                        )
                        // #endregion
                        
                        // Solicitar permissão de notificação para Android 13+ (API 33+)
                        val context = LocalContext.current
                        val notificationPermissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            if (isGranted) {
                                android.util.Log.d("MainActivity", "Permissão de notificação concedida")
                            } else {
                                android.util.Log.d("MainActivity", "Permissão de notificação negada")
                            }
                        }
                        
                        // Solicitar permissão automaticamente se necessário (Android 13+)
                        LaunchedEffect(Unit) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                when {
                                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                                        android.util.Log.d("MainActivity", "Permissão de notificação já concedida")
                                    }
                                    else -> {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            }
                        }
                        
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