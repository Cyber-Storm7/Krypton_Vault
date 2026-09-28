package com.example.bluetooth

/**
 * Converts standard ASCII characters into USB HID keyboard scancodes and modifiers
 * according to the USB HID Usage Tables specification (Report ID = 0).
 */
object HidScancodeConverter {

    const val MODIFIER_NONE: Byte = 0x00
    const val MODIFIER_LEFT_CTRL: Byte = 0x01
    const val MODIFIER_LEFT_SHIFT: Byte = 0x02
    const val MODIFIER_LEFT_ALT: Byte = 0x04
    const val MODIFIER_LEFT_GUI: Byte = 0x08

    const val USAGE_RETURN: Byte = 0x28
    const val USAGE_ESCAPE: Byte = 0x29
    const val USAGE_BACKSPACE: Byte = 0x2A
    const val USAGE_TAB: Byte = 0x2B
    const val USAGE_SPACEBAR: Byte = 0x2C

    data class KeyStroke(
        val usageCode: Byte,
        val modifier: Byte = MODIFIER_NONE
    )

    fun charToKeyStroke(char: Char): KeyStroke? {
        return when (char) {
            in 'a'..'z' -> KeyStroke(usageCode = (0x04 + (char - 'a')).toByte(), modifier = MODIFIER_NONE)
            in 'A'..'Z' -> KeyStroke(usageCode = (0x04 + (char - 'A')).toByte(), modifier = MODIFIER_LEFT_SHIFT)
            '1' -> KeyStroke(0x1E, MODIFIER_NONE)
            '2' -> KeyStroke(0x1F, MODIFIER_NONE)
            '3' -> KeyStroke(0x20, MODIFIER_NONE)
            '4' -> KeyStroke(0x21, MODIFIER_NONE)
            '5' -> KeyStroke(0x22, MODIFIER_NONE)
            '6' -> KeyStroke(0x23, MODIFIER_NONE)
            '7' -> KeyStroke(0x24, MODIFIER_NONE)
            '8' -> KeyStroke(0x25, MODIFIER_NONE)
            '9' -> KeyStroke(0x26, MODIFIER_NONE)
            '0' -> KeyStroke(0x27, MODIFIER_NONE)

            '!' -> KeyStroke(0x1E, MODIFIER_LEFT_SHIFT)
            '@' -> KeyStroke(0x1F, MODIFIER_LEFT_SHIFT)
            '#' -> KeyStroke(0x20, MODIFIER_LEFT_SHIFT)
            '$' -> KeyStroke(0x21, MODIFIER_LEFT_SHIFT)
            '%' -> KeyStroke(0x22, MODIFIER_LEFT_SHIFT)
            '^' -> KeyStroke(0x23, MODIFIER_LEFT_SHIFT)
            '&' -> KeyStroke(0x24, MODIFIER_LEFT_SHIFT)
            '*' -> KeyStroke(0x25, MODIFIER_LEFT_SHIFT)
            '(' -> KeyStroke(0x26, MODIFIER_LEFT_SHIFT)
            ')' -> KeyStroke(0x27, MODIFIER_LEFT_SHIFT)

            '\n' -> KeyStroke(USAGE_RETURN, MODIFIER_NONE)
            '\t' -> KeyStroke(USAGE_TAB, MODIFIER_NONE)
            ' ' -> KeyStroke(USAGE_SPACEBAR, MODIFIER_NONE)

            '-' -> KeyStroke(0x2D, MODIFIER_NONE)
            '_' -> KeyStroke(0x2D, MODIFIER_LEFT_SHIFT)
            '=' -> KeyStroke(0x2E, MODIFIER_NONE)
            '+' -> KeyStroke(0x2E, MODIFIER_LEFT_SHIFT)
            '[' -> KeyStroke(0x2F, MODIFIER_NONE)
            '{' -> KeyStroke(0x2F, MODIFIER_LEFT_SHIFT)
            ']' -> KeyStroke(0x30, MODIFIER_NONE)
            '}' -> KeyStroke(0x30, MODIFIER_LEFT_SHIFT)
            '\\' -> KeyStroke(0x31, MODIFIER_NONE)
            '|' -> KeyStroke(0x31, MODIFIER_LEFT_SHIFT)
            ';' -> KeyStroke(0x33, MODIFIER_NONE)
            ':' -> KeyStroke(0x33, MODIFIER_LEFT_SHIFT)
            '\'' -> KeyStroke(0x34, MODIFIER_NONE)
            '"' -> KeyStroke(0x34, MODIFIER_LEFT_SHIFT)
            '`' -> KeyStroke(0x35, MODIFIER_NONE)
            '~' -> KeyStroke(0x35, MODIFIER_LEFT_SHIFT)
            ',' -> KeyStroke(0x36, MODIFIER_NONE)
            '<' -> KeyStroke(0x36, MODIFIER_LEFT_SHIFT)
            '.' -> KeyStroke(0x37, MODIFIER_NONE)
            '>' -> KeyStroke(0x37, MODIFIER_LEFT_SHIFT)
            '/' -> KeyStroke(0x38, MODIFIER_NONE)
            '?' -> KeyStroke(0x38, MODIFIER_LEFT_SHIFT)

            else -> null
        }
    }
}
