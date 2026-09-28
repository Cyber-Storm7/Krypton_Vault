package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.crypto.BiometricUnlockManager
import com.example.crypto.EntropyCalculator
import com.example.crypto.wipe
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.components.LiquidGlassScreenContainer
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.theme.DarkGlassSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricMint
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassIridescentBorderBrush
import com.example.ui.theme.LightGlassSurface
import com.example.ui.theme.LocalKryptonColors
import com.example.ui.theme.PrimaryGradientBrush
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.UnlockInputMode
import com.example.ui.viewmodel.VaultViewModel
import com.example.util.SecurityPreferencesManager

@Composable
fun UnlockScreen(
    viewModel: VaultViewModel? = null,
    authViewModel: AuthViewModel = viewModel(),
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val kryptonColors = LocalKryptonColors.current
    val isDark = kryptonColors.isDark
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState.isUnlocked) {
        if (authState.isUnlocked) {
            viewModel?.refreshState()
            onUnlocked()
        }
    }

    LiquidGlassScreenContainer {
        if (!authState.isInitialized) {
            OnboardingSetupContent(
                authState = authState,
                onSetupVault = { masterPassword, quickPin ->
                    authViewModel.setupVault(masterPassword, quickPin) { success ->
                        if (success) {
                            viewModel?.refreshState()
                            onUnlocked()
                        }
                    }
                }
            )
        } else {
            ActiveUnlockContent(
                authState = authState,
                activity = activity,
                onToggleMode = { authViewModel.toggleUnlockMode() },
                onUnlockMasterPassword = { masterPassword ->
                    authViewModel.unlockWithMasterPassword(masterPassword) { success ->
                        if (success) {
                            viewModel?.refreshState()
                            onUnlocked()
                        }
                    }
                },
                onUnlockQuickPin = { quickPin ->
                    authViewModel.unlockWithQuickPin(quickPin) { success ->
                        if (success) {
                            viewModel?.refreshState()
                            onUnlocked()
                        }
                    }
                },
                onUnlockBiometrics = { cipher ->
                    authViewModel.unlockWithBiometrics(cipher) { success ->
                        if (success) {
                            viewModel?.refreshState()
                            onUnlocked()
                        }
                    }
                }
            )
        }
    }
}

/**
 * Dual-Tier Onboarding Setup:
 * Mandatory 8+ character Root Master Password + Optional 6-digit Quick PIN.
 */
@Composable
private fun OnboardingSetupContent(
    authState: AuthUiState,
    onSetupVault: (CharArray, CharArray?) -> Unit
) {
    val context = LocalContext.current
    val kryptonColors = LocalKryptonColors.current
    val isDark = kryptonColors.isDark

    var masterPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var quickPin by remember { mutableStateOf("") }
    var confirmQuickPin by remember { mutableStateOf("") }
    var quickPinVisible by remember { mutableStateOf(false) }
    var confirmQuickPinVisible by remember { mutableStateOf(false) }

    val entropyReport = remember(masterPassword) {
        val chars = masterPassword.toCharArray()
        val report = EntropyCalculator.analyze(chars)
        chars.wipe()
        report
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Hero Shield Emblem
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(kryptonColors.neuSurface)
                .border(1.dp, kryptonColors.neuBorderGradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security Emblem",
                tint = kryptonColors.primary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "KRYPTON VAULT",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = kryptonColors.textPrimary
        )
        Text(
            text = "Zero-Knowledge Offline Vault Initialization",
            fontSize = 13.sp,
            color = kryptonColors.primary,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "1. Root Master Password (Required)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = kryptonColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "8+ characters alphanumeric. Derives your master key via PBKDF2 (120,000 rounds) + AES-256-GCM.",
                    fontSize = 12.sp,
                    color = kryptonColors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                LiquidGlassTextField(
                    value = masterPassword,
                    onValueChange = { masterPassword = it },
                    placeholder = "Enter Master Password (8+ chars)",
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = kryptonColors.primary
                            )
                        }
                    },
                    testTag = "setup_master_password_input"
                )

                Spacer(modifier = Modifier.height(10.dp))

                LiquidGlassTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirm Master Password",
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle confirm password visibility",
                                tint = kryptonColors.primary
                            )
                        }
                    },
                    testTag = "setup_confirm_password_input"
                )

                if (masterPassword.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    EntropyIndicator(report = entropyReport)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Optional Quick PIN section
                Text(
                    text = "2. Quick PIN (Optional, 6 Digits)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = kryptonColors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Allows daily quick unlocking on scrambled keypad without re-typing long master passwords.",
                    fontSize = 12.sp,
                    color = kryptonColors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                LiquidGlassTextField(
                    value = quickPin,
                    onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) quickPin = it },
                    placeholder = "Optional 6-digit Quick PIN",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (quickPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { quickPinVisible = !quickPinVisible }) {
                            Icon(
                                imageVector = if (quickPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Quick PIN visibility",
                                tint = kryptonColors.primary
                            )
                        }
                    },
                    testTag = "setup_quick_pin_input"
                )

                if (quickPin.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LiquidGlassTextField(
                        value = confirmQuickPin,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) confirmQuickPin = it },
                        placeholder = "Confirm 6-digit Quick PIN",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (confirmQuickPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmQuickPinVisible = !confirmQuickPinVisible }) {
                                Icon(
                                    imageVector = if (confirmQuickPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Confirm Quick PIN visibility",
                                    tint = kryptonColors.primary
                                )
                            }
                        },
                        testTag = "setup_confirm_quick_pin_input"
                    )
                }

                authState.errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = err,
                        color = DangerRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                LiquidGlassPillButton(
                    text = if (authState.isLoading) "Deriving Master Key..." else "Create Master Vault",
                    enabled = !authState.isLoading,
                    onClick = {
                        if (masterPassword.length < 8) {
                            Toast.makeText(context, "Master password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                            return@LiquidGlassPillButton
                        }
                        if (masterPassword != confirmPassword) {
                            Toast.makeText(context, "Master passwords do not match", Toast.LENGTH_SHORT).show()
                            return@LiquidGlassPillButton
                        }
                        if (quickPin.isNotEmpty() && quickPin.length != 6) {
                            Toast.makeText(context, "Quick PIN must be exactly 6 digits", Toast.LENGTH_SHORT).show()
                            return@LiquidGlassPillButton
                        }
                        if (quickPin.isNotEmpty() && quickPin != confirmQuickPin) {
                            Toast.makeText(context, "Quick PINs do not match", Toast.LENGTH_SHORT).show()
                            return@LiquidGlassPillButton
                        }

                        val passChars = masterPassword.toCharArray()
                        val pinChars = if (quickPin.isNotEmpty()) quickPin.toCharArray() else null
                        onSetupVault(passChars, pinChars)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "initialize_vault_button"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Active Unlock Screen:
 * - Auto-launches BiometricPrompt on screen entry.
 * - Displays Scrambled Quick PIN Numpad or Alphanumeric Master Password keyboard.
 * - Liquid Glass pill button seamlessly toggles between modes.
 */
@Composable
private fun ActiveUnlockContent(
    authState: AuthUiState,
    activity: FragmentActivity?,
    onToggleMode: () -> Unit,
    onUnlockMasterPassword: (CharArray) -> Unit,
    onUnlockQuickPin: (CharArray) -> Unit,
    onUnlockBiometrics: (javax.crypto.Cipher) -> Unit
) {
    val context = LocalContext.current
    val kryptonColors = LocalKryptonColors.current
    val isDark = kryptonColors.isDark
    val focusManager = LocalFocusManager.current

    var masterPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var pinDigits by remember { mutableStateOf("") }

    val isScrambleEnabled = remember {
        SecurityPreferencesManager.isScrambleKeypadEnabled(context)
    }

    val scrambledKeypadOrder = remember {
        if (isScrambleEnabled) {
            (0..9).shuffled()
        } else {
            (0..9).toList()
        }
    }

    // Auto-launch Biometric Prompt immediately on screen display
    LaunchedEffect(Unit) {
        if (BiometricUnlockManager.isBiometricUnlockReady(context) && activity != null) {
            BiometricUnlockManager.launchImmediateBiometricPrompt(
                activity = activity,
                onSuccess = { cipher ->
                    onUnlockBiometrics(cipher)
                },
                onFallbackToPin = {
                    // Fallback cleanly to PIN or Password
                },
                onError = {
                    // Ready for manual entry
                }
            )
        }
    }

    // Auto-submit 6-digit PIN when complete
    LaunchedEffect(pinDigits) {
        if (pinDigits.length == 6) {
            val chars = pinDigits.toCharArray()
            pinDigits = ""
            onUnlockQuickPin(chars)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Vault Lock Emblem
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(kryptonColors.neuSurface)
                .border(1.dp, kryptonColors.neuBorderGradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security Shield",
                tint = kryptonColors.primary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "KRYPTON VAULT",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = kryptonColors.textPrimary
        )
        Text(
            text = if (authState.unlockMode == UnlockInputMode.QUICK_PIN) "Enter 6-Digit Quick PIN" else "Enter Root Master Password",
            fontSize = 13.sp,
            color = kryptonColors.primary,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Throttling or Error Message
                if (authState.lockoutSecondsRemaining > 0) {
                    Text(
                        text = "Security Throttle Active (${authState.lockoutSecondsRemaining}s)",
                        color = DangerRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                authState.errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = DangerRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Crossfade(targetState = authState.unlockMode, label = "unlock_mode_transition") { mode ->
                    when (mode) {
                        UnlockInputMode.QUICK_PIN -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                var isPinVisible by remember { mutableStateOf(false) }

                                // 6-digit indicator dots with visibility toggle
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        for (i in 0 until 6) {
                                            val filled = i < pinDigits.length
                                            val digitChar = if (filled) pinDigits[i] else null
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (filled) kryptonColors.primary else kryptonColors.neuInsetSurface
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        brush = if (filled) androidx.compose.ui.graphics.SolidColor(kryptonColors.primary) else kryptonColors.neuInsetBorderGradient,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (filled && isPinVisible && digitChar != null) {
                                                    Text(
                                                        text = digitChar.toString(),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = kryptonColors.onPrimary
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    IconButton(
                                        onClick = { isPinVisible = !isPinVisible },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle PIN visibility",
                                            tint = kryptonColors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Scrambled / Standard PIN Keypad
                                ScrambledKeypad(
                                    digitOrder = scrambledKeypadOrder,
                                    onDigitClick = { digit ->
                                        if (pinDigits.length < 6) {
                                            pinDigits += digit.toString()
                                        }
                                    },
                                    onBackspace = {
                                        if (pinDigits.isNotEmpty()) {
                                            pinDigits = pinDigits.dropLast(1)
                                        }
                                    },
                                    onBiometricClick = {
                                        if (activity != null) {
                                            BiometricUnlockManager.launchImmediateBiometricPrompt(
                                                activity = activity,
                                                onSuccess = { cipher -> onUnlockBiometrics(cipher) },
                                                onFallbackToPin = {},
                                                onError = {}
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        UnlockInputMode.MASTER_PASSWORD -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LiquidGlassTextField(
                                    value = masterPasswordInput,
                                    onValueChange = { masterPasswordInput = it },
                                    placeholder = "Enter 8+ char Master Password",
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (masterPasswordInput.isNotEmpty()) {
                                                val chars = masterPasswordInput.toCharArray()
                                                masterPasswordInput = ""
                                                onUnlockMasterPassword(chars)
                                            }
                                        }
                                    ),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle visibility",
                                                tint = kryptonColors.primary
                                            )
                                        }
                                    },
                                    testTag = "master_password_unlock_input"
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                LiquidGlassPillButton(
                                    text = if (authState.isLoading) "Verifying..." else "Unlock Vault",
                                    enabled = !authState.isLoading && masterPasswordInput.isNotEmpty(),
                                    onClick = {
                                        focusManager.clearFocus()
                                        val chars = masterPasswordInput.toCharArray()
                                        masterPasswordInput = ""
                                        onUnlockMasterPassword(chars)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "unlock_master_password_button"
                                )

                                if (activity != null && authState.isBiometricAvailable) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    IconButton(
                                        onClick = {
                                            BiometricUnlockManager.launchImmediateBiometricPrompt(
                                                activity = activity,
                                                onSuccess = { cipher -> onUnlockBiometrics(cipher) },
                                                onFallbackToPin = {},
                                                onError = {}
                                            )
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(kryptonColors.neuSurface)
                                            .border(1.dp, kryptonColors.neuBorderGradient, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Unlock with Biometrics",
                                            tint = kryptonColors.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Action Button: "Use Master Password" / "Use PIN"
                val toggleLabel = if (authState.unlockMode == UnlockInputMode.QUICK_PIN) {
                    "Use Master Password"
                } else {
                    if (authState.isQuickPinConfigured) "Use Quick PIN" else "Root Master Password Active"
                }

                val canToggle = authState.unlockMode == UnlockInputMode.QUICK_PIN || authState.isQuickPinConfigured

                if (canToggle) {
                    LiquidGlassPillButton(
                        text = toggleLabel,
                        isPrimary = false,
                        onClick = onToggleMode,
                        leadingIcon = {
                            Icon(
                                imageVector = if (authState.unlockMode == UnlockInputMode.QUICK_PIN) Icons.Default.Key else Icons.Default.Pin,
                                contentDescription = null,
                                tint = kryptonColors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "toggle_auth_mode_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrambledKeypad(
    digitOrder: List<Int>,
    onDigitClick: (Int) -> Unit,
    onBackspace: () -> Unit,
    onBiometricClick: () -> Unit
) {
    val kryptonColors = LocalKryptonColors.current
    val isDark = kryptonColors.isDark
    val buttonBg = kryptonColors.neuSurface
    val buttonBorder = kryptonColors.neuBorderGradient

    val row1 = digitOrder.subList(0, 3)
    val row2 = digitOrder.subList(3, 6)
    val row3 = digitOrder.subList(6, 9)
    val lastDigit = digitOrder[9]

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            row1.forEach { digit ->
                KeypadDigitButton(digit = digit, onClick = { onDigitClick(digit) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            row2.forEach { digit ->
                KeypadDigitButton(digit = digit, onClick = { onDigitClick(digit) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            row3.forEach { digit ->
                KeypadDigitButton(digit = digit, onClick = { onDigitClick(digit) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Biometric quick button
            val kryptonColors = LocalKryptonColors.current
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(buttonBg)
                    .border(1.dp, buttonBorder, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = kryptonColors.primary),
                        onClick = onBiometricClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Biometric Unlock",
                    tint = kryptonColors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // 10th digit
            KeypadDigitButton(digit = lastDigit, onClick = { onDigitClick(lastDigit) })

            // Backspace button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(buttonBg)
                    .border(1.dp, buttonBorder, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = DangerRed),
                        onClick = onBackspace
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = kryptonColors.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun KeypadDigitButton(
    digit: Int,
    onClick: () -> Unit
) {
    val kryptonColors = LocalKryptonColors.current
    val surfaceColor = kryptonColors.neuSurface
    val borderColor = kryptonColors.neuBorderGradient

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(surfaceColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = kryptonColors.primary),
                onClick = onClick
            )
            .testTag("keypad_digit_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = kryptonColors.textPrimary
        )
    }
}

@Composable
private fun EntropyIndicator(report: EntropyCalculator.EntropyReport) {
    val kryptonColors = LocalKryptonColors.current
    val (color, label) = when (report.strengthLevel) {
        EntropyCalculator.PasswordStrengthLevel.VERY_WEAK -> DangerRed to "Very Weak"
        EntropyCalculator.PasswordStrengthLevel.WEAK -> DangerRed to "Weak"
        EntropyCalculator.PasswordStrengthLevel.FAIR -> WarningAmber to "Fair"
        EntropyCalculator.PasswordStrengthLevel.STRONG -> EmeraldPrimary to "Strong"
        EntropyCalculator.PasswordStrengthLevel.VERY_STRONG -> ElectricMint to "Very Strong"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Entropy: ${report.entropyBits.toInt()} bits",
                fontSize = 12.sp,
                color = kryptonColors.primary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (report.entropyBits / 100.0).coerceIn(0.0, 1.0).toFloat() },
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(percent = 50))
        )
    }
}

