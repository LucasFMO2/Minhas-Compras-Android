package com.example.minhascompras.data

import com.example.minhascompras.utils.Logger
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val assets: List<GitHubAsset>
)

@Serializable
data class GitHubAsset(
    val name: String,
    val browser_download_url: String,
    val size: Long
)

data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
    val fileName: String,
    val fileSize: Long = 0L
) {
    companion object {
        /**
         * Cria um UpdateInfo a partir de um GitHubRelease, extraindo o versionCode diretamente do APK.
         * 
         * @param release Release do GitHub
         * @param currentVersionCode versionCode da versão atual instalada
         * @param updateManager UpdateManager para extrair versionCode do APK
         * @return UpdateInfo se houver atualização disponível, null caso contrário
         */
        suspend fun fromGitHubRelease(
            release: GitHubRelease, 
            currentVersionCode: Int,
            updateManager: UpdateManager
        ): UpdateInfo? {
            Logger.d("UpdateInfo", "=== Update Check ===")
            Logger.d("UpdateInfo", "Release tag: ${release.tag_name}")
            Logger.d("UpdateInfo", "Current versionCode: $currentVersionCode")
            
            // Encontrar o asset do APK primeiro
            val apkAsset = release.assets.firstOrNull { 
                it.name.endsWith(".apk", ignoreCase = true) 
            }
            
            if (apkAsset == null) {
                Logger.e("UpdateInfo", "No APK asset found in release. Assets: ${release.assets.map { it.name }}")
                return null
            }
            
            Logger.d("UpdateInfo", "APK asset found: ${apkAsset.name}")
            Logger.d("UpdateInfo", "Download URL: ${apkAsset.browser_download_url}")
            
            // Extrair versionCode diretamente do APK
            Logger.d("UpdateInfo", "Extracting versionCode from APK...")
            val versionCode = updateManager.extractVersionCodeFromApk(apkAsset.browser_download_url)
            
            Logger.d("UpdateInfo", "Extracted versionCode from APK: $versionCode")
            Logger.d("UpdateInfo", "Comparison: $versionCode > $currentVersionCode = ${versionCode > currentVersionCode}")
            
            // Validação: versionCode deve ser maior que 0
            if (versionCode <= 0) {
                Logger.e("UpdateInfo", "Invalid versionCode extracted from APK: $versionCode")
                return null
            }
            
            // Comparação: se o versionCode extraído for maior que o atual, há atualização
            if (versionCode <= currentVersionCode) {
                Logger.d("UpdateInfo", "No update available - versionCode $versionCode <= current $currentVersionCode")
                return null
            }
            
            Logger.d("UpdateInfo", "Update available! New versionCode: $versionCode")
            
            return UpdateInfo(
                versionName = release.tag_name.removePrefix("v"),
                versionCode = versionCode,
                downloadUrl = apkAsset.browser_download_url,
                releaseNotes = release.body,
                fileName = apkAsset.name,
                fileSize = apkAsset.size
            )
        }
        
    }
}

