package com.example.minhascompras.data

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
    val fileName: String
) {
    companion object {
        fun fromGitHubRelease(release: GitHubRelease, currentVersionCode: Int): UpdateInfo? {
            // Extrair versionCode do tag_name
            val versionCode = extractVersionCode(release.tag_name)
            
            android.util.Log.d("UpdateInfo", "Release tag: ${release.tag_name}")
            android.util.Log.d("UpdateInfo", "Extracted versionCode: $versionCode")
            android.util.Log.d("UpdateInfo", "Current versionCode: $currentVersionCode")
            android.util.Log.d("UpdateInfo", "Is update available: ${versionCode > currentVersionCode}")
            
            // Comparação: se o versionCode extraído for maior que o atual, há atualização
            if (versionCode <= currentVersionCode) {
                android.util.Log.d("UpdateInfo", "No update available - versionCode $versionCode <= current $currentVersionCode")
                return null
            }
            
            // Encontrar o asset do APK
            val apkAsset = release.assets.firstOrNull { 
                it.name.endsWith(".apk", ignoreCase = true) 
            }
            
            if (apkAsset == null) {
                android.util.Log.e("UpdateInfo", "No APK asset found in release")
                return null
            }
            
            android.util.Log.d("UpdateInfo", "APK asset found: ${apkAsset.name}")
            
            return UpdateInfo(
                versionName = release.tag_name.removePrefix("v"),
                versionCode = versionCode,
                downloadUrl = apkAsset.browser_download_url,
                releaseNotes = release.body,
                fileName = apkAsset.name
            )
        }
        
        private fun extractVersionCode(tagName: String): Int {
            // Mapeamento direto de versão para versionCode
            // Baseado no histórico real do projeto:
            // v2.3 -> versionCode 5
            // v2.4 -> versionCode 6
            // v2.5 -> versionCode 7
            // v2.6 -> versionCode 8
            // v2.7 -> versionCode 9
            // v2.8 -> versionCode 10
            // v2.9 -> versionCode 11
            // v2.9.1 -> versionCode 12
            // v2.9.2 -> versionCode 13
            val parts = tagName.removePrefix("v").split(".")
            return if (parts.size >= 2) {
                val major = parts[0].toIntOrNull() ?: 0
                val minor = parts[1].toIntOrNull() ?: 0
                
                // Verificar se há patch version (ex: 2.9.1, 2.9.2)
                if (parts.size >= 3) {
                    val patch = parts[2].toIntOrNull() ?: 0
                    // Obter versionCode base da versão major.minor
                    val baseVersionCode = when ("$major.$minor") {
                        "2.3" -> 5
                        "2.4" -> 6
                        "2.5" -> 7
                        "2.6" -> 8
                        "2.7" -> 9
                        "2.8" -> 10
                        "2.9" -> 11
                        else -> (major - 2) * 10 + minor + 5
                    }
                    // Adicionar patch ao versionCode base
                    // v2.9.1: 11 + 1 = 12
                    // v2.9.2: 11 + 2 = 13
                    baseVersionCode + patch
                } else {
                    // Versão sem patch (ex: 2.9)
                    when ("$major.$minor") {
                        "2.3" -> 5
                        "2.4" -> 6
                        "2.5" -> 7
                        "2.6" -> 8
                        "2.7" -> 9
                        "2.8" -> 10
                        "2.9" -> 11
                        "2.10" -> 12
                        "3.0" -> 13
                        else -> (major - 2) * 10 + minor + 5
                    }
                }
            } else {
                parts[0].toIntOrNull() ?: 0
            }
        }
    }
}

