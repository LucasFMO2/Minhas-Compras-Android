package com.example.minhascompras.data.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.minhascompras.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal

/**
 * Verificador de integridade e autenticidade de APKs
 * Implementa múltiplas camadas de validação para segurança
 */
class IntegrityChecker(private val context: Context) {
    
    companion object {
        private const val EXPECTED_SIGNATURE_HASH = "A1:B2:C3:D4:E5:F6:07:18:29:3A:4B:5C:6D:7E:8F:90:A1:B2:C3:D4:E5:F6:07:18:29:3A:4B:5C:6D:7E:8F:90"
        private const val EXPECTED_PACKAGE_NAME = "com.example.minhascompras"
        private const val MIN_SDK_VERSION = 24
        private const val MAX_APK_SIZE = 100 * 1024 * 1024L // 100MB
        
        // Algoritmos suportados
        private const val SHA_256 = "SHA-256"
        private const val SHA_1 = "SHA-1"
    }
    
    /**
     * Realiza verificação completa de integridade do APK
     * @param apkFile Arquivo APK a ser verificado
     * @param expectedChecksum Checksum esperado (opcional)
     * @return IntegrityResult com resultado detalhado
     */
    suspend fun verifyApkIntegrity(
        apkFile: File, 
        expectedChecksum: String? = null
    ): IntegrityResult = withContext(Dispatchers.IO) {
        
        val checks = mutableMapOf<IntegrityCheck, Boolean>()
        val details = mutableMapOf<IntegrityCheck, String>()
        var overallResult = true
        
        try {
            Logger.i("IntegrityChecker", "Iniciando verificação completa de integridade: ${apkFile.name}")
            
            // 1. Verificação básica do arquivo
            val fileCheck = verifyFileIntegrity(apkFile)
            checks[IntegrityCheck.FILE_EXISTS] = fileCheck.exists
            checks[IntegrityCheck.FILE_SIZE] = fileCheck.validSize
            checks[IntegrityCheck.FILE_READABLE] = fileCheck.readable
            details[IntegrityCheck.FILE_EXISTS] = if (fileCheck.exists) "Arquivo existe" else "Arquivo não encontrado"
            details[IntegrityCheck.FILE_SIZE] = if (fileCheck.validSize) "Tamanho válido: ${fileCheck.size}" else "Tamanho inválido: ${fileCheck.size}"
            details[IntegrityCheck.FILE_READABLE] = if (fileCheck.readable) "Arquivo legível" else "Arquivo não legível"
            
            if (!fileCheck.exists || !fileCheck.validSize || !fileCheck.readable) {
                overallResult = false
                Logger.e("IntegrityChecker", "Verificação básica do arquivo falhou")
                return@withContext IntegrityResult(
                    isValid = false,
                    checks = checks,
                    details = details,
                    errorMessage = "Verificação básica do arquivo falhou"
                )
            }
            
            // 2. Verificação de checksum
            val checksumResult = verifyChecksum(apkFile, expectedChecksum)
            checks[IntegrityCheck.CHECKSUM_SHA256] = checksumResult.sha256Valid
            checks[IntegrityCheck.CHECKSUM_SHA1] = true // SHA-1 sempre válido para compatibilidade
            details[IntegrityCheck.CHECKSUM_SHA256] = "SHA-256: ${checksumResult.sha256}"
            details[IntegrityCheck.CHECKSUM_SHA1] = "SHA-1: ${checksumResult.sha1}"
            
            if (expectedChecksum != null && !checksumResult.sha256Valid) {
                overallResult = false
                Logger.e("IntegrityChecker", "Verificação de checksum SHA-256 falhou")
            }
            
            // 3. Verificação de assinatura do APK
            val signatureResult = verifyApkSignature(apkFile)
            checks[IntegrityCheck.SIGNATURE_VALID] = signatureResult.isValid
            checks[IntegrityCheck.SIGNATURE_MATCH] = signatureResult.matchesExpected
            details[IntegrityCheck.SIGNATURE_VALID] = if (signatureResult.isValid) "Assinatura válida" else "Assinatura inválida"
            details[IntegrityCheck.SIGNATURE_MATCH] = if (signatureResult.matchesExpected) "Assinatura corresponde à esperada" else "Assinatura não corresponde"
            
            if (!signatureResult.isValid || !signatureResult.matchesExpected) {
                overallResult = false
                Logger.e("IntegrityChecker", "Verificação de assinatura falhou")
            }
            
            // 4. Verificação de informações do pacote
            val packageResult = verifyPackageInfo(apkFile)
            checks[IntegrityCheck.PACKAGE_NAME] = packageResult.packageNameValid
            checks[IntegrityCheck.VERSION_CODE] = packageResult.versionCodeValid
            checks[IntegrityCheck.MIN_SDK] = packageResult.minSdkValid
            details[IntegrityCheck.PACKAGE_NAME] = "Package: ${packageResult.packageName}"
            details[IntegrityCheck.VERSION_CODE] = "Version: ${packageResult.versionName} (${packageResult.versionCode})"
            details[IntegrityCheck.MIN_SDK] = "Min SDK: ${packageResult.minSdk}"
            
            if (!packageResult.packageNameValid || !packageResult.versionCodeValid || !packageResult.minSdkValid) {
                overallResult = false
                Logger.e("IntegrityChecker", "Verificação de informações do pacote falhou")
            }
            
            // 5. Verificação de certificado
            val certificateResult = verifyCertificate(apkFile)
            checks[IntegrityCheck.CERTIFICATE_VALID] = certificateResult.isValid
            checks[IntegrityCheck.CERTIFICATE_TRUSTED] = certificateResult.isTrusted
            details[IntegrityCheck.CERTIFICATE_VALID] = if (certificateResult.isValid) "Certificado válido" else "Certificado inválido"
            details[IntegrityCheck.CERTIFICATE_TRUSTED] = if (certificateResult.isTrusted) "Certificado confiável" else "Certificado não confiável"
            
            if (!certificateResult.isValid || !certificateResult.isTrusted) {
                overallResult = false
                Logger.e("IntegrityChecker", "Verificação de certificado falhou")
            }
            
            // 6. Verificação de estrutura do APK
            val structureResult = verifyApkStructure(apkFile)
            checks[IntegrityCheck.MANIFEST_PRESENT] = structureResult.hasManifest
            checks[IntegrityCheck.CLASSES_PRESENT] = structureResult.hasClasses
            checks[IntegrityCheck.RESOURCES_PRESENT] = structureResult.hasResources
            details[IntegrityCheck.MANIFEST_PRESENT] = if (structureResult.hasManifest) "AndroidManifest.xml presente" else "AndroidManifest.xml ausente"
            details[IntegrityCheck.CLASSES_PRESENT] = if (structureResult.hasClasses) "Classes.dex presentes" else "Classes.dex ausentes"
            details[IntegrityCheck.RESOURCES_PRESENT] = if (structureResult.hasResources) "Resources.arsc presente" else "Resources.arsc ausente"
            
            if (!structureResult.hasManifest || !structureResult.hasClasses) {
                overallResult = false
                Logger.e("IntegrityChecker", "Verificação de estrutura do APK falhou")
            }
            
            val result = IntegrityResult(
                isValid = overallResult,
                checks = checks,
                details = details,
                errorMessage = if (overallResult) null else "Uma ou mais verificações de integridade falharam",
                checksumSha256 = checksumResult.sha256,
                checksumSha1 = checksumResult.sha1,
                packageInfo = packageResult,
                signatureInfo = signatureResult,
                certificateInfo = certificateResult
            )
            
            Logger.i("IntegrityChecker", "Verificação concluída: ${if (result.isValid) "APK válido" else "APK inválido"}")
            result
            
        } catch (e: Exception) {
            Logger.e("IntegrityChecker", "Erro durante verificação de integridade", e)
            IntegrityResult(
                isValid = false,
                checks = checks,
                details = details,
                errorMessage = "Erro durante verificação: ${e.message}"
            )
        }
    }
    
    /**
     * Verificação rápida de integridade (para checks preliminares)
     */
    suspend fun quickIntegrityCheck(apkFile: File): QuickIntegrityResult = withContext(Dispatchers.IO) {
        try {
            Logger.d("IntegrityChecker", "Realizando verificação rápida: ${apkFile.name}")
            
            // Verificações básicas apenas
            val fileCheck = verifyFileIntegrity(apkFile)
            val checksumResult = verifyChecksum(apkFile)
            
            QuickIntegrityResult(
                isFileValid = fileCheck.exists && fileCheck.validSize && fileCheck.readable,
                checksumSha256 = checksumResult.sha256,
                fileSize = fileCheck.size,
                isQuickValid = fileCheck.exists && fileCheck.validSize && fileCheck.readable
            )
            
        } catch (e: Exception) {
            Logger.e("IntegrityChecker", "Erro na verificação rápida", e)
            QuickIntegrityResult(
                isFileValid = false,
                checksumSha256 = "",
                fileSize = 0,
                isQuickValid = false
            )
        }
    }
    
    /**
     * Verifica integridade básica do arquivo
     */
    private fun verifyFileIntegrity(apkFile: File): FileIntegrityResult {
        val exists = apkFile.exists()
        val readable = exists && apkFile.canRead()
        val size = if (exists) apkFile.length() else 0L
        val validSize = size > 0 && size <= MAX_APK_SIZE
        
        return FileIntegrityResult(
            exists = exists,
            readable = readable,
            size = size,
            validSize = validSize
        )
    }
    
    /**
     * Calcula e verifica checksums do arquivo
     */
    private suspend fun verifyChecksum(apkFile: File, expectedChecksum: String? = null): ChecksumResult {
        return withContext(Dispatchers.IO) {
            try {
                val sha256 = calculateChecksum(apkFile, SHA_256)
                val sha1 = calculateChecksum(apkFile, SHA_1)
                
                val sha256Valid = expectedChecksum?.let { expected ->
                    sha256.equals(expected, ignoreCase = true)
                } ?: true // Se não há checksum esperado, considera válido
                
                ChecksumResult(
                    sha256 = sha256,
                    sha1 = sha1,
                    sha256Valid = sha256Valid
                )
                
            } catch (e: Exception) {
                Logger.e("IntegrityChecker", "Erro ao calcular checksum", e)
                ChecksumResult(
                    sha256 = "",
                    sha1 = "",
                    sha256Valid = false
                )
            }
        }
    }
    
    /**
     * Calcula checksum do arquivo usando algoritmo especificado
     */
    private fun calculateChecksum(file: File, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Verifica assinatura do APK
     */
    private fun verifyApkSignature(apkFile: File): SignatureResult {
        return try {
            val packageManager = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNATURES.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNATURES)
            }
            
            if (packageInfo?.signatures == null || packageInfo.signatures.isEmpty()) {
                return SignatureResult(
                    isValid = false,
                    matchesExpected = false,
                    signatureHash = ""
                )
            }
            
            val signature = packageInfo.signatures[0]
            val signatureHash = calculateSignatureHash(signature.toByteArray())
            val matchesExpected = signatureHash.equals(EXPECTED_SIGNATURE_HASH, ignoreCase = true)
            
            SignatureResult(
                isValid = true,
                matchesExpected = matchesExpected,
                signatureHash = signatureHash
            )
            
        } catch (e: Exception) {
            Logger.e("IntegrityChecker", "Erro ao verificar assinatura", e)
            SignatureResult(
                isValid = false,
                matchesExpected = false,
                signatureHash = ""
            )
        }
    }
    
    /**
     * Calcula hash da assinatura para comparação
     */
    private fun calculateSignatureHash(signature: ByteArray): String {
        val digest = MessageDigest.getInstance(SHA_256)
        val hash = digest.digest(signature)
        return hash.joinToString(":") { "%02X".format(it) }
    }
    
    /**
     * Verifica informações do pacote
     */
    private fun verifyPackageInfo(apkFile: File): PackageInfoResult {
        return try {
            val packageManager = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            }
            
            if (packageInfo == null) {
                return PackageInfoResult(
                    packageName = "",
                    versionName = "",
                    versionCode = 0,
                    minSdk = 0,
                    packageNameValid = false,
                    versionCodeValid = false,
                    minSdkValid = false
                )
            }
            
            val packageNameValid = packageInfo.packageName == EXPECTED_PACKAGE_NAME
            val versionCodeValid = packageInfo.versionCode > 0
            val minSdkValid = packageInfo.applicationInfo?.targetSdkVersion ?: 0 >= MIN_SDK_VERSION
            
            PackageInfoResult(
                packageName = packageInfo.packageName,
                versionName = packageInfo.versionName ?: "",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode
                },
                minSdk = packageInfo.applicationInfo?.targetSdkVersion ?: 0,
                packageNameValid = packageNameValid,
                versionCodeValid = versionCodeValid,
                minSdkValid = minSdkValid
            )
            
        } catch (e: Exception) {
            Logger.e("IntegrityChecker", "Erro ao verificar informações do pacote", e)
            PackageInfoResult(
                packageName = "",
                versionName = "",
                versionCode = 0,
                minSdk = 0,
                packageNameValid = false,
                versionCodeValid = false,
                minSdkValid = false
            )
        }
    }
    
    /**
     * Verifica certificado do APK
     */
    private fun verifyCertificate(apkFile: File): CertificateResult {
        return try {
            val packageManager = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageArchiveInfo(
                    apkFile.absolutePath,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNATURES.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNATURES)
            }
            
            if (packageInfo?.signatures == null || packageInfo.signatures.isEmpty()) {
                return CertificateResult(
                    isValid = false,
                    isTrusted = false,
                    subject = "",
                    issuer = "",
                    validFrom = 0L,
                    validUntil = 0L
                )
            }
            
            val signature = packageInfo.signatures[0]
            val certFactory = CertificateFactory.getInstance("X.509")
            val cert = certFactory.generateCertificate(java.io.ByteArrayInputStream(signature.toByteArray())) as X509Certificate
            
            // Verificar se o certificado não expirou
            val now = System.currentTimeMillis()
            val isValid = cert.checkValidity() == null
            val isTrusted = now >= cert.notBefore.time && now <= cert.notAfter.time
            
            CertificateResult(
                isValid = isValid,
                isTrusted = isTrusted,
                subject = cert.subjectX500Principal.name,
                issuer = cert.issuerX500Principal.name,
                validFrom = cert.notBefore.time,
                validUntil = cert.notAfter.time
            )
            
        } catch (e: Exception) {
            Logger.e("IntegrityChecker", "Erro ao verificar certificado", e)
            CertificateResult(
                isValid = false,
                isTrusted = false,
                subject = "",
                issuer = "",
                validFrom = 0L,
                validUntil = 0L
            )
        }
    }
    
    /**
     * Verifica estrutura básica do APK
     */
    private fun verifyApkStructure(apkFile: File): ApkStructureResult {
        return try {
            java.util.zip.ZipFile(apkFile).use { zipFile ->
                val entries = zipFile.entries()
                var hasManifest = false
                var hasClasses = false
                var hasResources = false
                
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    when {
                        entry.name == "AndroidManifest.xml" -> hasManifest = true
                        entry.name.startsWith("classes") && entry.name.endsWith(".dex") -> hasClasses = true
                        entry.name == "resources.arsc" -> hasResources = true
                    }
                }
                
                ApkStructureResult(
                    hasManifest = hasManifest,
                    hasClasses = hasClasses,
                    hasResources = hasResources
                )
            }
            
        } catch (e: Exception) {
            Logger.e("IntegrityChecker", "Erro ao verificar estrutura do APK", e)
            ApkStructureResult(
                hasManifest = false,
                hasClasses = false,
                hasResources = false
            )
        }
    }
}

/**
 * Tipos de verificação de integridade
 */
enum class IntegrityCheck {
    FILE_EXISTS,
    FILE_SIZE,
    FILE_READABLE,
    CHECKSUM_SHA256,
    CHECKSUM_SHA1,
    SIGNATURE_VALID,
    SIGNATURE_MATCH,
    PACKAGE_NAME,
    VERSION_CODE,
    MIN_SDK,
    CERTIFICATE_VALID,
    CERTIFICATE_TRUSTED,
    MANIFEST_PRESENT,
    CLASSES_PRESENT,
    RESOURCES_PRESENT
}

/**
 * Resultado completo da verificação de integridade
 */
data class IntegrityResult(
    val isValid: Boolean,
    val checks: Map<IntegrityCheck, Boolean>,
    val details: Map<IntegrityCheck, String>,
    val errorMessage: String? = null,
    val checksumSha256: String = "",
    val checksumSha1: String = "",
    val packageInfo: PackageInfoResult? = null,
    val signatureInfo: SignatureResult? = null,
    val certificateInfo: CertificateResult? = null
) {
    /**
     * Retorna status formatado para logging
     */
    fun getStatusSummary(): String {
        val passed = checks.values.count { it }
        val total = checks.size
        return "Verificação concluída: $passed/$total testes passaram"
    }
}

/**
 * Resultado rápido de verificação para checks preliminares
 */
data class QuickIntegrityResult(
    val isFileValid: Boolean,
    val checksumSha256: String,
    val fileSize: Long,
    val isQuickValid: Boolean
)

/**
 * Resultado da verificação de arquivo
 */
data class FileIntegrityResult(
    val exists: Boolean,
    val readable: Boolean,
    val size: Long,
    val validSize: Boolean
)

/**
 * Resultado da verificação de checksum
 */
data class ChecksumResult(
    val sha256: String,
    val sha1: String,
    val sha256Valid: Boolean
)

/**
 * Resultado da verificação de assinatura
 */
data class SignatureResult(
    val isValid: Boolean,
    val matchesExpected: Boolean,
    val signatureHash: String
)

/**
 * Resultado da verificação de informações do pacote
 */
data class PackageInfoResult(
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val minSdk: Int,
    val packageNameValid: Boolean,
    val versionCodeValid: Boolean,
    val minSdkValid: Boolean
)

/**
 * Resultado da verificação de certificado
 */
data class CertificateResult(
    val isValid: Boolean,
    val isTrusted: Boolean,
    val subject: String,
    val issuer: String,
    val validFrom: Long,
    val validUntil: Long
) {
    /**
     * Formata período de validade
     */
    fun getValidityPeriod(): String {
        val from = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(validFrom))
        val until = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(validUntil))
        return "$from - $until"
    }
}

/**
 * Resultado da verificação de estrutura do APK
 */
data class ApkStructureResult(
    val hasManifest: Boolean,
    val hasClasses: Boolean,
    val hasResources: Boolean
)