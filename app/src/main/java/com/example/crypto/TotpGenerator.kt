package com.example.crypto

import java.nio.ByteBuffer
import java.security.GeneralSecurityException
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor
import kotlin.math.pow

/**
 * Pure Kotlin RFC 6238 Time-Based One-Time Password (TOTP) Generator.
 * Supports configurable digits (6 or 8), step interval (30s or 60s),
 * HMAC algorithms (HmacSHA1, HmacSHA256), and Base32 decoding with padding tolerance.
 */
object TotpGenerator {

    enum class HmacAlgorithm(val javaName: String) {
        SHA1("HmacSHA1"),
        SHA256("HmacSHA256")
    }

    /**
     * Generates a TOTP code for the given Base32 secret at current or specified timestamp.
     */
    fun generateTotp(
        secretBase32: String,
        timeMillis: Long = System.currentTimeMillis(),
        periodSeconds: Long = 30L,
        digits: Int = 6,
        algorithm: HmacAlgorithm = HmacAlgorithm.SHA1
    ): String {
        val cleanSecret = secretBase32.trim().replace(" ", "").replace("-", "")
        if (cleanSecret.isEmpty()) return "------"

        return try {
            val keyBytes = decodeBase32(cleanSecret)
            val currentCounter = floor(timeMillis / 1000.0 / periodSeconds).toLong()

            val counterBytes = ByteBuffer.allocate(8).putLong(currentCounter).array()

            val mac = Mac.getInstance(algorithm.javaName)
            val keySpec = SecretKeySpec(keyBytes, algorithm.javaName)
            mac.init(keySpec)
            val hash = mac.doFinal(counterBytes)

            // Dynamic truncation (RFC 4226)
            val offset = (hash[hash.size - 1].toInt() and 0x0F)
            val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                    ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)

            val modulus = 10.0.pow(digits.toDouble()).toInt()
            val otp = binary % modulus

            keyBytes.wipe()

            otp.toString().padStart(digits, '0')
        } catch (_: GeneralSecurityException) {
            "------"
        } catch (_: IllegalArgumentException) {
            "------"
        }
    }

    /**
     * Calculates the seconds remaining in the current time-step.
     */
    fun getRemainingSeconds(
        timeMillis: Long = System.currentTimeMillis(),
        periodSeconds: Long = 30L
    ): Int {
        val seconds = (timeMillis / 1000) % periodSeconds
        return (periodSeconds - seconds).toInt()
    }

    /**
     * Calculates the progress ratio (1.0f at start of cycle down to 0.0f at expiration).
     */
    fun getRemainingProgress(
        timeMillis: Long = System.currentTimeMillis(),
        periodSeconds: Long = 30L
    ): Float {
        val elapsed = (timeMillis / 1000.0) % periodSeconds
        val remaining = periodSeconds - elapsed
        return (remaining / periodSeconds).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Decodes an RFC 4648 Base32 string into a raw ByteArray.
     * Sanitizes lowercase, spaces, and padding '=' characters.
     */
    fun decodeBase32(input: String): ByteArray {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val clean = input.uppercase(Locale.US).replace("=", "").trim()

        var buffer = 0
        var bitsLeft = 0
        val output = mutableListOf<Byte>()

        for (char in clean) {
            val charVal = base32Chars.indexOf(char)
            if (charVal < 0) {
                // Ignore illegal characters or throw
                continue
            }

            buffer = (buffer shl 5) or charVal
            bitsLeft += 5

            if (bitsLeft >= 8) {
                val byteVal = (buffer shr (bitsLeft - 8)) and 0xFF
                output.add(byteVal.toByte())
                bitsLeft -= 8
            }
        }

        val result = ByteArray(output.size)
        for (i in output.indices) {
            result[i] = output[i]
        }
        return result
    }
}
