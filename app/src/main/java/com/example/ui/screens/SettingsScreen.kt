package com.example.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.crypto.CryptoManager
import com.example.crypto.DuressVaultManager
import com.example.crypto.wipe
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.components.LiquidGlassScreenContainer
import com.example.ui.components.LiquidGlassSwitch
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.theme.DarkGlassSurface
import com.example.ui.theme.ElectricMint
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassIridescentBorderBrush
import com.example.ui.theme.KryptonThemeAccent
import com.example.ui.theme.LightGlassSurface
import com.example.ui.theme.LocalKryptonColors
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.VaultViewModel
import com.example.util.SecurityPreferencesManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: VaultViewModel,
    isFlagSecureEnabled: Boolean,
    onToggleFlagSecure: (Boolean) -> Unit,
    onLockVault: () -> Unit,
    onOpenSelectiveSync: ((List<com.example.model.VaultItemDecrypted>) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()

    val currentTheme by SecurityPreferencesManager.themeAccentFlow.collectAsState()
    val kryptonColors = LocalKryptonColors.current
    val isDark = kryptonColors.isDark

    // Ambient decoy session flag:
    // When inside decoy session, DUPLICATE/SUPPRESS all duress settings entirely!
    val isDecoySession = remember { DuressVaultManager.isCurrentSessionDecoy }

    // Duress Decoy states
    var duressEnabled by remember { mutableStateOf(DuressVaultManager.isDuressEnabled(context)) }
    var showDuressPinDialog by remember { mutableStateOf(false) }
    var duressPinInput by remember { mutableStateOf("") }
    var duressPinConfirm by remember { mutableStateOf("") }

    // Bluetooth Sync Dialog state
    var showBluetoothSyncDialog by remember { mutableStateOf(false) }

    // Scramble Keypad state
    var scrambleKeypadEnabled by remember {
        mutableStateOf(SecurityPreferencesManager.isScrambleKeypadEnabled(context))
    }

    // Auto-Lock Timeout state (10s, 30s, 60s)
    var autoLockTimeoutSeconds by remember {
        mutableStateOf(SecurityPreferencesManager.getAutoLockTimeoutSeconds(context))
    }

    // Quick PIN configuration state
    var isQuickPinConfigured by remember {
        mutableStateOf(CryptoManager.isQuickPinConfigured(context))
    }
    var showQuickPinDialog by remember { mutableStateOf(false) }
    var quickPinInput by remember { mutableStateOf("") }
    var quickPinConfirm by remember { mutableStateOf("") }

    // Biometric states
    var biometricEnabled by remember {
        mutableStateOf(CryptoManager.isBiometricEnabled(context))
    }

    LiquidGlassScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onNavigateBack != null) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(kryptonColors.surfaceElevated)
                            .border(1.dp, kryptonColors.outline, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = kryptonColors.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column {
                    Text(
                        text = "Security Suite",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = kryptonColors.textPrimary
                    )
                    Text(
                        text = "Zero-Knowledge Offline Hardening",
                        fontSize = 13.sp,
                        color = kryptonColors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 0. APPEARANCE & COLOR THEME
            SectionHeader(title = "Appearance & Theme", icon = Icons.Default.Palette)
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Accent Color Scheme",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = kryptonColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Active: ${currentTheme.title} ${if (currentTheme == KryptonThemeAccent.MONOCHROME) "(Default)" else ""}",
                                fontSize = 12.sp,
                                color = kryptonColors.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        KryptonThemeAccent.entries.forEach { accent ->
                            val isSelected = currentTheme == accent
                            val isMonochrome = accent == KryptonThemeAccent.MONOCHROME

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            kryptonColors.surfaceElevated
                                        } else {
                                            kryptonColors.neuSurface
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) kryptonColors.primary else kryptonColors.outline.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        SecurityPreferencesManager.setSelectedTheme(context, accent)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 1.dp,
                                                color = if (isDark) Color(0x40FFFFFF) else Color(0x40000000),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (accent.secondaryPreviewColor != null) {
                                            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                                                drawArc(
                                                    color = accent.previewColor,
                                                    startAngle = 90f,
                                                    sweepAngle = 180f,
                                                    useCenter = true
                                                )
                                                drawArc(
                                                    color = accent.secondaryPreviewColor,
                                                    startAngle = 270f,
                                                    sweepAngle = 180f,
                                                    useCenter = true
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(
                                                        if (isMonochrome) {
                                                            if (isDark) Color.White else Color.Black
                                                        } else {
                                                            accent.previewColor
                                                        }
                                                    )
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (isMonochrome) {
                                                    if (isDark) Color.Black else Color.White
                                                } else if (accent.secondaryPreviewColor != null) {
                                                    Color.Black
                                                } else {
                                                    Color.White
                                                },
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = accent.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) kryptonColors.primary else kryptonColors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. PLAUSIBLE DENIABILITY (DURESS / DECOY VAULT)
            // BUG 2 FIX: Completely hidden and suppressed if current session is a decoy vault!
            if (!isDecoySession) {
                SectionHeader(title = "Plausible Deniability", icon = Icons.Default.Shield)
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Duress Decoy Vault",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = kryptonColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Unlocks a plausible decoy vault with dummy credentials under physical coercion. Zero visual warnings.",
                                    fontSize = 12.sp,
                                    color = kryptonColors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            LiquidGlassSwitch(
                                checked = duressEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        showDuressPinDialog = true
                                    } else {
                                        DuressVaultManager.setDuressEnabled(context, false)
                                        duressEnabled = false
                                    }
                                },
                                testTag = "duress_vault_switch"
                            )
                        }

                        if (duressEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                LiquidGlassPillButton(
                                    text = "Set Duress PIN",
                                    onClick = { showDuressPinDialog = true },
                                    isPrimary = false,
                                    modifier = Modifier.weight(1f)
                                )
                                LiquidGlassPillButton(
                                    text = "Populate Decoys",
                                    onClick = {
                                        DuressVaultManager.populateDefaultDecoyItems()
                                        Toast.makeText(context, "Decoy vault loaded with dummy credentials", Toast.LENGTH_SHORT).show()
                                    },
                                    isPrimary = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }



            // 2.5 PEER-TO-PEER ENCRYPTED BLUETOOTH SYNC (Section 4 Specification)
            SectionHeader(title = "Peer Bluetooth Sync", icon = Icons.Default.Bluetooth)
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Encrypted Peer-to-Peer Sync",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = kryptonColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "High-speed offline RFCOMM socket transfer with 4-digit mutual authentication and AES-256-GCM encryption. Includes visual conflict diff resolution.",
                        fontSize = 12.sp,
                        color = kryptonColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LiquidGlassPillButton(
                        text = "Open Peer Bluetooth Sync",
                        onClick = { showBluetoothSyncDialog = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = kryptonColors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "open_bluetooth_sync_button",
                        isPrimary = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. KEYPAD & PIN ARMOR
            SectionHeader(title = "Keypad & Quick PIN", icon = Icons.Default.Dialpad)
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Scramble Keypad Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Scramble Keypad Layout",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = kryptonColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Randomizes numeric digit positions on each unlock to defend against thermal imaging and shoulder surfing.",
                                fontSize = 12.sp,
                                color = kryptonColors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        LiquidGlassSwitch(
                            checked = scrambleKeypadEnabled,
                            onCheckedChange = { checked ->
                                SecurityPreferencesManager.setScrambleKeypadEnabled(context, checked)
                                scrambleKeypadEnabled = checked
                            },
                            testTag = "scramble_keypad_switch"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6-digit Quick PIN Configuration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "6-Digit Quick PIN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = kryptonColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isQuickPinConfigured) "Configured for instant scrambled numpad access." else "Not set. Use Master Password on unlock.",
                                fontSize = 12.sp,
                                color = kryptonColors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        LiquidGlassPillButton(
                            text = if (isQuickPinConfigured) "Change PIN" else "Set PIN",
                            onClick = { showQuickPinDialog = true },
                            isPrimary = !isQuickPinConfigured,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Pin,
                                    contentDescription = null,
                                    tint = if (!isQuickPinConfigured) kryptonColors.onPrimary else kryptonColors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. AUTO-LOCK TIMEOUT (10s, 30s, 60s)
            SectionHeader(title = "Auto-Lock Security", icon = Icons.Default.Timer)
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Auto-Lock on App Exit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = kryptonColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vault remains unlocked while actively in use, and automatically locks when you leave or background the app after:",
                        fontSize = 12.sp,
                        color = kryptonColors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val lockOptions = listOf(
                            10 to "10 Seconds",
                            30 to "30 Seconds",
                            60 to "60 Seconds"
                        )
                        lockOptions.forEach { (sec, label) ->
                            val isSelected = autoLockTimeoutSeconds == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) kryptonColors.primary.copy(alpha = 0.2f)
                                        else kryptonColors.surfaceElevated
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) kryptonColors.primary else kryptonColors.outline,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        SecurityPreferencesManager.setAutoLockTimeoutSeconds(context, sec)
                                        autoLockTimeoutSeconds = sec
                                        Toast.makeText(context, "Auto-lock set to $label", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${sec}s",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) kryptonColors.primary else kryptonColors.textSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. HARDWARE BIOMETRICS & SYSTEM CONTROLS
            SectionHeader(title = "Hardware Protection", icon = Icons.Default.Fingerprint)
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Biometric Auto-Unlock toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Instant Biometric Auto-Unlock",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = kryptonColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Prompts fingerprint immediately on launch, unwrapping the AES-256 key directly from Android Keystore.",
                                fontSize = 12.sp,
                                color = kryptonColors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        LiquidGlassSwitch(
                            checked = biometricEnabled,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (activity != null) {
                                        try {
                                            val cipher = CryptoManager.getBiometricCipherForEncryption()
                                            val executor = ContextCompat.getMainExecutor(context)
                                            val prompt = BiometricPrompt(
                                                activity,
                                                executor,
                                                object : BiometricPrompt.AuthenticationCallback() {
                                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                        super.onAuthenticationSucceeded(result)
                                                        val authedCipher = result.cryptoObject?.cipher
                                                        if (authedCipher != null) {
                                                            scope.launch {
                                                                val success = CryptoManager.enableBiometricUnlock(context, authedCipher)
                                                                if (success) {
                                                                    biometricEnabled = true
                                                                    Toast.makeText(context, "Biometrics enabled", Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                        }
                                                    }
                                                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                        super.onAuthenticationError(errorCode, errString)
                                                        Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                            prompt.authenticate(
                                                BiometricPrompt.PromptInfo.Builder()
                                                    .setTitle("Configure Biometric Unlock")
                                                    .setSubtitle("Confirm your fingerprint to wrap the master key")
                                                    .setNegativeButtonText("Cancel")
                                                    .build(),
                                                BiometricPrompt.CryptoObject(cipher)
                                            )
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Biometrics setup error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        CryptoManager.disableBiometrics(context)
                                        biometricEnabled = false
                                    }
                                }
                            },
                            testTag = "biometrics_toggle_switch"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // FLAG_SECURE toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Anti-Screen Capture (FLAG_SECURE)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = kryptonColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Prevents screenshots, screen recording, and app switcher thumbnail leakage.",
                                fontSize = 12.sp,
                                color = kryptonColors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        LiquidGlassSwitch(
                            checked = isFlagSecureEnabled,
                            onCheckedChange = onToggleFlagSecure,
                            testTag = "flag_secure_switch"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lock Vault Button
            LiquidGlassPillButton(
                text = "Lock Vault Immediately",
                onClick = onLockVault,
                isPrimary = false,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = kryptonColors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "lock_vault_immediately_button"
            )

            Spacer(modifier = Modifier.height(120.dp)) // Space for floating bottom nav bar
        }

        // --- DIALOGS & MODAL OVERLAYS ---

        // Quick PIN Dialog
        if (showQuickPinDialog) {
            var quickPinInputVisible by remember { mutableStateOf(false) }
            var quickPinConfirmVisible by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showQuickPinDialog = false },
                title = { Text("Configure 6-Digit Quick PIN") },
                text = {
                    Column {
                        Text(
                            text = "Set a 6-digit Quick PIN to unlock quickly using the scrambled keypad without entering your full master password.",
                            fontSize = 13.sp,
                            color = kryptonColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LiquidGlassTextField(
                            value = quickPinInput,
                            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) quickPinInput = it },
                            placeholder = "Enter 6-digit Quick PIN",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = if (quickPinInputVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { quickPinInputVisible = !quickPinInputVisible }) {
                                    Icon(
                                        imageVector = if (quickPinInputVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Quick PIN visibility",
                                        tint = kryptonColors.primary
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LiquidGlassTextField(
                            value = quickPinConfirm,
                            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) quickPinConfirm = it },
                            placeholder = "Confirm 6-digit Quick PIN",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = if (quickPinConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { quickPinConfirmVisible = !quickPinConfirmVisible }) {
                                    Icon(
                                        imageVector = if (quickPinConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Confirm PIN visibility",
                                        tint = kryptonColors.primary
                                    )
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (quickPinInput.length != 6) {
                                Toast.makeText(context, "Quick PIN must be exactly 6 digits", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            if (quickPinInput != quickPinConfirm) {
                                Toast.makeText(context, "PINs do not match", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            scope.launch {
                                CryptoManager.setQuickPin(context, quickPinInput.toCharArray())
                                isQuickPinConfigured = true
                                showQuickPinDialog = false
                                quickPinInput = ""
                                quickPinConfirm = ""
                                Toast.makeText(context, "Quick PIN updated successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Save PIN", color = kryptonColors.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showQuickPinDialog = false
                        quickPinInput = ""
                        quickPinConfirm = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Duress PIN Dialog (Suppressed in decoy session)
        if (showDuressPinDialog && !isDecoySession) {
            var duressPinInputVisible by remember { mutableStateOf(false) }
            var duressPinConfirmVisible by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showDuressPinDialog = false },
                title = { Text("Configure Duress PIN") },
                text = {
                    Column {
                        Text(
                            text = "Choose a PIN different from your real Master Password. Entering this PIN at unlock silently loads decoy data.",
                            fontSize = 13.sp,
                            color = kryptonColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LiquidGlassTextField(
                            value = duressPinInput,
                            onValueChange = { duressPinInput = it },
                            placeholder = "Enter Duress PIN",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = if (duressPinInputVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { duressPinInputVisible = !duressPinInputVisible }) {
                                    Icon(
                                        imageVector = if (duressPinInputVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Duress PIN visibility",
                                        tint = kryptonColors.primary
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LiquidGlassTextField(
                            value = duressPinConfirm,
                            onValueChange = { duressPinConfirm = it },
                            placeholder = "Confirm Duress PIN",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = if (duressPinConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { duressPinConfirmVisible = !duressPinConfirmVisible }) {
                                    Icon(
                                        imageVector = if (duressPinConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Confirm PIN visibility",
                                        tint = kryptonColors.primary
                                    )
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (duressPinInput != duressPinConfirm) {
                                Toast.makeText(context, "PINs do not match", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            if (duressPinInput.length < 4) {
                                Toast.makeText(context, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            if (quickPinInput.isNotEmpty() && duressPinInput == quickPinInput) {
                                Toast.makeText(context, "Duress PIN must differ from Master Quick PIN", Toast.LENGTH_SHORT).show()
                                return@TextButton
                            }
                            DuressVaultManager.setDuressPin(context, duressPinInput.toCharArray())
                            duressEnabled = true
                            showDuressPinDialog = false
                            Toast.makeText(context, "Duress Vault configured", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Save PIN", color = kryptonColors.primary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDuressPinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Bluetooth Sync Dialog (Section 4 Specification)
        if (showBluetoothSyncDialog) {
            BluetoothSyncDialog(
                viewModel = viewModel,
                onOpenSelectiveSync = onOpenSelectiveSync,
                onDismiss = { showBluetoothSyncDialog = false }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    val kryptonColors = LocalKryptonColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = kryptonColors.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = kryptonColors.primary,
            letterSpacing = 0.5.sp
        )
    }
}
