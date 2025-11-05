package com.example.minhascompras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minhascompras.data.AppDatabase
import com.example.minhascompras.data.ItemCompraRepository
import com.example.minhascompras.ui.screens.ListaComprasScreen
import com.example.minhascompras.ui.theme.MinhasComprasTheme
import com.example.minhascompras.ui.viewmodel.ListaComprasViewModel
import com.example.minhascompras.ui.viewmodel.ListaComprasViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ItemCompraRepository(database.itemCompraDao())
        val viewModelFactory = ListaComprasViewModelFactory(repository)
        
        setContent {
            MinhasComprasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ListaComprasViewModel = viewModel(factory = viewModelFactory)
                    ListaComprasScreen(viewModel = viewModel)
                }
            }
        }
    }
}