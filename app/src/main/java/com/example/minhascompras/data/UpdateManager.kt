package com.example.minhascompras.data

import android.content.Context
import android.content.Intent
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
        private const val GITHUB_API_URL = "https://api.github.com/repos/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/latest"
        private const val DOWNLOAD_DIR = "updates"
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 2000L
        private const val CONNECT_TIMEOUT_MS = 15000
        private const val READ_TIMEOUT_MS = 30000
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    private val isDownloadCancelled = AtomicBoolean(false)
    
    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        // Retry automático em caso de falha de rede
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
                    
                    val updateInfo = UpdateInfo.fromGitHubRelease(release, currentVersionCode)
                    
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
                    delay(RETRY_DELAY_MS)
                }
            } catch (e: java.net.UnknownHostException) {
                lastException = e
                Logger.e("UpdateManager", "Network error (attempt ${attempt + 1}/$MAX_RETRIES)", e)
                if (attempt < MAX_RETRIES - 1) {
                    delay(RETRY_DELAY_MS)
                }
            } catch (e: Exception) {
                lastException = e
                Logger.e("UpdateManager", "Error checking for update (attempt ${attempt + 1}/$MAX_RETRIES)", e)
                if (attempt < MAX_RETRIES - 1) {
                    delay(RETRY_DELAY_MS)
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
    
    suspend fun downloadUpdate(updateInfo: UpdateInfo, onProgress: (Int) -> Unit): File? = withContext(Dispatchers.IO) {
        isDownloadCancelled.set(false)
        
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

