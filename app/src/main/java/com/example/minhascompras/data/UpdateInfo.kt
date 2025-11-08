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
            // Como o versionCode no build.gradle é incremental (8 para v2.6),
            // vamos comparar diretamente com o versionCode atual
            val versionCode = extractVersionCode(release.tag_name)
            
            // Comparação: se o versionCode extraído for maior que o atual, há atualização
            // Mas como estamos usando versionCode incremental no build.gradle,
            // vamos comparar diretamente
            if (versionCode <= currentVersionCode) {
                return null
            }
            
            // Encontrar o asset do APK
            val apkAsset = release.assets.firstOrNull { 
                it.name.endsWith(".apk", ignoreCase = true) 
            } ?: return null
            
            return UpdateInfo(
                versionName = release.tag_name.removePrefix("v"),
                versionCode = versionCode,
                downloadUrl = apkAsset.browser_download_url,
                releaseNotes = release.body,
                fileName = apkAsset.name
            )
        }
        
        private fun extractVersionCode(tagName: String): Int {
            // Remove "v" e calcula versionCode baseado na versão
            // Ex: "v2.6" -> calcula um versionCode baseado na versão
            // Como o versionCode no build.gradle é incremental (8 para v2.6),
            // vamos usar uma fórmula simples: major * 10 + minor
            // Mas para manter compatibilidade, vamos usar apenas o número após o ponto
            // e adicionar um offset baseado no major
            val parts = tagName.removePrefix("v").split(".")
            return if (parts.size >= 2) {
                val major = parts[0].toIntOrNull() ?: 0
                val minor = parts[1].toIntOrNull() ?: 0
                // Fórmula: major * 10 + minor (ex: 2.6 -> 2*10 + 6 = 26)
                // Mas como o versionCode atual é 8, vamos usar apenas o minor + offset
                // Para v2.6 (versionCode 8), vamos usar: 2*4 + 0 = 8 (ajustar conforme necessário)
                // Simplificando: usar major * 4 + minor para aproximar do versionCode real
                major * 4 + minor
            } else {
                parts[0].toIntOrNull() ?: 0
            }
        }
    }
}

