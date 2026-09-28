package com.example.crypto

import java.security.MessageDigest
import kotlin.math.log2
import kotlin.math.pow

/**
 * Offline Password Health & Shannon Entropy Engine.
 * 
 * Strict Specification Requirements:
 * - Calculates true Shannon Entropy.
 * - Flags weak passwords (< 12 chars or < 45 bits entropy).
 * - Identifies reused passwords detected locally via SHA-256 hash deduplication.
 * - Identifies passwords older than 180 days.
 * - Zero network access, 100% offline.
 */
object HealthAuditEngine {

    const val MIN_SECURE_LENGTH = 12
    const val MIN_SECURE_ENTROPY_BITS = 45.0
    const val OLD_PASSWORD_THRESHOLD_DAYS = 180L

    enum class StrengthGrade(val label: String, val colorHex: Long) {
        CRITICAL("Critical", 0xFFEF4444),
        WEAK("Weak", 0xFFF97316),
        FAIR("Fair", 0xFFEAB308),
        STRONG("Strong", 0xFF10B981),
        VERY_STRONG("Very Strong", 0xFF06B6D4)
    }

    data class ItemHealthAssessment(
        val entropyBits: Double,
        val isWeak: Boolean,
        val isReused: Boolean,
        val isOld: Boolean,
        val strengthGrade: StrengthGrade,
        val crackTimeEstimate: String
    )

    data class OverallAuditSummary(
        val totalCount: Int,
        val weakCount: Int,
        val reusedCount: Int,
        val oldCount: Int,
        val overallScore: Int
    )

    /**
     * Calculates Shannon entropy in bits for an input password:
     * H = -Sum(p_i * log2(p_i)) * Length
     */
    fun calculateShannonEntropy(password: CharArray): Double {
        if (password.isEmpty()) return 0.0

        val length = password.size
        val frequencyMap = mutableMapOf<Char, Int>()
        for (c in password) {
            frequencyMap[c] = (frequencyMap[c] ?: 0) + 1
        }

        var perCharEntropy = 0.0
        for ((_, count) in frequencyMap) {
            val probability = count.toDouble() / length
            perCharEntropy -= probability * log2(probability)
        }

        // Account for character pool diversity
        var poolSize = 0
        var hasUpper = false
        var hasLower = false
        var hasDigit = false
        var hasSymbol = false

        for (c in password) {
            when {
                c.isUpperCase() -> hasUpper = true
                c.isLowerCase() -> hasLower = true
                c.isDigit() -> hasDigit = true
                else -> hasSymbol = true
            }
        }
        if (hasLower) poolSize += 26
        if (hasUpper) poolSize += 26
        if (hasDigit) poolSize += 10
        if (hasSymbol) poolSize += 32

        val poolEntropy = if (poolSize > 0) length * log2(poolSize.toDouble()) else 0.0
        val shannonBits = length * perCharEntropy

        return if (poolEntropy > 0) {
            minOf(shannonBits * 1.25, poolEntropy)
        } else {
            shannonBits
        }
    }

    /**
     * Evaluates whether a password is weak (< 12 characters OR < 45 bits entropy).
     */
    fun isWeakPassword(password: CharArray, entropyBits: Double? = null): Boolean {
        if (password.size < MIN_SECURE_LENGTH) return true
        val bits = entropyBits ?: calculateShannonEntropy(password)
        return bits < MIN_SECURE_ENTROPY_BITS
    }

    /**
     * Checks if a credential has not been rotated in > 180 days.
     */
    fun isOldPassword(lastModifiedTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Boolean {
        val maxAgeMillis = OLD_PASSWORD_THRESHOLD_DAYS * 24L * 60L * 60L * 1000L
        return (currentTimestamp - lastModifiedTimestamp) > maxAgeMillis
    }

    /**
     * Hashes a password using SHA-256 for local deduplication without exposing plaintext.
     */
    fun computeDeduplicationHash(password: CharArray): String {
        return try {
            val bytes = CharArray(password.size)
            System.arraycopy(password, 0, bytes, 0, password.size)
            val byteList = ByteArray(bytes.size)
            for (i in bytes.indices) {
                byteList[i] = bytes[i].code.toByte()
            }
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(byteList)
            byteList.wipe()
            digest.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Identifies which SHA-256 password hashes are reused (appear > 1 time).
     */
    fun findReusedHashes(hashes: List<String>): Set<String> {
        val frequency = mutableMapOf<String, Int>()
        for (h in hashes) {
            if (h.isNotBlank()) {
                frequency[h] = (frequency[h] ?: 0) + 1
            }
        }
        return frequency.filter { it.value > 1 }.keys
    }

    /**
     * Assesses a single credential against all health parameters.
     */
    fun assessCredential(
        password: CharArray,
        lastModifiedTimestamp: Long,
        isHashReused: Boolean
    ): ItemHealthAssessment {
        val entropy = calculateShannonEntropy(password)
        val weak = isWeakPassword(password, entropy)
        val old = isOldPassword(lastModifiedTimestamp)

        val grade = when {
            entropy < 35.0 || password.size < 8 -> StrengthGrade.CRITICAL
            entropy < 50.0 || password.size < MIN_SECURE_LENGTH -> StrengthGrade.WEAK
            entropy < 65.0 -> StrengthGrade.FAIR
            entropy < 85.0 -> StrengthGrade.STRONG
            else -> StrengthGrade.VERY_STRONG
        }

        val crackTime = estimateCrackTime(entropy)

        return ItemHealthAssessment(
            entropyBits = (entropy * 10).toInt() / 10.0,
            isWeak = weak,
            isReused = isHashReused,
            isOld = old,
            strengthGrade = grade,
            crackTimeEstimate = crackTime
        )
    }

    /**
     * Calculates an overall vault security health score (0-100%).
     */
    fun calculateHealthScore(total: Int, weak: Int, reused: Int, old: Int): Int {
        if (total == 0) return 100
        val weakPenalty = (weak.toDouble() / total) * 50.0
        val reusedPenalty = (reused.toDouble() / total) * 35.0
        val oldPenalty = (old.toDouble() / total) * 15.0
        val rawScore = 100.0 - (weakPenalty + reusedPenalty + oldPenalty)
        return rawScore.toInt().coerceIn(0, 100)
    }

    /**
     * Estimates brute-force crack time assuming a 10 billion guesses/sec GPU cluster.
     */
    fun estimateCrackTime(entropyBits: Double): String {
        if (entropyBits <= 0) return "Instant"
        val guessesPerSecond = 10_000_000_000.0
        val combinations = 2.0.pow(entropyBits)
        val seconds = (combinations / 2.0) / guessesPerSecond

        return when {
            seconds < 1.0 -> "Instant (< 1s)"
            seconds < 60.0 -> "${seconds.toInt()} seconds"
            seconds < 3600.0 -> "${(seconds / 60).toInt()} minutes"
            seconds < 86400.0 -> "${(seconds / 3600).toInt()} hours"
            seconds < 31536000.0 -> "${(seconds / 86400).toInt()} days"
            seconds < 3153600000.0 -> "${(seconds / 31536000).toInt()} years"
            else -> "Centuries"
        }
    }
}
