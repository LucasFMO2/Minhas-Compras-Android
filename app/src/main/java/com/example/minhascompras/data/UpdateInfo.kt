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
            
            // Tentar extrair versionCode do nome do arquivo primeiro (mais rápido)
            // Formato esperado: minhascompras-v2.14.0-code64.apk ou minhascompras-v2.14.0.apk
            var versionCode = extractVersionCodeFromFileName(apkAsset.name, release.tag_name)
            
            // Se não conseguiu extrair do nome, tentar das release notes
            if (versionCode <= 0) {
                versionCode = extractVersionCodeFromReleaseNotes(release.body)
            }
            
            // Se ainda não conseguiu, fazer download parcial do APK (mais rápido que completo)
            if (versionCode <= 0) {
                Logger.d("UpdateInfo", "Extracting versionCode from APK (partial download)...")
                versionCode = updateManager.extractVersionCodeFromApkPartial(apkAsset.browser_download_url)
            }
            
            // Último recurso: download completo do APK (lento)
            if (versionCode <= 0) {
                Logger.d("UpdateInfo", "Extracting versionCode from APK (full download)...")
                versionCode = updateManager.extractVersionCodeFromApk(apkAsset.browser_download_url)
            }
            
            Logger.d("UpdateInfo", "Extracted versionCode: $versionCode")
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
         * Tenta extrair o versionCode do nome do arquivo APK.
         * Formatos suportados:
         * - minhascompras-v2.14.0-code64.apk
         * - minhascompras-v2.14.0.apk (usa tag_name como fallback)
         */
        private fun extractVersionCodeFromFileName(fileName: String, tagName: String): Int {
            try {
                // Tentar extrair do padrão "-codeXX" no nome do arquivo
                val codePattern = Regex("-code(\\d+)", RegexOption.IGNORE_CASE)
                val match = codePattern.find(fileName)
                if (match != null) {
                    val code = match.groupValues[1].toIntOrNull()
                    if (code != null && code > 0) {
                        Logger.d("UpdateInfo", "Extracted versionCode from filename: $code")
                        return code
                    }
                }
                
                // Se não encontrou, tentar extrair da tag (v2.14.0 -> assumir code baseado na versão)
                // Isso é menos confiável, mas melhor que nada
                val versionPattern = Regex("v?(\\d+)\\.(\\d+)\\.(\\d+)")
                val versionMatch = versionPattern.find(tagName)
                if (versionMatch != null) {
                    val major = versionMatch.groupValues[1].toIntOrNull() ?: 0
                    val minor = versionMatch.groupValues[2].toIntOrNull() ?: 0
                    val patch = versionMatch.groupValues[3].toIntOrNull() ?: 0
                    // Estimativa: major * 1000 + minor * 100 + patch (não é exato, mas melhor que 0)
                    val estimatedCode = major * 1000 + minor * 100 + patch
                    if (estimatedCode > 0) {
                        Logger.d("UpdateInfo", "Estimated versionCode from tag: $estimatedCode (may be inaccurate)")
                        // Retornar 0 para forçar download parcial/completo, pois estimativa não é confiável
                        return 0
                    }
                }
            } catch (e: Exception) {
                Logger.e("UpdateInfo", "Error extracting versionCode from filename", e)
            }
            return 0
        }
        
        /**
         * Tenta extrair o versionCode das release notes.
         * Procura por padrões como "versionCode: 64" ou "code: 64"
         */
        private fun extractVersionCodeFromReleaseNotes(releaseNotes: String): Int {
            try {
                val patterns = listOf(
                    Regex("versionCode[\\s:]+(\\d+)", RegexOption.IGNORE_CASE),
                    Regex("code[\\s:]+(\\d+)", RegexOption.IGNORE_CASE),
                    Regex("Version Code[\\s:]+(\\d+)", RegexOption.IGNORE_CASE)
                )
                
                for (pattern in patterns) {
                    val match = pattern.find(releaseNotes)
                    if (match != null) {
                        val code = match.groupValues[1].toIntOrNull()
                        if (code != null && code > 0) {
                            Logger.d("UpdateInfo", "Extracted versionCode from release notes: $code")
                            return code
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("UpdateInfo", "Error extracting versionCode from release notes", e)
            }
            return 0
        }
        
    }
}

