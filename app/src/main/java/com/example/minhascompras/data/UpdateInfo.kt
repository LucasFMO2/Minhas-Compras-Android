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
            // Lógica automática para calcular versionCode a partir do versionName
            // Fórmula baseada no histórico real do projeto:
            // 
            // Histórico:
            // - 2.3 -> 5
            // - 2.4 -> 6
            // - 2.5 -> 7
            // - 2.6 -> 8
            // - 2.7 -> 9
            // - 2.8 -> 10
            // - 2.9 -> 11
            // - 2.9.1 -> 12 (11 + 1)
            // - 2.9.2 -> 13 (11 + 2)
            // - 2.9.3 -> 14 (11 + 3)
            // - 2.10.0 -> 16
            //
            // Fórmula para versões 2.x:
            // - Para minor < 10: baseVersionCode = minor + 2
            // - Para minor >= 10: baseVersionCode = (major - 2) * 10 + minor + 5 + 1
            // - Para patch versions: versionCode = baseVersionCode + patch
            
            try {
                val cleanTag = tagName.removePrefix("v").removePrefix("V")
                val parts = cleanTag.split(".")
                
                android.util.Log.d("UpdateInfo", "Extracting versionCode from tag: $tagName -> $cleanTag")
                android.util.Log.d("UpdateInfo", "Parts: ${parts.joinToString(", ")}")
                
                if (parts.size >= 2) {
                    val major = parts[0].toIntOrNull() ?: 0
                    val minor = parts[1].toIntOrNull() ?: 0
                    
                    // Calcular baseVersionCode usando fórmula automática
                    val baseVersionCode = if (minor < 10) {
                        // Para versões 2.3 até 2.9: baseVersionCode = minor + 2
                        minor + 2
                    } else {
                        // Para versões 2.10+: baseVersionCode = (major - 2) * 10 + minor + 5 + 1
                        (major - 2) * 10 + minor + 5 + 1
                    }
                    
                    // Verificar se há patch version (ex: 2.9.1, 2.9.2, 2.9.3, 2.10.0)
                    if (parts.size >= 3) {
                        val patch = parts[2].toIntOrNull() ?: 0
                        // Adicionar patch ao baseVersionCode
                        val result = baseVersionCode + patch
                        android.util.Log.d("UpdateInfo", "Extracted versionCode (with patch): $result (base: $baseVersionCode + patch: $patch)")
                        return result
                    } else {
                        // Versão sem patch (ex: 2.9, 2.10)
                        android.util.Log.d("UpdateInfo", "Extracted versionCode (no patch): $baseVersionCode")
                        return baseVersionCode
                    }
                } else if (parts.size == 1) {
                    // Versão com apenas major (ex: 2, 3)
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

