package com.example.minhascompras.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {
    companion object {
        private const val GITHUB_API_BASE_URL = "https://api.github.com/repos/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases"
        private const val DOWNLOAD_DIR = "updates"
        private const val MAX_RELEASES_TO_CHECK = 20
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            // Adicionar cache busting com timestamp para garantir verificação em tempo real
            val timestamp = System.currentTimeMillis()
            val url = URL("$GITHUB_API_BASE_URL?per_page=$MAX_RELEASES_TO_CHECK&_t=$timestamp")
            
            android.util.Log.d("UpdateManager", "Checking for updates from: $url")
            android.util.Log.d("UpdateManager", "Current versionCode: $currentVersionCode")
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
            connection.setRequestProperty("Pragma", "no-cache")
            connection.setRequestProperty("Expires", "0")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                android.util.Log.d("UpdateManager", "GitHub API Response received")
                
                // Decodificar lista de releases
                val releases = json.decodeFromString<List<GitHubRelease>>(response)
                android.util.Log.d("UpdateManager", "Found ${releases.size} releases")
                
                if (releases.isEmpty()) {
                    android.util.Log.w("UpdateManager", "No releases found")
                    return@withContext null
                }
                
                // Iterar sobre todas as releases e encontrar a mais recente válida
                var latestUpdateInfo: UpdateInfo? = null
                var latestVersionCode = currentVersionCode
                
                for (release in releases) {
                    android.util.Log.d("UpdateManager", "Checking release: ${release.tag_name}")
                    
                    val updateInfo = UpdateInfo.fromGitHubRelease(release, currentVersionCode)
                    
                    if (updateInfo != null && updateInfo.versionCode > latestVersionCode) {
                        android.util.Log.d("UpdateManager", "Found newer version: ${updateInfo.versionName} (code: ${updateInfo.versionCode})")
                        latestUpdateInfo = updateInfo
                        latestVersionCode = updateInfo.versionCode
                    }
                }
                
                // Log para debug
                android.util.Log.d("UpdateManager", "=== Update Check Result ===")
                android.util.Log.d("UpdateManager", "Current versionCode: $currentVersionCode")
                android.util.Log.d("UpdateManager", "Latest versionCode found: $latestVersionCode")
                android.util.Log.d("UpdateManager", "Update available: ${latestUpdateInfo != null}")
                if (latestUpdateInfo != null) {
                    android.util.Log.d("UpdateManager", "Update versionName: ${latestUpdateInfo.versionName}")
                    android.util.Log.d("UpdateManager", "Update fileName: ${latestUpdateInfo.fileName}")
                } else {
                    android.util.Log.d("UpdateManager", "No update available - already on latest version")
                }
                
                return@withContext latestUpdateInfo
            } else {
                android.util.Log.e("UpdateManager", "HTTP Error: ${connection.responseCode} - ${connection.responseMessage}")
            }
            null
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Error checking for update", e)
            e.printStackTrace()
            null
        }
    }
    
    suspend fun downloadUpdate(updateInfo: UpdateInfo, onProgress: (Int) -> Unit): File? = withContext(Dispatchers.IO) {
        try {
            val url = URL(updateInfo.downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            
            val totalSize = connection.contentLength
            val inputStream = connection.inputStream
            
            // Criar diretório de downloads
            val downloadDir = File(context.getExternalFilesDir(null), DOWNLOAD_DIR)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            
            val outputFile = File(downloadDir, updateInfo.fileName)
            val outputStream = FileOutputStream(outputFile)
            
            val buffer = ByteArray(8192)
            var downloaded = 0
            var bytesRead: Int
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloaded += bytesRead
                
                if (totalSize > 0) {
                    val progress = (downloaded * 100 / totalSize).toInt()
                    onProgress(progress)
                }
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
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

