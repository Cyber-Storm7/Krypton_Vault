package com.example.util

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.crypto.CryptoManager
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * LifecycleManager:
 * Centralized auto-lock controller powered by [ProcessLifecycleOwner].
 * Monitors app-level foreground/background transitions rather than individual Activity pauses,
 * eliminating false-positive locks triggered by share intents, permission dialogs, and
 * system overlays.
 *
 * Key Features:
 * - Uses ProcessLifecycleOwner for accurate app-wide background detection
 * - Supports an [ignoreNextPause] flag to suppress one background transition (e.g., launching a share chooser)
 * - Configurable timeout via [SecurityPreferencesManager]
 * - Thread-safe via AtomicBoolean/AtomicLong
 */
object LifecycleManager : DefaultLifecycleObserver {

    private val lastBackgroundTimestamp = AtomicLong(0L)
    private val lastInteractionTimestamp = AtomicLong(System.currentTimeMillis())
    private val isInitialized = AtomicBoolean(false)

    /**
     * When set to true, the next onStop (background) event will be silently ignored
     * and the flag will be auto-reset. Use this before launching external intents
     * (share choosers, file pickers, etc.) to prevent spurious vault locks.
     */
    private val ignoreNextPause = AtomicBoolean(false)

    /** Callback invoked when the vault should be locked due to inactivity timeout. */
    var onAutoLock: (() -> Unit)? = null

    /**
     * Initialize the lifecycle observer. Safe to call multiple times; only the first call
     * registers with ProcessLifecycleOwner.
     */
    fun initialize(context: Context) {
        if (isInitialized.compareAndSet(false, true)) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    /**
     * Set the flag to suppress the next background transition from being treated as
     * a lock-worthy event. The flag auto-resets after one use.
     *
     * Call this immediately before launching a share intent, file picker, or any
     * external activity that will cause the app to momentarily move to the background.
     */
    fun suppressNextPause() {
        ignoreNextPause.set(true)
    }

    /**
     * Record a user interaction timestamp. Call from [Activity.onUserInteraction].
     */
    fun recordInteraction() {
        lastInteractionTimestamp.set(System.currentTimeMillis())
    }

    /**
     * Returns the timestamp of the last recorded user interaction.
     */
    fun getLastInteractionTime(): Long = lastInteractionTimestamp.get()

    // ── ProcessLifecycleOwner Callbacks ──────────────────────────────────

    override fun onStart(owner: LifecycleOwner) {
        // App has come to the foreground — check if we exceeded the timeout while backgrounded
        checkAutoLockTimeout()
    }

    override fun onStop(owner: LifecycleOwner) {
        // App has moved to the background
        if (ignoreNextPause.compareAndSet(true, false)) {
            // This transition was expected (e.g. share intent) — don't record it
            return
        }
        if (CryptoManager.isUnlocked()) {
            lastBackgroundTimestamp.set(System.currentTimeMillis())
        }
    }

    // ── Timeout Logic ────────────────────────────────────────────────────

    /**
     * Checks if enough time has elapsed since the last background event to warrant
     * an auto-lock. Uses the timeout configured in [SecurityPreferencesManager].
     */
    private fun checkAutoLockTimeout() {
        if (!CryptoManager.isUnlocked()) return

        val bgTime = lastBackgroundTimestamp.get()
        if (bgTime <= 0L) return

        try {
            val context = com.example.KryptonApplication.instance
            val timeoutSec = SecurityPreferencesManager.getAutoLockTimeoutSeconds(context)
            val elapsedMs = System.currentTimeMillis() - bgTime

            if (elapsedMs >= timeoutSec * 1000L) {
                lastBackgroundTimestamp.set(0L)
                onAutoLock?.invoke()
            }
        } catch (_: Exception) {
            // Application instance not yet available — skip
        }
    }

    /**
     * Manually check foreground inactivity (called from a polling coroutine in the UI).
     * Returns true if the vault should be locked.
     */
    fun shouldLockDueToInactivity(context: Context): Boolean {
        if (!CryptoManager.isUnlocked()) return false
        val timeoutSec = SecurityPreferencesManager.getAutoLockTimeoutSeconds(context)
        val idleMs = System.currentTimeMillis() - lastInteractionTimestamp.get()
        return idleMs >= timeoutSec * 1000L
    }
}
