package com.example.crypto

import kotlin.math.log2
import kotlin.math.pow

/**
 * Offline Password Health & Shannon Entropy Engine.
 * Evaluates bit strength, vulnerability metrics, and brute-force resistance.
 */
object EntropyCalculator {

    data class EntropyReport(
        val entropyBits: Double,
        val isWeak: Boolean,
        val crackTimeFormatted: String,
        val strengthLevel: PasswordStrengthLevel
    )

    enum class PasswordStrengthLevel(val label: String, val colorHex: Long) {
        VERY_WEAK("Very Weak", 0xFFEF4444),
        WEAK("Weak", 0xFFF97316),
        FAIR("Fair", 0xFFEAB308),
        STRONG("Strong", 0xFF10B981),
        VERY_STRONG("Very Strong", 0xFF06B6D4)
    }

    /**
     * Calculates true Shannon entropy in bits for the input characters:
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

        // Harmonic blend between Shannon distribution and pool space
        return if (poolEntropy > 0) {
            minOf(shannonBits * 1.25, poolEntropy)
        } else {
            shannonBits
        }
    }

    fun analyze(password: CharArray): EntropyReport {
        val bits = calculateShannonEntropy(password)
        val isWeak = password.size < 12 || bits < 45.0

        val level = when {
            bits < 35.0 || password.size < 8 -> PasswordStrengthLevel.VERY_WEAK
            bits < 50.0 || password.size < 12 -> PasswordStrengthLevel.WEAK
            bits < 65.0 -> PasswordStrengthLevel.FAIR
            bits < 85.0 -> PasswordStrengthLevel.STRONG
            else -> PasswordStrengthLevel.VERY_STRONG
        }

        val crackTime = estimateCrackTime(bits)
        return EntropyReport(
            entropyBits = (bits * 10).toInt() / 10.0,
            isWeak = isWeak,
            crackTimeFormatted = crackTime,
            strengthLevel = level
        )
    }

    /**
     * Checks if a vault record is considered old (> 180 days).
     */
    fun isOldPassword(lastModifiedTimestamp: Long): Boolean {
        val oneHundredEightyDaysMillis = 180L * 24 * 60 * 60 * 1000
        return (System.currentTimeMillis() - lastModifiedTimestamp) > oneHundredEightyDaysMillis
    }

    /**
     * Estimates offline brute force crack time assuming high-end GPU cluster (10^10 hashes/sec).
     */
    private fun estimateCrackTime(entropyBits: Double): String {
        if (entropyBits <= 0) return "Instant"
        val guessesPerSecond = 10_000_000_000.0 // 10 Billion guesses per second
        val combinations = 2.0.pow(entropyBits)
        val seconds = (combinations / 2.0) / guessesPerSecond

        return when {
            seconds < 1 -> "Instant (< 1s)"
            seconds < 60 -> "${seconds.toInt()} seconds"
            seconds < 3600 -> "${(seconds / 60).toInt()} minutes"
            seconds < 86400 -> "${(seconds / 3600).toInt()} hours"
            seconds < 31536000 -> "${(seconds / 86400).toInt()} days"
            seconds < 3153600000.0 -> "${(seconds / 31536000).toInt()} years"
            else -> "Centuries"
        }
    }
}
