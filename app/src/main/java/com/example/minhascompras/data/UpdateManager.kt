package com.example.minhascompras.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {
    companion object {
        private const val GITHUB_API_URL = "https://api.github.com/repos/nerddescoladofmo-cmyk/Minhas-Compras-Android/releases/latest"
        private const val DOWNLOAD_DIR = "updates"
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun checkForUpdate(currentVersionCode: Int): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
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
                Logger.e("UpdateManager", "HTTP Error: ${connection.responseCode} - ${connection.responseMessage}")
            }
            null
        } catch (e: Exception) {
            Logger.e("UpdateManager", "Error checking for update", e)
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

