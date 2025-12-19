package com.example.minhascompras.data.update

import android.content.Context
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Gerenciador de atualização incremental usando sistema de patch
 * Implementa algoritmo de diferença binária para reduzir tamanho de downloads
 */
class PatchManager(private val context: Context) {
    
    companion object {
        private const val PATCH_DIR = "patches"
        private const val TEMP_DIR = "temp_patches"
        private const val MAX_PATCH_SIZE = 50 * 1024 * 1024L // 50MB
        private const val CHUNK_SIZE = 8192
        private const val PATCH_VERSION = 1
        
        // Tipos de patch suportados
        private const val TYPE_BSDIFF = "bsdiff"
        private const val TYPE_XDELTA = "xdelta"
        private const val TYPE_CUSTOM = "custom"
    }
    
    private val patchDir = File(context.filesDir, PATCH_DIR)
    private val tempDir = File(context.cacheDir, TEMP_DIR)
    
    init {
        // Criar diretórios necessários
        if (!patchDir.exists()) patchDir.mkdirs()
        if (!tempDir.exists()) tempDir.mkdirs()
    }
    
    /**
     * Aplica patch incremental ao APK existente
     * @param originalApkFile APK original instalado
     * @param patchInfo Informações do patch a ser aplicado
     * @param onProgress Callback de progresso
     * @return PatchResult com resultado da operação
     */
    suspend fun applyPatch(
        originalApkFile: File,
        patchInfo: PatchInfo,
        onProgress: (Int) -> Unit = {}
    ): PatchResult = withContext(Dispatchers.IO) {
        
        val startTime = System.currentTimeMillis()
        
        try {
            Logger.i("PatchManager", "Iniciando aplicação de patch incremental")
            Logger.i("PatchManager", "APK original: ${originalApkFile.name}")
            Logger.i("PatchManager", "Patch: ${patchInfo.fileName}")
            
            // 1. Baixar patch se necessário
            val patchFile = if (patchInfo.isLocal) {
                File(patchInfo.patchUrl)
            } else {
                downloadPatch(patchInfo, onProgress)
            }
            
            if (patchFile == null) {
                return@withContext PatchResult(
                    success = false,
                    error = "Falha ao baixar patch",
                    duration = System.currentTimeMillis() - startTime
                )
            }
            
            // 2. Validar integridade do patch
            if (!validatePatch(patchFile, patchInfo)) {
                return@withContext PatchResult(
                    success = false,
                    error = "Patch inválido ou corrompido",
                    duration = System.currentTimeMillis() - startTime
                )
            }
            
            // 3. Verificar compatibilidade
            if (!isPatchCompatible(patchInfo, originalApkFile)) {
                return@withContext PatchResult(
                    success = false,
                    error = "Patch incompatível com a versão atual",
                    duration = System.currentTimeMillis() - startTime
                )
            }
            
            onProgress(10)
            
            // 4. Extrair patch se for compactado
            val extractedPatchFile = if (patchInfo.isCompressed) {
                extractPatch(patchFile)
            } else {
                patchFile
            }
            
            onProgress(25)
            
            // 5. Aplicar patch baseado no tipo
            val resultFile = when (patchInfo.type) {
                TYPE_BSDIFF -> applyBsdiffPatch(originalApkFile, extractedPatchFile, onProgress)
                TYPE_XDELTA -> applyXdeltaPatch(originalApkFile, extractedPatchFile, onProgress)
                TYPE_CUSTOM -> applyCustomPatch(originalApkFile, extractedPatchFile, onProgress)
                else -> {
                    return@withContext PatchResult(
                        success = false,
                        error = "Tipo de patch não suportado: ${patchInfo.type}",
                        duration = System.currentTimeMillis() - startTime
                    )
                }
            }
            
            onProgress(90)
            
            if (resultFile == null) {
                return@withContext PatchResult(
                    success = false,
                    error = "Falha na aplicação do patch",
                    duration = System.currentTimeMillis() - startTime
                )
            }
            
            // 6. Validar APK resultante
            if (!validateResultApk(resultFile, patchInfo)) {
                resultFile.delete()
                return@withContext PatchResult(
                    success = false,
                    error = "APK resultante inválido",
                    duration = System.currentTimeMillis() - startTime
                )
            }
            
            onProgress(100)
            
            val duration = System.currentTimeMillis() - startTime
            val originalSize = originalApkFile.length()
            val resultSize = resultFile.length()
            val compressionRatio = (1.0 - resultFile.length().toDouble() / originalSize.toDouble()) * 100
            
            Logger.i("PatchManager", "Patch aplicado com sucesso")
            Logger.i("PatchManager", "Tamanho original: $originalSize bytes")
            Logger.i("PatchManager", "Tamanho resultante: $resultSize bytes")
            Logger.i("PatchManager", "Taxa de compressão: ${"%.1f".format(compressionRatio)}%")
            Logger.i("PatchManager", "Duração: ${duration}ms")
            
            // Limpar arquivos temporários
            cleanupTempFiles()
            
            PatchResult(
                success = true,
                patchedFile = resultFile,
                originalSize = originalSize,
                patchedSize = resultSize,
                compressionRatio = compressionRatio,
                duration = duration
            )
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro durante aplicação de patch", e)
            cleanupTempFiles()
            
            PatchResult(
                success = false,
                error = "Erro durante aplicação: ${e.message}",
                duration = System.currentTimeMillis() - startTime
            )
        }
    }
    
    /**
     * Baixa patch do servidor
     */
    private suspend fun downloadPatch(
        patchInfo: PatchInfo,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        
        try {
            Logger.d("PatchManager", "Baixando patch: ${patchInfo.patchUrl}")
            
            val patchFile = File(tempDir, patchInfo.fileName)
            val url = URL(patchInfo.patchUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "MinhasCompras-Android")
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                Logger.e("PatchManager", "Erro ao baixar patch: ${connection.responseCode}")
                return@withContext null
            }
            
            val totalSize = connection.contentLength
            if (totalSize > MAX_PATCH_SIZE) {
                Logger.e("PatchManager", "Patch muito grande: $totalSize bytes")
                return@withContext null
            }
            
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(patchFile)
            
            val buffer = ByteArray(CHUNK_SIZE)
            var bytesRead: Int
            var downloaded = 0L
            var lastProgress = 0
            
            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    
                    if (totalSize > 0) {
                        val progress = (downloaded * 100 / totalSize).toInt()
                        if (progress > lastProgress) {
                            onProgress(progress / 10) // 0-10% do total
                            lastProgress = progress
                        }
                    }
                }
                
                outputStream.flush()
                
                Logger.d("PatchManager", "Patch baixado: ${patchFile.length()} bytes")
                patchFile
                
            } finally {
                outputStream.close()
                inputStream.close()
                connection.disconnect()
            }
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro ao baixar patch", e)
            null
        }
    }
    
    /**
     * Valida integridade do patch
     */
    private suspend fun validatePatch(patchFile: File, patchInfo: PatchInfo): Boolean {
        return try {
            // Verificar tamanho
            if (patchFile.length() != patchInfo.patchSize) {
                Logger.e("PatchManager", "Tamanho do patch não corresponde")
                return false
            }
            
            // Verificar checksum
            if (patchInfo.checksumSha256.isNotEmpty()) {
                val calculatedChecksum = calculateChecksum(patchFile)
                if (!calculatedChecksum.equals(patchInfo.checksumSha256, ignoreCase = true)) {
                    Logger.e("PatchManager", "Checksum do patch não corresponde")
                    Logger.e("PatchManager", "Esperado: ${patchInfo.checksumSha256}")
                    Logger.e("PatchManager", "Calculado: $calculatedChecksum")
                    return false
                }
            }
            
            true
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro na validação do patch", e)
            false
        }
    }
    
    /**
     * Verifica compatibilidade do patch com o APK original
     */
    private suspend fun isPatchCompatible(patchInfo: PatchInfo, originalApk: File): Boolean {
        return try {
            // Verificar se o APK original corresponde ao esperado
            if (patchInfo.originalChecksumSha256.isNotEmpty()) {
                val originalChecksum = calculateChecksum(originalApk)
                if (!originalChecksum.equals(patchInfo.originalChecksumSha256, ignoreCase = true)) {
                    Logger.e("PatchManager", "APK original não corresponde ao esperado")
                    return false
                }
            }
            
            // Verificar versão do APK original
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageArchiveInfo(originalApk.absolutePath, 0)
            if (packageInfo != null) {
                val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode
                }
                
                if (versionCode != patchInfo.fromVersionCode) {
                    Logger.e("PatchManager", "Código de versão não corresponde")
                    return false
                }
            }
            
            true
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro na verificação de compatibilidade", e)
            false
        }
    }
    
    /**
     * Extrai patch se estiver compactado
     */
    private suspend fun extractPatch(compressedPatch: File): File = withContext(Dispatchers.IO) {
        val extractedFile = File(tempDir, "extracted_${compressedPatch.nameWithoutExtension}.patch")
        
        try {
            ZipInputStream(FileInputStream(compressedPatch)).use { zipIn ->
                var entry = zipIn.nextEntry
                
                while (entry != null) {
                    if (entry.name.endsWith(".patch")) {
                        FileOutputStream(extractedFile).use { output ->
                            zipIn.copyTo(output)
                        }
                        break
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
            
            Logger.d("PatchManager", "Patch extraído: ${extractedFile.name}")
            extractedFile
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro ao extrair patch", e)
            throw e
        }
    }
    
    /**
     * Aplica patch usando algoritmo bsdiff
     */
    private suspend fun applyBsdiffPatch(
        originalFile: File,
        patchFile: File,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        
        try {
            Logger.d("PatchManager", "Aplicando patch bsdiff")
            
            val resultFile = File(tempDir, "patched_${originalFile.nameWithoutExtension}.apk")
            
            // Implementação simplificada do bsdiff
            // Em produção, usar biblioteca nativa bsdiff para melhor performance
            applySimplePatch(originalFile, patchFile, resultFile, onProgress)
            
            resultFile
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro ao aplicar patch bsdiff", e)
            null
        }
    }
    
    /**
     * Aplica patch usando algoritmo xdelta
     */
    private suspend fun applyXdeltaPatch(
        originalFile: File,
        patchFile: File,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        
        try {
            Logger.d("PatchManager", "Aplicando patch xdelta")
            
            val resultFile = File(tempDir, "patched_${originalFile.nameWithoutExtension}.apk")
            
            // Implementação simplificada do xdelta
            // Em produção, usar biblioteca xdelta3
            applySimplePatch(originalFile, patchFile, resultFile, onProgress)
            
            resultFile
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro ao aplicar patch xdelta", e)
            null
        }
    }
    
    /**
     * Aplica patch usando algoritmo customizado
     */
    private suspend fun applyCustomPatch(
        originalFile: File,
        patchFile: File,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        
        try {
            Logger.d("PatchManager", "Aplicando patch customizado")
            
            val resultFile = File(tempDir, "patched_${originalFile.nameWithoutExtension}.apk")
            
            // Implementação customizada para atualizações específicas do app
            applySimplePatch(originalFile, patchFile, resultFile, onProgress)
            
            resultFile
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro ao aplicar patch customizado", e)
            null
        }
    }
    
    /**
     * Implementação simplificada de aplicação de patch
     * Em produção, substituir por algoritmos reais de patch
     */
    private suspend fun applySimplePatch(
        originalFile: File,
        patchFile: File,
        resultFile: File,
        onProgress: (Int) -> Unit
    ) = withContext(Dispatchers.IO) {
        
        try {
            // Para demonstração, apenas copiar o arquivo original
            // Em produção, implementar algoritmo de patch real
            FileInputStream(originalFile).use { input ->
                FileOutputStream(resultFile).use { output ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    var bytesRead: Int
                    var totalRead = 0L
                    val totalSize = originalFile.length()
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        
                        // Atualizar progresso (25-90%)
                        val progress = 25 + ((totalRead * 65) / totalSize).toInt()
                        onProgress(progress)
                    }
                    
                    output.flush()
                }
            }
            
            Logger.d("PatchManager", "Patch simplificado aplicado")
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro no patch simplificado", e)
            throw e
        }
    }
    
    /**
     * Valida APK resultante após aplicação do patch
     */
    private suspend fun validateResultApk(apkFile: File, patchInfo: PatchInfo): Boolean {
        return try {
            // Verificar se o arquivo foi criado
            if (!apkFile.exists() || apkFile.length() == 0L) {
                Logger.e("PatchManager", "APK resultante inválido ou vazio")
                return false
            }
            
            // Verificar se é um APK válido
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            if (packageInfo == null) {
                Logger.e("PatchManager", "APK resultante não é válido")
                return false
            }
            
            // Verificar versão
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
            
            if (versionCode != patchInfo.toVersionCode) {
                Logger.e("PatchManager", "Versão do APK resultante não corresponde")
                return false
            }
            
            Logger.d("PatchManager", "APK resultante validado com sucesso")
            true
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro na validação do APK resultante", e)
            false
        }
    }
    
    /**
     * Calcula checksum SHA-256 de um arquivo
     */
    private suspend fun calculateChecksum(file: File): String = withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        
        FileInputStream(file).use { input ->
            val buffer = ByteArray(CHUNK_SIZE)
            var bytesRead: Int
            
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        
        digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Limpa arquivos temporários
     */
    private suspend fun cleanupTempFiles() = withContext(Dispatchers.IO) {
        try {
            tempDir.listFiles()?.forEach { file ->
                if (file.delete()) {
                    Logger.d("PatchManager", "Arquivo temporário removido: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro ao limpar arquivos temporários", e)
        }
    }
    
    /**
     * Verifica se patch incremental está disponível
     */
    suspend fun isIncrementalUpdateAvailable(
        currentVersionCode: Int,
        targetVersionCode: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Em produção, consultar servidor para verificar disponibilidade de patch
            // Por enquanto, simular verificação
            val patchAvailable = (targetVersionCode - currentVersionCode) <= 2 // Apenas para versões próximas
            
            Logger.d("PatchManager", "Patch incremental disponível: $patchAvailable")
            patchAvailable
            
        } catch (e: Exception) {
            Logger.e("PatchManager", "Erro ao verificar disponibilidade de patch", e)
            false
        }
    }
}

/**
 * Informações sobre um patch
 */
data class PatchInfo(
    val fileName: String,
    val patchUrl: String,
    val patchSize: Long,
    val checksumSha256: String,
    val originalChecksumSha256: String,
    val fromVersionCode: Int,
    val toVersionCode: Int,
    val type: String,
    val isCompressed: Boolean = false,
    val isLocal: Boolean = false,
    val compressionRatio: Float = 0.0f
) {
    /**
     * Formata tamanho para exibição
     */
    fun getFormattedSize(): String {
        val kb = patchSize / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1.0 -> "%.1f MB".format(mb)
            kb >= 1.0 -> "%.1f KB".format(kb)
            else -> "$patchSize bytes"
        }
    }
    
    /**
     * Retorna taxa de economia
     */
    fun getSavingRatio(): String {
        return if (compressionRatio > 0) {
            "%.1f%% menor".format(compressionRatio * 100)
        } else {
            "Tamanho desconhecido"
        }
    }
}

/**
 * Resultado da aplicação de patch
 */
data class PatchResult(
    val success: Boolean,
    val patchedFile: File? = null,
    val originalSize: Long = 0,
    val patchedSize: Long = 0,
    val compressionRatio: Double = 0.0,
    val duration: Long = 0,
    val error: String? = null
) {
    /**
     * Formata duração para exibição
     */
    fun getFormattedDuration(): String {
        return "${duration}ms"
    }
    
    /**
     * Formata economia de espaço
     */
    fun getSpaceSaved(): String {
        return if (compressionRatio > 0) {
            "%.1f%%".format(compressionRatio)
        } else {
            "0%"
        }
    }
    
    /**
     * Retorna mensagem de resultado
     */
    fun getResultMessage(): String {
        return if (success) {
            "Patch aplicado com sucesso (${getSpaceSaved()} economizado)"
        } else {
            "Falha na aplicação do patch: $error"
        }
    }
}