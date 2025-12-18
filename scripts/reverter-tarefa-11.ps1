# =====================================================
# Script de Reversão da Tarefa 11: Múltiplas Listas (PowerShell)
# =====================================================
# AVISO IMPORTANTE: ESTE SCRIPT IRÁ REMOVER PERMANENTEMENTE TODOS OS DADOS
# Execute apenas se tiver certeza absoluta do que está fazendo!
# =====================================================

# Parâmetros de Configuração
param(
    [Parameter(Mandatory=$true, Help="Caminho do projeto Android")]
    [string]$ProjectPath</string>,
    
    [Parameter(Mandatory=$true, Help="Versão alvo da tarefa 11")]
    [string]$TargetVersion</string>,
    
    [Parameter(Mandatory=$false, Help="Forçar reversão completa (parcial ou total)")]
    [string]$Force</string>,
    
    [Parameter(Mandatory=$false, Help="Mostrar informações detalhadas")]
    [string]$Verbose</string>
)

# Função de Log
function Write-Log {
    param(
        [string]$Message</string>,
        [string]$Level</string> = "Information"
    )
    Write-Host -ForegroundColor Green
    Write-Host -ForegroundColor White
    Write-Output ""
}

# Função Principal
function Main {
    param(
        [string]$ProjectPath</string>,
        [string]$TargetVersion</string> = "4",
        [string]$Force</string> = $false,
        [string]$Verbose</string> = $false
    )
    
    # Validar parâmetros
    if (-not $ProjectPath) -or (-not $TargetVersion)) {
        Write-Log "Erro: Parâmetros obrigatórios não fornecidos!"
        Write-Log "Uso: .\scripts\reverter-tarefa-11.ps1 -ProjectPath <caminho_do_projeto> -TargetVersion 4 -Verbose"
        exit 1
    }
    
    Write-Log "Iniciando reversão da tarefa 11..."
    Write-Log "Projeto: $ProjectPath"
    Write-Log "Versão alvo: $TargetVersion"
    
    # ETAPA 1: REVERTER MIGRAÇÕES DO BANCO DE DADOS
    Write-Log "===================================================="
    Write-Log "ETAPA 1: Revertendo banco para versão 4..."
    
    # Verificar se o banco de dados existe
    $databasePath = Join-Path -ProjectPath $ProjectPath -ChildPath "compras_database.db"
    
    if (Test-Path $databasePath) {
        Write-Log "AVISO: Banco de dados encontrado em $databasePath"
        Write-Log "Fazendo backup do banco atual..."
        
        # Fazer backup do banco atual
        $backupPath = Join-Path -ProjectPath $ProjectPath -ChildPath "backup_compras_versao4_$(Get-Date -Format 'yyyyMMdd_HHmmss').db"
        Copy-Item -Path $databasePath -Destination $backupPath
        
        Write-Log "Backup criado em: $backupPath"
    } else {
        Write-Log "AVISO: Banco de dados não encontrado em $databasePath"
        Write-Log "Criando banco padrão para backup..."
        
        # Criar banco padrão para backup
        $defaultDatabasePath = Join-Path -ProjectPath $ProjectPath -ChildPath "default_compras_database.db"
        
        # Copiar estrutura do banco atual (se existir) para o banco padrão
        if (Test-Path $databasePath) {
            Copy-Item -Path $databasePath -Destination $defaultDatabasePath
            Write-Log "Estrutura copiada para banco padrão"
        }
        
        Write-Log "Banco padrão criado/preparado em: $defaultDatabasePath"
    }
    
    # ETAPA 2: REMOVER ENTIDADES CRIADAS
    Write-Log "===================================================="
    Write-Log "ETAPA 2: Removendo entidades e arquivos criados para múltiplas listas..."
    
    # Lista de arquivos a serem removidos
    $filesToRemove = @(
        "app/src/main/java/com/example/minhascompras/data/ShoppingList*.kt",
        "app/src/main/java/com/example/minhascompras/data/ShoppingListDao*.kt",
        "app/src/main/java/com/example/minhascompras/data/ShoppingListRepository*.kt",
        "app/src/main/java/com/example/minhascompras/data/ShoppingListViewModel*.kt",
        "app/src/main/java/com/example/minhascompras/data/ShoppingListPreferencesManager*.kt",
        "app/src/main/java/com/example/minhascompras/ui/screens/ListaComprasScreen.kt",
        "app/src/main/java/com/example/minhascompras/ui/screens/*List*.kt",
        "app/src/main/java/com/example/minhascompras/ui/viewmodel/*List*.kt",
        "app/src/main/java/com/example/minhascompras/ui/components/*List*.kt"
    )
    
    # Remover arquivos criados para múltiplas listas
    foreach ($file in $filesToRemove) {
        $fullPath = Join-Path -ProjectPath $ProjectPath -ChildPath $file
        
        Write-Log "Removendo: $fullPath"
        
        if (Test-Path $fullPath) {
            Remove-Item -Path $fullPath -Force -Recurse
            Write-Log "Arquivo removido: $file"
        } else {
            Write-Log "Arquivo não encontrado: $fullPath"
        }
    }
    
    # ETAPA 3: REVERTER MODIFICAÇÕES EM ARQUIVOS EXISTENTES
    Write-Log "===================================================="
    Write-Log "ETAPA 3: Revertendo modificações em arquivos existentes..."
    
    # Lista de arquivos a serem modificados
    $filesToRevert = @(
        "app/src/main/java/com/example/minhascompras/data/AppDatabase.kt",
        "app/src/main/java/com/example/minhascompras/data/ItemCompra.kt",
        "app/src/main/java/com/example/minhascompras/data/ItemCompraDao.kt",
        "app/src/main/java/com/example/minhascompras/data/ItemCompraRepository.kt",
        "app/src/main/java/com/example/minhascompras/ui/viewmodel/ListaComprasViewModel.kt",
        "app/src/main/java/com/example/minhascompras/ui/viewmodel/HistoryViewModel.kt",
        "app/src/main/java/com/example/minhascompras/ui/screens/ListaComprasScreen.kt"
    )
    
    # Modificar AppDatabase.kt
    Write-Log "Modificando AppDatabase.kt para remover referências a múltiplas listas..."
    
    $appDatabaseContent = Get-Content -Path $ProjectPath\app\src\main\java\com\example\minhascompras\data\AppDatabase.kt"
    
    # Remover entidades e migrações relacionadas a múltiplas listas
    $newAppDatabaseContent = $appDatabaseContent -replace `
    @Database(
        entities = [
            ItemCompra::class
        ],
        version = 4,
        exportSchema = false
    )
    
    # Remover referências a DAOs e Repositories
    $newAppDatabaseContent = $newAppDatabaseContent -replace `
import androidx.room.*`
    
    @Dao
    interface ItemCompraDao {
        @Query("SELECT * FROM itens_compra ORDER BY comprado ASC, dataCriacao DESC")
        fun getAllItens(): Flow<List<ItemCompra>>
        
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(item: ItemCompra): Long
        
        @Update
        suspend fun update(item: ItemCompra)
        
        @Delete
        suspend fun delete(item: ItemCompra)
        
        @Query("DELETE FROM itens_compra")
        suspend fun deleteAll()
    }
    `
    
    # Salvar o AppDatabase.kt modificado
    Set-Content -Path $ProjectPath\app\src\main\java\com\example\minhascompras\data\AppDatabase.kt -Value $newAppDatabaseContent
    
    Write-Log "AppDatabase.kt atualizado para versão 4"
    
    # ETAPA 4: REVERTER MODIFICAÇÕES EM DAOs E REPOSITORIES
    Write-Log "===================================================="
    Write-Log "ETAPA 4: Revertendo modificações em DAOs e Repositories..."
    
    # Modificar ItemCompraDao.kt
    Write-Log "Modificando ItemCompraDao.kt para remover queries com listId..."
    
    $itemCompraDaoContent = Get-Content -Path $ProjectPath\app\src\main\java\com\example\minhascompras\data\ItemCompraDao.kt`
    
    $newItemCompraDaoContent = $itemCompraDaoContent -replace `
import androidx.room.*`
    
    @Dao
    interface ItemCompraDao {
        @Query("SELECT * FROM itens_compra ORDER BY dataCriacao DESC")
        fun getAllItens(): Flow<List<ItemCompra>>
        
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(item: ItemCompra): Long
        
        @Update
        suspend fun update(item: ItemCompra)
        
        @Delete
        suspend fun delete(item: ItemCompra)
        
        @Query("DELETE FROM itens_compra")
        suspend fun deleteAll()
    }
    `
    
    # Salvar o ItemCompraDao.kt modificado
    Set-Content -Path $ProjectPath\app\src\main\java\com\example\minhascompras\data\ItemCompraDao.kt -Value $newItemCompraDaoContent
    
    Write-Log "ItemCompraDao.kt atualizado para remover queries com listId"
    
    # ETAPA 5: REVERTER MODIFICAÇÕES EM VIEWMODELS E REPOSITORIES
    Write-Log "===================================================="
    Write-Log "ETAPA 5: Revertendo modificações em ViewModels e UI Components..."
    
    # Modificar ViewModels relacionados a múltiplas listas
    $viewModelFiles = @(
        "app/src/main/java/com/example/minhascompras/ui/viewmodel/ListaComprasViewModel.kt",
        "app/src/main/java/com/example/minhascompras/ui/viewmodel/HistoryViewModel.kt"
    )
    
    foreach ($viewModelFile in $viewModelFiles) {
        Write-Log "Modificando: $viewModelFile"
        
        $viewModelContent = Get-Content -Path $viewModelFile
        
        $newViewModelContent = $viewModelContent -replace `
package com.example.minhascompras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModel.compose.viewModel
import com.example.minhascompras.data.ItemCompraRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ListaComprasViewModel(
    private val repository: ItemCompraRepository,
    private val _searchQuery = MutableStateFlow("")
    private val _filterStatus = MutableStateFlow("Todos")
    private val _sortOrder = MutableStateFlow("DATA_CRIACAO_DESC")
    
    val allItens = repository.getAllItens()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Remover dependências de múltiplas listas
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    
    fun onFilterStatusChanged(status: String) {
        _filterStatus.value = status
    }
    
    fun onSortOrderChanged(order: String) {
        _sortOrder.value = order
    }
    
    fun insertItem(item: ItemCompra) {
        viewModelScope.launch {
            isLoading.value = true
            repository.insert(item)
                .onSuccess { isLoading.value = false }
                .onFailure { exception ->
                    errorMessage.value = "Erro ao inserir item: ${exception.message}"
                }
        }
    }
    
    fun updateItem(item: ItemCompra) {
        viewModelScope.launch {
            repository.update(item)
        }
    }
    
    fun deleteItem(item: ItemCompra) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }
    
    fun deleteComprados() {
        viewModelScope.launch {
            repository.deleteComprados()
        }
    }
    
    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
    
    fun getAllItensForExport(): List<ItemCompra> {
        return repository.getAllItens()
    }
    
    fun getShareableText(): String {
        return repository.getShareableText()
    }
}
`
    
    # Salvar o ViewModel modificado
    Set-Content -Path $viewModelFile -Value $newViewModelContent
    
    Write-Log "ViewModels atualizados para remover dependências de múltiplas listas"
    
    # ETAPA 6: REVERTER MODIFICAÇÕES EM UI COMPONENTS
    Write-Log "===================================================="
    Write-Log "ETAPA 6: Revertendo modificações em UI Components..."
    
    # Modificar ListaComprasScreen.kt
    Write-Log "Modificando ListaComprasScreen.kt para remover componentes de múltiplas listas..."
    
    $screenContent = Get-Content -Path $ProjectPath\app\src\main\java\com\example\minhascompras\ui\screens\ListaComprasScreen.kt`
    
    $newScreenContent = $screenContent -replace `
package com.example.minhascompras.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext

import com.example.minhascompras.ui.viewmodel.ListaComprasViewModel
import com.example.minhascompras.ui.viewmodel.HistoryViewModel

sealed class Screen(val route: String) {
    object ListaCompras : Screen("lista_compras")
    object Settings : Screen("settings")
    object History : Screen("history")
}

@Composable
fun ListaComprasScreen(
    viewModel: ListaComprasViewModel = viewModel(factory = ListaComprasViewModelFactory),
    updateViewModel: UpdateViewModel = viewModel(factory = UpdateViewModelFactory),
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
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()
        
        // Removendo componentes de múltiplas listas
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Apenas conteúdo existente antes da implementação da tarefa 11
            Text(
                text = "Minhas Compras",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            
            // Botão flutuante de adicionar item (existente antes da tarefa 11)
            FloatingActionButton(
                onClick = {
                    try {
                        navController.navigate(Screen.Settings.route)
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Erro ao navegar", e)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
        
        NavHost(
            navController = navController,
            startDestination = Screen.ListaCompras.route
        ) {
            composable(Screen.ListaCompras.route) {
                ListaComprasScreen(
                    viewModel = viewModel,
                    updateViewModel = updateViewModel,
                    onNavigateToSettings = {
                        try {
                            navController.popBackStack()
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Ir ao voltar", e)
                        }
                    },
                    onNavigateToHistory = {
                        try {
                            navController.popBackStack()
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Ir ao voltar", e)
                        }
                    }
                )
            }
        )
    }
}
`
    
    # Salvar a tela modificada
    Set-Content -Path $ProjectPath\app\src\main\java\com\example\minhascompras\ui\screens\ListaComprasScreen.kt -Value $newScreenContent
    
    Write-Log "ListaComprasScreen.kt atualizado para remover componentes de múltiplas listas"
    
    # ETAPA 7: LIMPAR PREFERÊNCIAS E DADOS RELACIONADOS
    Write-Log "===================================================="
    Write-Log "ETAPA 7: Removendo preferências e dados relacionados..."
    
    # Remover arquivos de preferências
    $preferenceFiles = @(
        "$ProjectPath/app/src/main/java/com/example/minhascompras/data/ShoppingListPreferencesManager.kt",
        "$ProjectPath/app/src/main/java/com/example/minhascompras/data/UserPreferencesManager.kt",
        "$ProjectPath/app/src/main/java/com/example/minhascompras/data/ThemePreferencesManager.kt"
    )
    
    foreach ($preferenceFile in $preferenceFiles) {
        Write-Log "Removendo: $preferenceFile"
        
        if (Test-Path $preferenceFile) {
            Remove-Item -Path $preferenceFile -Force
        } else {
            Write-Log "Arquivo não encontrado: $preferenceFile"
        }
    }
    
    Write-Log "Preferências e dados relacionados removidos"
    
    # ETAPA 8: RECOMPILAR E TESTAR APLICAÇÃO
    Write-Log "===================================================="
    Write-Log "ETAPA 8: Recompilando aplicação..."
    
    # Recompilar (em modo Debug)
    & $compileCommand = "./gradlew assembleDebug"
    
    if ($Verbose) {
        Write-Log "Executando: $compileCommand"
    } else {
        Write-Log "Executando: $compileCommand"
    }
    
    Write-Log "Aguardando saída do comando..."
    
    $compileResult = Invoke-Expression -Command $compileCommand 2>&1 | Out-String -ErrorAction Stop
    
    if ($compileResult.ExitCode -ne 0) {
        Write-Log "✅ Aplicação recompilada com sucesso!"
        Write-Log "Saída do comando:"
        Write-Log $compileResult
    } else {
        Write-Log "❌ Falha na compilação! Código de saída: $($compileResult.ExitCode)"
        Write-Log "Erros:"
        Write-Log $compileResult.Error
        exit 1
    }
    
    Write-Log "===================================================="
    Write-Log "Processo de reversão concluído com sucesso!"
    Write-Log "Resumo das operações:"
    Write-Log "- Migrações revertidas: MIGRATION_4_5, MIGRATION_5_6"
    Write-Log "- Arquivos removidos: $($filesToRemove.Count) arquivos"
    Write-Log "- Arquivos modificados: $($filesToRevert.Count) arquivos"
    Write-Log "- Componentes de UI removidos: 1 tela (ListaComprasScreen.kt)"
    Write-Log "- Preferências removidas: $($preferenceFiles.Count) arquivos"
    Write-Log "- Total de arquivos processados: $($filesToRemove.Count + $filesToRevert.Count + $preferenceFiles.Count)"
    
    if ($Force) {
        Write-Log "⚠️ AVISO: Modo força ativado. As alterações serão aplicadas sem confirmação."
    }
    
    Write-Log "Para executar o script:"
    Write-Log ".\scripts\reverter-tarefa-11.ps1 -ProjectPath <caminho_do_projeto> -TargetVersion 4"
    Write-Log ".\scripts\reverter-tarefa-11.ps1 -ProjectPath <caminho_do_projeto> -TargetVersion 4 -Verbose"
    Write-Log ""
}

# Função para confirmar ação
function Confirm-Action {
    param(
        [string]$Message</string>,
        [string]$Title</string> = "Confirmação Necessária",
        [string]$Yes</string> = "Sim",
        [string]$No</string> = "Não"
    )
    
    $choice = Read-Host -Prompt $Message -Title $Title -Yes $No
    
    if ($choice -eq 0) {
        Write-Log "❌ Operação cancelada pelo usuário."
        exit 1
    }
    
    return $choice -eq 1
}

# Execução principal
try {
    # Confirmação inicial
    $confirmado = Confirm-Action -Message "Deseja reverter completamente a tarefa 11 (RF-010: Múltiplas Listas) e todas as suas subtarefas?" -Title "Confirmação de Reversão Completa" -Yes "Sim" -No "Não"
    
    if (-not $confirmado) {
        Write-Log "❌ Operação cancelada pelo usuário."
        exit 1
    }
    
    # Executar reversão se confirmado
    if ($confirmado) {
        # ETAPA 1
        & $Main -ProjectPath $ProjectPath -TargetVersion $TargetVersion -Verbose
        
        # ETAPA 2
        & $Main -ProjectPath $ProjectPath -TargetVersion $TargetVersion -Verbose
        
        # ETAPA 3
        & $Main -ProjectPath $ProjectPath -TargetVersion $TargetVersion -Verbose
        
        # ETAPA 4
        & $Main -ProjectPath $ProjectPath -TargetVersion $TargetVersion -Verbose
        
        # ETAPA 5
        & $Main -ProjectPath $ProjectPath -TargetVersion $TargetVersion -Verbose
        
        # ETAPA 6
        & $Main -ProjectPath $ProjectPath -TargetVersion $TargetVersion -Verbose
        
        # ETAPA 7
        & $Main -ProjectPath $ProjectPath -TargetVersion $TargetVersion -Verbose
        
        # ETAPA 8
        & $Main -ProjectPath $ProjectPath -TargetVersion $TargetVersion -Verbose
        
        Write-Log "Reversão da tarefa 11 concluída com sucesso!"
    } else {
        Write-Log "Reversão cancelada."
        exit 1
    }
    
} catch {
    Write-Log "ERRO FATAL durante execução: $_"
    Write-Log "Stack Trace: $($_"
    exit 1
}