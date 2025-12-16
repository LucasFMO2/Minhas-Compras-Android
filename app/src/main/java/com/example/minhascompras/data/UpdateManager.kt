package com.example.minhascompras.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

class UpdateManager(private val context: Context) {
    companion object {
        private const val GITHUB_API_URL = "https://api.github.com/repos/Lucasfmo1/Minhas-Compras-Android/releases/latest"
        private const val DOWNLOAD_DIR = "updates"
        private const val MAX_RETRIES = 3
        private const val INITIAL_RETRY_DELAY_MS = 1000L // Delay inicial de 1 segundo
        private const val MAX_RETRY_DELAY_MS = 10000L // Delay máximo de 10 segundos
        private const val CONNECT_TIMEOUT_MS = 15000
        private const val READ_TIMEOUT_MS = 30000
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    private val isDownloadCancelled = AtomicBoolean(false)
    
    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        val updateManager = this@UpdateManager
        
        // Retry automático com backoff exponencial em caso de falha de rede
        repeat(MAX_RETRIES) { attempt ->
            var connection: HttpURLConnection? = null
            try {
                val url = URL(GITHUB_API_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.setRequestProperty("User-Agent", "MinhasCompras-Android")
                connection.connectTimeout = CONNECT_TIMEOUT_MS
                connection.readTimeout = READ_TIMEOUT_MS
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Logger.d("UpdateManager", "GitHub API Response received")
                    
                    val release = json.decodeFromString<GitHubRelease>(response)
                    Logger.d("UpdateManager", "Release parsed: tag=${release.tag_name}, assets=${release.assets.size}")
                    
                    val updateInfo = UpdateInfo.fromGitHubRelease(release, currentVersionCode, updateManager)
                    
                    Logger.d("UpdateManager", "=== Update Check Result ===")
                    Logger.d("UpdateManager", "Current versionCode: $currentVersionCode")
                    Logger.d("UpdateManager", "Release tag: ${release.tag_name}")
                    Logger.d("UpdateManager", "Extracted versionCode: ${updateInfo?.versionCode}")
                    Logger.d("UpdateManager", "Update available: ${updateInfo != null}")
                    if (updateInfo != null) {
                        Logger.d("UpdateManager", "Update versionName: ${updateInfo.versionName}")
                        Logger.d("UpdateManager", "Update fileName: ${updateInfo.fileName}")
                    }
                    
                    return@withContext updateInfo
                } else {
                    val errorMsg = "HTTP Error: ${connection.responseCode} - ${connection.responseMessage}"
                    Logger.e("UpdateManager", errorMsg)
                    lastException = Exception(errorMsg)
                }
            } catch (e: java.net.SocketTimeoutException) {
                lastException = e
                Logger.e("UpdateManager", "Timeout error (attempt ${attempt + 1}/$MAX_RETRIES)", e)
                if (attempt < MAX_RETRIES - 1) {
                    // Backoff exponencial: 1s, 2s, 4s (limitado a MAX_RETRY_DELAY_MS)
                    val retryDelay = minOf(INITIAL_RETRY_DELAY_MS * (1 shl attempt), MAX_RETRY_DELAY_MS)
                    Logger.d("UpdateManager", "Retrying in ${retryDelay}ms...")
                    delay(retryDelay)
                }
            } catch (e: java.net.UnknownHostException) {
                lastException = e
                Logger.e("UpdateManager", "Network error (attempt ${attempt + 1}/$MAX_RETRIES)", e)
                if (attempt < MAX_RETRIES - 1) {
                    // Backoff exponencial: 1s, 2s, 4s (limitado a MAX_RETRY_DELAY_MS)
                    val retryDelay = minOf(INITIAL_RETRY_DELAY_MS * (1 shl attempt), MAX_RETRY_DELAY_MS)
                    Logger.d("UpdateManager", "Retrying in ${retryDelay}ms...")
                    delay(retryDelay)
                }
            } catch (e: Exception) {
                lastException = e
                Logger.e("UpdateManager", "Error checking for update (attempt ${attempt + 1}/$MAX_RETRIES)", e)
                if (attempt < MAX_RETRIES - 1) {
                    // Backoff exponencial: 1s, 2s, 4s (limitado a MAX_RETRY_DELAY_MS)
                    val retryDelay = minOf(INITIAL_RETRY_DELAY_MS * (1 shl attempt), MAX_RETRY_DELAY_MS)
                    Logger.d("UpdateManager", "Retrying in ${retryDelay}ms...")
                    delay(retryDelay)
                }
            } finally {
                connection?.disconnect()
            }
        }
        
        // Log da última exceção se todas as tentativas falharam
        lastException?.let { exception ->
            Logger.e("UpdateManager", "All retry attempts failed. Last error: ${exception.message}", exception)
        }
        
        null
    }
    
    fun cancelDownload() {
        isDownloadCancelled.set(true)
    }
    
    /**
     * Extrai o versionCode diretamente do APK baixando temporariamente o arquivo.
     * Usa PackageManager para ler o versionCode sem instalar o APK.
     * 
     * @param apkUrl URL do APK para download
     * @return versionCode do APK ou 0 se houver erro
     */
    suspend fun extractVersionCodeFromApk(apkUrl: String): Int = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        var connection: HttpURLConnection? = null
        var inputStream: java.io.InputStream? = null
        var outputStream: FileOutputStream? = null
        
        try {
            Logger.d("UpdateManager", "Extracting versionCode from APK: $apkUrl")
            
            // Criar arquivo temporário
            val tempDir = File(context.cacheDir, "temp_apk_check")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            tempFile = File(tempDir, "temp_check.apk")
            
            // Se já existe, deletar
            if (tempFile.exists()) {
                tempFile.delete()
            }
            
            // Fazer download do APK
            val url = URL(apkUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "MinhasCompras-Android")
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.connect()
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Logger.e("UpdateManager", "Failed to download APK for versionCode check: ${connection.responseCode}")
                return@withContext 0
            }
            
            inputStream = connection.inputStream
            outputStream = FileOutputStream(tempFile)
            
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0L
            val totalSize = connection.contentLength
            
            // Limitar download a 50MB para verificação (APKs geralmente são menores)
            val maxSizeForCheck = 50 * 1024 * 1024L // 50MB
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (totalRead > maxSizeForCheck) {
                    Logger.w("UpdateManager", "APK too large for versionCode check, stopping download")
                    break
                }
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            outputStream = null
            inputStream = null
            
            // Verificar se o arquivo foi baixado (pelo menos parcialmente)
            if (!tempFile.exists() || tempFile.length() == 0L) {
                Logger.e("UpdateManager", "Failed to download APK for versionCode check")
                return@withContext 0
            }
            
            Logger.d("UpdateManager", "APK downloaded for check: ${tempFile.length()} bytes")
            
            // Usar PackageManager para ler o versionCode do APK
            val packageManager = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(
                    tempFile.absolutePath,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(tempFile.absolutePath, 0)
            }
            
            if (packageInfo == null) {
                Logger.e("UpdateManager", "Failed to read package info from APK")
                return@withContext 0
            }
            
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
            
            Logger.d("UpdateManager", "Extracted versionCode from APK: $versionCode")
            Logger.d("UpdateManager", "APK versionName: ${packageInfo.versionName}")
            
            versionCode
        } catch (e: Exception) {
            Logger.e("UpdateManager", "Error extracting versionCode from APK", e)
            0
        } finally {
            // Limpar recursos
            try {
                outputStream?.close()
                inputStream?.close()
                connection?.disconnect()
                
                // Deletar arquivo temporário
                tempFile?.delete()
            } catch (e: Exception) {
                Logger.e("UpdateManager", "Error cleaning up temp APK file", e)
            }
        }
    }
    
    /**
     * Extrai o versionCode do APK fazendo download parcial (apenas os primeiros MB).
     * Mais rápido que download completo, mas ainda requer download de parte do arquivo.
     * 
     * @param apkUrl URL do APK para download
     * @return versionCode do APK ou 0 se houver erro
     */
    suspend fun extractVersionCodeFromApkPartial(apkUrl: String): Int = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        var connection: HttpURLConnection? = null
        var inputStream: java.io.InputStream? = null
        var outputStream: FileOutputStream? = null
        
        try {
            Logger.d("UpdateManager", "Extracting versionCode from APK (partial): $apkUrl")
            
            // Criar arquivo temporário
            val tempDir = File(context.cacheDir, "temp_apk_check")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            tempFile = File(tempDir, "temp_check_partial.apk")
            
            // Se já existe, deletar
            if (tempFile.exists()) {
                tempFile.delete()
            }
            
            // Fazer download parcial do APK (apenas primeiros 5MB - suficiente para metadados)
            val url = URL(apkUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "MinhasCompras-Android")
            connection.setRequestProperty("Range", "bytes=0-5242880") // Primeiros 5MB
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.connect()
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                Logger.e("UpdateManager", "Failed to download APK partially: $responseCode")
                return@withContext 0
            }
            
            inputStream = connection.inputStream
            outputStream = FileOutputStream(tempFile)
            
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalRead = 0L
            val maxPartialSize = 5 * 1024 * 1024L // 5MB máximo para download parcial
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1 && totalRead < maxPartialSize) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            outputStream = null
            inputStream = null
            
            // Verificar se o arquivo foi baixado (pelo menos parcialmente)
            if (!tempFile.exists() || tempFile.length() == 0L) {
                Logger.e("UpdateManager", "Failed to download APK partially for versionCode check")
                return@withContext 0
            }
            
            Logger.d("UpdateManager", "APK partially downloaded for check: ${tempFile.length()} bytes")
            
            // Tentar usar PackageManager para ler o versionCode do APK parcial
            // Nota: Pode não funcionar se o APK estiver muito truncado, mas geralmente funciona
            val packageManager = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(
                    tempFile.absolutePath,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(tempFile.absolutePath, 0)
            }
            
            if (packageInfo == null) {
                Logger.d("UpdateManager", "Failed to read package info from partial APK, will try full download")
                return@withContext 0
            }
            
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
            
            Logger.d("UpdateManager", "Extracted versionCode from partial APK: $versionCode")
            Logger.d("UpdateManager", "APK versionName: ${packageInfo.versionName}")
            
            versionCode
        } catch (e: Exception) {
            Logger.e("UpdateManager", "Error extracting versionCode from partial APK", e)
            0
        } finally {
            // Limpar recursos
            try {
                outputStream?.close()
                inputStream?.close()
                connection?.disconnect()
                
                // Deletar arquivo temporário
                tempFile?.delete()
            } catch (e: Exception) {
                Logger.e("UpdateManager", "Error cleaning up temp partial APK file", e)
            }
        }
    }
    
    private fun cleanupOldDownloads() {
        try {
            val downloadDir = File(context.getExternalFilesDir(null), DOWNLOAD_DIR)
            if (downloadDir.exists() && downloadDir.isDirectory) {
                downloadDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.name.endsWith(".apk", ignoreCase = true)) {
                        val fileAge = System.currentTimeMillis() - file.lastModified()
                        // Deletar arquivos com mais de 7 dias
                        if (fileAge > 7 * 24 * 60 * 60 * 1000L) {
                            file.delete()
                            Logger.d("UpdateManager", "Deleted old APK: ${file.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("UpdateManager", "Error cleaning up old downloads", e)
        }
    }
    
    private fun hasEnoughSpace(requiredBytes: Long): Boolean {
        return try {
            val downloadDir = File(context.getExternalFilesDir(null), DOWNLOAD_DIR)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val stat = android.os.StatFs(downloadDir.absolutePath)
            val availableBytes = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                stat.availableBytes
            } else {
                @Suppress("DEPRECATION")
                stat.availableBlocks.toLong() * stat.blockSize
            }
            availableBytes >= requiredBytes * 1.2 // 20% de margem
        } catch (e: Exception) {
            Logger.e("UpdateManager", "Error checking available space", e)
            true // Assumir que há espaço se não conseguir verificar
        }
    }
    
    suspend fun downloadUpdate(updateInfo: UpdateInfo, currentVersionCode: Int, onProgress: (Int) -> Unit): File? = withContext(Dispatchers.IO) {
        isDownloadCancelled.set(false)
        
        // Validação adicional: verificar se a versão é maior que a atual
        if (updateInfo.versionCode <= currentVersionCode) {
            Logger.e("UpdateManager", "Tentativa de baixar versão igual ou inferior. Atual: $currentVersionCode, Tentativa: ${updateInfo.versionCode}")
            return@withContext null
        }
        
        // Limpar downloads antigos antes de baixar
        cleanupOldDownloads()
        
        var connection: HttpURLConnection? = null
        var inputStream: java.io.InputStream? = null
        var outputStream: FileOutputStream? = null
        var outputFile: File? = null
        
        try {
            val url = URL(updateInfo.downloadUrl)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "MinhasCompras-Android")
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.connect()
            
            val totalSize = connection.contentLength
            // Aceitar totalSize -1 (tamanho desconhecido) ou > 0
            if (totalSize == 0) {
                Logger.e("UpdateManager", "Invalid file size: $totalSize")
                return@withContext null
            }
            
            // Verificar espaço disponível apenas se o tamanho for conhecido
            if (totalSize > 0 && !hasEnoughSpace(totalSize.toLong())) {
                Logger.e("UpdateManager", "Not enough space available. Required: $totalSize bytes")
                return@withContext null
            }
            
            inputStream = connection.inputStream
            
            // Criar diretório de downloads
            val downloadDir = File(context.getExternalFilesDir(null), DOWNLOAD_DIR)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            
            // Deletar APK anterior se existir
            outputFile = File(downloadDir, updateInfo.fileName)
            if (outputFile.exists()) {
                outputFile.delete()
            }
            
            outputStream = FileOutputStream(outputFile)
            
            val buffer = ByteArray(8192 * 4) // Buffer maior para melhor performance
            var downloaded = 0
            var bytesRead: Int
            var lastProgressUpdate = 0
            
            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    if (isDownloadCancelled.get()) {
                        Logger.d("UpdateManager", "Download cancelled by user")
                        outputStream.close()
                        inputStream.close()
                        outputFile.delete()
                        return@withContext null
                    }
                    
                    outputStream.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    
                    if (totalSize > 0) {
                        val progress = (downloaded * 100 / totalSize).toInt()
                        // Atualizar progresso apenas a cada 1% para evitar muitas atualizações
                        if (progress > lastProgressUpdate) {
                            onProgress(progress)
                            lastProgressUpdate = progress
                        }
                    }
                }
                
                outputStream.flush()
                outputStream.close()
                inputStream.close()
                outputStream = null
                inputStream = null
                
                // Validar que o arquivo foi baixado completamente apenas se o tamanho for conhecido
                if (totalSize > 0 && outputFile.length() != totalSize.toLong()) {
                    Logger.e("UpdateManager", "Download incomplete. Expected: $totalSize, Got: ${outputFile.length()}")
                    outputFile.delete()
                    return@withContext null
                }
                
                Logger.d("UpdateManager", "Download completed successfully. Size: ${outputFile.length()} bytes")
                outputFile
            } catch (e: Exception) {
                outputStream?.close()
                inputStream?.close()
                outputFile?.delete()
                throw e
            }
        } catch (e: Exception) {
            Logger.e("UpdateManager", "Error downloading update", e)
            outputStream?.close()
            inputStream?.close()
            outputFile?.delete()
            null
        } finally {
            connection?.disconnect()
        }
    }
    
    fun installApk(apkFile: File) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }
            
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(intent)
    }
}

