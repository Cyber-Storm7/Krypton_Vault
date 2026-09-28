package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import com.example.crypto.wipe

object ClipboardHelper {
    private val handler = Handler(Looper.getMainLooper())
    private var pendingClearRunnable: Runnable? = null

    /**
     * Copies sensitive text (like passwords or TOTP) and marks it as sensitive for Android 13+
     * so it doesn't stay visible in the system clipboard overlay, plus auto-clears it after 30 seconds.
     */
    fun copySensitive(context: Context, label: String, text: CharArray, autoClearDelayMillis: Long = 30_000L) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val textString = String(text)
        val clip = ClipData.newPlainText(label, textString)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }

        clipboard.setPrimaryClip(clip)

        // Cancel previous timer
        pendingClearRunnable?.let { handler.removeCallbacks(it) }

        // Schedule auto-clear after 30 seconds
        val runnable = Runnable {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    clipboard.clearPrimaryClip()
                } else {
                    clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                }
            } catch (_: Exception) {}
        }
        pendingClearRunnable = runnable
        handler.postDelayed(runnable, autoClearDelayMillis)
    }

    fun copyPlain(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
