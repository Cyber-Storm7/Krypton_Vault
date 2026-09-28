package com.example.crypto

import java.util.Arrays

/**
 * Memory sanitization utilities for zero-knowledge security.
 * Clears sensitive arrays immediately after use to prevent memory dumping attacks.
 */
fun CharArray.wipe() {
    Arrays.fill(this, '\u0000')
}

fun ByteArray.wipe() {
    Arrays.fill(this, 0.toByte())
}
