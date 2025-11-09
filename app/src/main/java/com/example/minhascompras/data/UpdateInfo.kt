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
        fun fromGitHubRelease(release: GitHubRelease, currentVersionCode: Int): UpdateInfo? {
            // Extrair versionCode do tag_name
            val versionCode = extractVersionCode(release.tag_name)
            
            Logger.d("UpdateInfo", "=== Update Check ===")
            Logger.d("UpdateInfo", "Release tag: ${release.tag_name}")
            Logger.d("UpdateInfo", "Extracted versionCode: $versionCode")
            Logger.d("UpdateInfo", "Current versionCode: $currentVersionCode")
            Logger.d("UpdateInfo", "Comparison: $versionCode > $currentVersionCode = ${versionCode > currentVersionCode}")
            
            // Validação: versionCode deve ser maior que 0
            if (versionCode <= 0) {
                Logger.e("UpdateInfo", "Invalid versionCode extracted: $versionCode")
                return null
            }
            
            // Comparação: se o versionCode extraído for maior que o atual, há atualização
            if (versionCode <= currentVersionCode) {
                Logger.d("UpdateInfo", "No update available - versionCode $versionCode <= current $currentVersionCode")
                return null
            }
            
            Logger.d("UpdateInfo", "Update available! New versionCode: $versionCode")
            
            // Encontrar o asset do APK
            val apkAsset = release.assets.firstOrNull { 
                it.name.endsWith(".apk", ignoreCase = true) 
            }
            
            if (apkAsset == null) {
                Logger.e("UpdateInfo", "No APK asset found in release. Assets: ${release.assets.map { it.name }}")
                return null
            }
            
            Logger.d("UpdateInfo", "APK asset found: ${apkAsset.name}")
            Logger.d("UpdateInfo", "Download URL: ${apkAsset.browser_download_url}")
            
            return UpdateInfo(
                versionName = release.tag_name.removePrefix("v"),
                versionCode = versionCode,
                downloadUrl = apkAsset.browser_download_url,
                releaseNotes = release.body,
                fileName = apkAsset.name,
                fileSize = apkAsset.size
            )
        }
        
        /**
         * Extrai o versionCode de uma tag de versão usando fórmula genérica sequencial.
         * Suporta versões com múltiplos níveis: major.minor.patch.build.revision...
         * 
         * Fórmula:
         * - Para versões 2.x sem patch (x <= 9): versionCode = x + 2
         * - Para versões 2.x sem patch (x >= 10): versionCode = 2*x - 4
         * - Para versões 2.x.y com patch: versionCode = baseVersionCode(x) + y
         * - Para versões com mais componentes (ex: 2.10.1.2): cada componente adicional incrementa sequencialmente
         * 
         * Exemplos de cálculo:
         * - 2.3 = 3 + 2 = 5
         * - 2.9 = 9 + 2 = 11
         * - 2.9.1 = 11 + 1 = 12
         * - 2.9.3 = 11 + 3 = 14
         * - 2.10.0 = 2*10 - 4 = 16
         * - 2.10.1 = 16 + 1 = 17
         * - 2.10.1.2 = 16 + 1 + 2 = 19
         * - 2.11.0 = 2*11 - 4 = 18
         * - 2.11.0.1 = 18 + 0 + 1 = 19
         * - 2.12.0 = 2*12 - 4 = 20
         */
        private fun extractVersionCode(tagName: String): Int {
            try {
                val cleanTag = tagName.removePrefix("v").removePrefix("V")
                val parts = cleanTag.split(".")
                
                Logger.d("UpdateInfo", "Extracting versionCode from tag: $tagName -> $cleanTag")
                Logger.d("UpdateInfo", "Parts: ${parts.joinToString(", ")}")
                
                if (parts.size < 2) {
                    Logger.e("UpdateInfo", "Invalid tag format (need at least major.minor): $tagName")
                    return 0
                }
                
                val major = parts[0].toIntOrNull() ?: 0
                val minor = parts[1].toIntOrNull() ?: 0
                
                // Validar major version
                if (major != 2) {
                    Logger.w("UpdateInfo", "Unsupported major version: $major. Using generic formula.")
                }
                
                // Calcular baseVersionCode para versão major.minor sem patch
                // Fórmula genérica sequencial baseada no padrão observado:
                // - Para 2.x (x <= 9): versionCode = x + 2
                // - Para 2.x (x >= 10): versionCode = x + 6 + (x - 10) = 2*x - 4
                //   Isso garante: 2.10.0 = 16, 2.11.0 = 18, 2.12.0 = 20, etc.
                // - Para outras majors: usar fórmula genérica
                val baseVersionCode = when {
                    major == 2 && minor >= 3 && minor <= 9 -> minor + 2
                    major == 2 && minor >= 10 -> 2 * minor - 4  // 2.10.0 = 16, 2.11.0 = 18, 2.12.0 = 20
                    else -> (major - 2) * 10 + minor + 5  // Fórmula genérica para futuras majors
                }
                
                // Processar componentes adicionais (patch, build, revision, etc.)
                // Cada componente adicional incrementa o versionCode sequencialmente
                // Ex: 2.10.1.2 = baseVersionCode(2.10) + 1 + 2 = 16 + 1 + 2 = 19
                var result = baseVersionCode
                if (parts.size >= 3) {
                    // Processar todos os componentes após major.minor
                    val additionalComponents = parts.subList(2, parts.size)
                    val componentValues = additionalComponents.mapNotNull { it.toIntOrNull() }
                    
                    if (componentValues.isNotEmpty()) {
                        val sum = componentValues.sum()
                        result = baseVersionCode + sum
                        Logger.d("UpdateInfo", "Extracted versionCode (with ${additionalComponents.size} additional components): $result (base: $baseVersionCode + sum: $sum)")
                        Logger.d("UpdateInfo", "Component values: ${componentValues.joinToString(", ")}")
                    } else {
                        Logger.w("UpdateInfo", "Invalid component values in tag: $tagName")
                    }
                } else {
                    // Versão sem componentes adicionais (ex: 2.9, 2.10)
                    Logger.d("UpdateInfo", "Extracted versionCode (no additional components): $baseVersionCode")
                }
                
                return result
            } catch (e: Exception) {
                Logger.e("UpdateInfo", "Error extracting versionCode from tag: $tagName", e)
                return 0
            }
        }
    }
}

