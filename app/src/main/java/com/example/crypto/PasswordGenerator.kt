package com.example.crypto

import java.security.SecureRandom

/**
 * Production-grade random password and Diceware passphrase generator.
 * Employs SecureRandom for CSPRNG guarantees.
 */
object PasswordGenerator {
    private val secureRandom = SecureRandom()

    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val DIGITS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?"
    private const val AMBIGUOUS = "il1Lo0O"

    // Precomputed clean sets with AMBIGUOUS characters removed
    private const val UPPERCASE_CLEAN = "ABCDEFGHJKMNPQRSTUVWXYZ"
    private const val LOWERCASE_CLEAN = "abcdefghjkmnpqrstuvwxyz"
    private const val DIGITS_CLEAN = "23456789"

    private val DICEWARE_WORDLIST = listOf(
        "abacus", "abbey", "ability", "abrupt", "absent", "absorb", "absurd", "accent", "acid", "acoustic",
        "acquire", "across", "action", "active", "actor", "adapt", "address", "advance", "aerial", "affect",
        "afford", "afraid", "agency", "agent", "agenda", "agree", "ahead", "aim", "airport", "alarm",
        "album", "alert", "algebra", "alien", "allot", "almost", "alone", "alpha", "already", "alter",
        "always", "amateur", "amazing", "amber", "ambient", "amend", "amount", "amuse", "anchor", "ancient",
        "angle", "animal", "animate", "ankle", "announce", "answer", "antenna", "antique", "anvil", "anyway",
        "apart", "apology", "appear", "apple", "apply", "approve", "apron", "arcade", "arch", "arctic",
        "arena", "argue", "armor", "arrow", "artist", "ascend", "ash", "aspect", "assault", "asset",
        "assist", "assume", "asteroid", "astral", "athlete", "atom", "attack", "attend", "attic", "audio",
        "audit", "august", "aunt", "aurora", "author", "auto", "autumn", "avatar", "avenue", "avoid",
        "awake", "aware", "awesome", "awful", "awkward", "axis", "baby", "bachelor", "bacon", "badge",
        "bagel", "balance", "balcony", "ball", "bamboo", "banana", "banner", "barber", "bargain", "barrel",
        "base", "basic", "basket", "battle", "beach", "beacon", "beam", "beauty", "become", "beef",
        "before", "begin", "behave", "behind", "belief", "belong", "bench", "benefit", "berry", "betray",
        "better", "between", "beyond", "bicycle", "bid", "bike", "bind", "biology", "bird", "birth",
        "bitter", "black", "blade", "blame", "blanket", "blast", "blaze", "blend", "bless", "blind",
        "block", "blood", "bloom", "blossom", "blouse", "blue", "blur", "board", "boat", "body",
        "boiler", "bold", "bolt", "bomb", "bone", "bonus", "book", "boost", "border", "boring",
        "bounce", "boundary", "brave", "bread", "breeze", "bridge", "bright", "bronze", "bubble", "budget",
        "bullet", "bundle", "bunker", "burst", "bus", "cabin", "cable", "cactus", "cage", "cake",
        "camera", "campus", "canal", "candle", "canvas", "canyon", "captain", "carbon", "cargo", "carpet",
        "castle", "casual", "catalog", "cave", "celestial", "cement", "center", "century", "cereal", "champion",
        "channel", "chapter", "charge", "chase", "cheap", "check", "cheese", "cherry", "chest", "chief",
        "cipher", "circle", "citizen", "city", "claim", "clap", "clarify", "classic", "clean", "clerk",
        "clever", "click", "cliff", "climate", "climb", "cloak", "clock", "clone", "cloth", "cloud",
        "cluster", "clutch", "coach", "coast", "cobalt", "coffee", "coin", "colony", "column", "comet",
        "comic", "common", "compact", "compass", "complex", "compute", "concert", "condor", "confirm", "connect",
        "conquer", "console", "control", "copper", "coral", "corner", "cosmos", "couch", "courage", "cradle",
        "craft", "crane", "crater", "crawford", "crazy", "credit", "creek", "crew", "cricket", "crimson",
        "crisis", "crisp", "critic", "cross", "crowd", "crucial", "cruise", "crystal", "cube", "culture",
        "cupboard", "curious", "current", "curtain", "cushion", "custom", "cyber", "cycle", "cylinder", "dagger"
    )

    data class RandomPasswordConfig(
        val length: Int = 16,
        val includeUppercase: Boolean = true,
        val includeLowercase: Boolean = true,
        val includeDigits: Boolean = true,
        val includeSymbols: Boolean = true,
        val excludeAmbiguous: Boolean = true
    )

    data class PassphraseConfig(
        val wordCount: Int = 4,
        val delimiter: String = "-",
        val capitalizeWords: Boolean = true
    )

    /**
     * Generates a random cryptographic character array.
     */
    fun generatePassword(config: RandomPasswordConfig): CharArray {
        var pool = ""
        val required = mutableListOf<Char>()

        val upperPool = if (config.excludeAmbiguous) UPPERCASE_CLEAN else UPPERCASE
        val lowerPool = if (config.excludeAmbiguous) LOWERCASE_CLEAN else LOWERCASE
        val digitsPool = if (config.excludeAmbiguous) DIGITS_CLEAN else DIGITS
        val symbolsPool = SYMBOLS

        if (config.includeUppercase) {
            pool += upperPool
            required.add(upperPool[secureRandom.nextInt(upperPool.length)])
        }
        if (config.includeLowercase) {
            pool += lowerPool
            required.add(lowerPool[secureRandom.nextInt(lowerPool.length)])
        }
        if (config.includeDigits) {
            pool += digitsPool
            required.add(digitsPool[secureRandom.nextInt(digitsPool.length)])
        }
        if (config.includeSymbols) {
            pool += symbolsPool
            required.add(symbolsPool[secureRandom.nextInt(symbolsPool.length)])
        }

        if (pool.isEmpty()) {
            pool = LOWERCASE
            required.add(LOWERCASE[secureRandom.nextInt(LOWERCASE.length)])
        }

        val targetLength = maxOf(config.length, required.size)
        val passwordChars = CharArray(targetLength)

        for (i in required.indices) {
            passwordChars[i] = required[i]
        }

        for (i in required.size until targetLength) {
            val randomIndex = secureRandom.nextInt(pool.length)
            passwordChars[i] = pool[randomIndex]
        }

        // Fisher-Yates shuffle
        for (i in passwordChars.size - 1 downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val temp = passwordChars[i]
            passwordChars[i] = passwordChars[j]
            passwordChars[j] = temp
        }

        return passwordChars
    }

    /**
     * Generates a Diceware passphrase using the curated wordlist.
     */
    fun generatePassphrase(config: PassphraseConfig): CharArray {
        val count = config.wordCount.coerceIn(3, 8)
        val selectedWords = mutableListOf<String>()

        for (i in 0 until count) {
            val index = secureRandom.nextInt(DICEWARE_WORDLIST.size)
            var word = DICEWARE_WORDLIST[index]
            if (config.capitalizeWords) {
                word = word.replaceFirstChar { it.uppercaseChar() }
            }
            selectedWords.add(word)
        }

        val joined = selectedWords.joinToString(config.delimiter)
        return joined.toCharArray()
    }
}
