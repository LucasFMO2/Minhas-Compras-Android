package com.example.minhascompras

import android.app.Application
import android.util.Log
import java.lang.Thread.UncaughtExceptionHandler

class MinhasComprasApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Configurar handler global para exceções não capturadas
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Log.e("MinhasComprasApp", "Exceção não capturada no thread: ${thread.name}", exception)
            exception.printStackTrace()
            
            // Chamar o handler padrão para manter o comportamento normal
            defaultHandler?.uncaughtException(thread, exception)
        }
        
        Log.d("MinhasComprasApp", "Application inicializada com sucesso")
    }
}

