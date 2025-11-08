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
            
            android.util.Log.d("UpdateInfo", "=== Update Check ===")
            android.util.Log.d("UpdateInfo", "Release tag: ${release.tag_name}")
            android.util.Log.d("UpdateInfo", "Extracted versionCode: $versionCode")
            android.util.Log.d("UpdateInfo", "Current versionCode: $currentVersionCode")
            android.util.Log.d("UpdateInfo", "Comparison: $versionCode > $currentVersionCode = ${versionCode > currentVersionCode}")
            
            // Validação: versionCode deve ser maior que 0
            if (versionCode <= 0) {
                android.util.Log.e("UpdateInfo", "Invalid versionCode extracted: $versionCode")
                return null
            }
            
            // Comparação: se o versionCode extraído for maior que o atual, há atualização
            if (versionCode <= currentVersionCode) {
                android.util.Log.d("UpdateInfo", "No update available - versionCode $versionCode <= current $currentVersionCode")
                return null
            }
            
            android.util.Log.d("UpdateInfo", "Update available! New versionCode: $versionCode")
            
            // Encontrar o asset do APK
            val apkAsset = release.assets.firstOrNull { 
                it.name.endsWith(".apk", ignoreCase = true) 
            }
            
            if (apkAsset == null) {
                android.util.Log.e("UpdateInfo", "No APK asset found in release. Assets: ${release.assets.map { it.name }}")
                return null
            }
            
            android.util.Log.d("UpdateInfo", "APK asset found: ${apkAsset.name}")
            android.util.Log.d("UpdateInfo", "Download URL: ${apkAsset.browser_download_url}")
            
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
            // v2.9.3 -> versionCode 14
            try {
                val cleanTag = tagName.removePrefix("v").removePrefix("V")
                val parts = cleanTag.split(".")
                
                android.util.Log.d("UpdateInfo", "Extracting versionCode from tag: $tagName -> $cleanTag")
                android.util.Log.d("UpdateInfo", "Parts: ${parts.joinToString(", ")}")
                
                if (parts.size >= 2) {
                    val major = parts[0].toIntOrNull() ?: 0
                    val minor = parts[1].toIntOrNull() ?: 0
                    
                    // Verificar se há patch version (ex: 2.9.1, 2.9.2, 2.9.3)
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
                        // v2.9.3: 11 + 3 = 14
                        val result = baseVersionCode + patch
                        android.util.Log.d("UpdateInfo", "Extracted versionCode (with patch): $result (base: $baseVersionCode + patch: $patch)")
                        return result
                    } else {
                        // Versão sem patch (ex: 2.9)
                        val result = when ("$major.$minor") {
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
                        android.util.Log.d("UpdateInfo", "Extracted versionCode (no patch): $result")
                        return result
                    }
                } else if (parts.size == 1) {
                    val result = parts[0].toIntOrNull() ?: 0
                    android.util.Log.d("UpdateInfo", "Extracted versionCode (single part): $result")
                    return result
                } else {
                    android.util.Log.e("UpdateInfo", "Invalid tag format: $tagName")
                    return 0
                }
            } catch (e: Exception) {
                android.util.Log.e("UpdateInfo", "Error extracting versionCode from tag: $tagName", e)
                return 0
            }
        }
    }
}

