package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.EntropyCalculator
import com.example.crypto.PasswordGenerator
import com.example.crypto.wipe
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.components.LiquidGlassScreenContainer
import com.example.ui.components.LiquidGlassSwitch
import com.example.ui.theme.DarkGlassSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricMint
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassIridescentBorderBrush
import com.example.ui.theme.LightGlassSurface
import com.example.ui.theme.LocalKryptonColors
import com.example.ui.theme.PrimaryGradientBrush
import com.example.ui.theme.WarningAmber
import com.example.util.ClipboardHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratorScreen(
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val kryptonColors = LocalKryptonColors.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Random, 1 = Passphrase

    // Random config
    var length by remember { mutableFloatStateOf(20f) }
    var incUpper by remember { mutableStateOf(true) }
    var incLower by remember { mutableStateOf(true) }
    var incDigits by remember { mutableStateOf(true) }
    var incSymbols by remember { mutableStateOf(true) }
    var excludeAmbiguous by remember { mutableStateOf(true) }

    // Passphrase config
    var wordCount by remember { mutableFloatStateOf(4f) }
    var delimiter by remember { mutableStateOf("-") }
    var capitalizeWords by remember { mutableStateOf(true) }

    var generatedChars by remember { mutableStateOf(CharArray(0)) }
    var copiedRecently by remember { mutableStateOf(false) }

    val regenerate = {
        generatedChars.wipe()
        if (selectedTab == 0) {
            val config = PasswordGenerator.RandomPasswordConfig(
                length = length.toInt(),
                includeUppercase = incUpper,
                includeLowercase = incLower,
                includeDigits = incDigits,
                includeSymbols = incSymbols,
                excludeAmbiguous = excludeAmbiguous
            )
            generatedChars = PasswordGenerator.generatePassword(config)
        } else {
            val config = PasswordGenerator.PassphraseConfig(
                wordCount = wordCount.toInt(),
                delimiter = delimiter,
                capitalizeWords = capitalizeWords
            )
            generatedChars = PasswordGenerator.generatePassphrase(config)
        }
        copiedRecently = false
    }

    val lengthInt = length.toInt()
    val wordCountInt = wordCount.toInt()
    LaunchedEffect(selectedTab, lengthInt, incUpper, incLower, incDigits, incSymbols, excludeAmbiguous, wordCountInt, delimiter, capitalizeWords) {
        regenerate()
    }

    val entropyReport = remember(generatedChars) {
        EntropyCalculator.analyze(generatedChars)
    }

    LiquidGlassScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(kryptonColors.neuSurface)
                        .border(1.dp, kryptonColors.neuBorderGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = kryptonColors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Generator",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = kryptonColors.textPrimary
                    )
                    Text(
                        text = "Cryptographically Secure Entropy",
                        fontSize = 13.sp,
                        color = kryptonColors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mode Selector Pill Tabs (Random Password vs. Passphrase)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(kryptonColors.neuInsetSurface)
                    .border(1.dp, kryptonColors.neuInsetBorderGradient, RoundedCornerShape(percent = 50))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Password", "Passphrase").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    val tabModifier = if (isSelected) {
                        Modifier
                            .background(kryptonColors.primary)
                            .shadow(2.dp, RoundedCornerShape(percent = 50))
                    } else {
                        Modifier.background(Color.Transparent)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(percent = 50))
                            .then(tabModifier)
                            .clickable { selectedTab = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isSelected) kryptonColors.onPrimary else kryptonColors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Primary Liquid Glass Card displaying generated password
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val passString = String(generatedChars)

                    // Inset display window for password
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(kryptonColors.neuInsetSurface)
                            .border(1.dp, kryptonColors.neuInsetBorderGradient, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = passString,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (passString.length > 28) 17.sp else 21.sp,
                            color = kryptonColors.textPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generated_password_display")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Entropy Score & Strength Bar
                    val (strengthColor, strengthLabel) = when (entropyReport.strengthLevel) {
                        EntropyCalculator.PasswordStrengthLevel.VERY_WEAK -> DangerRed to "Very Weak"
                        EntropyCalculator.PasswordStrengthLevel.WEAK -> DangerRed to "Weak"
                        EntropyCalculator.PasswordStrengthLevel.FAIR -> WarningAmber to "Fair"
                        EntropyCalculator.PasswordStrengthLevel.STRONG -> EmeraldPrimary to "Strong"
                        EntropyCalculator.PasswordStrengthLevel.VERY_STRONG -> ElectricMint to "Very Strong"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${entropyReport.entropyBits.toInt()} bits entropy",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = kryptonColors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = strengthLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = strengthColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (entropyReport.entropyBits / 128.0).coerceIn(0.0, 1.0).toFloat() },
                        color = strengthColor,
                        trackColor = strengthColor.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(percent = 50))
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action buttons: Copy & Regenerate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidGlassPillButton(
                            text = if (copiedRecently) "Copied!" else "Copy to Clipboard",
                            onClick = {
                                ClipboardHelper.copySensitive(context, "Password", generatedChars)
                                copiedRecently = true
                                Toast.makeText(context, "Copied (auto-clears in 30s)", Toast.LENGTH_SHORT).show()
                            },
                            isPrimary = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = if (copiedRecently) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = kryptonColors.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            testTag = "copy_password_button"
                        )

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(kryptonColors.neuSurface)
                                .border(1.dp, kryptonColors.neuBorderGradient, RoundedCornerShape(percent = 50))
                                .clickable { regenerate() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate",
                                tint = kryptonColors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Controls & Customization Card
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (selectedTab == 0) {
                        // RANDOM PASSWORD CONFIG
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Password Length",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = kryptonColors.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(kryptonColors.primaryContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${length.toInt()} chars",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = kryptonColors.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = length,
                            onValueChange = { length = kotlin.math.round(it) },
                            valueRange = 8f..64f,
                            steps = 55,
                            colors = SliderDefaults.colors(
                                thumbColor = kryptonColors.primary,
                                activeTrackColor = kryptonColors.primary,
                                inactiveTrackColor = kryptonColors.primary.copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        GeneratorToggleRow(
                            label = "Uppercase (A-Z)",
                            checked = incUpper,
                            onCheckedChange = { incUpper = it }
                        )
                        GeneratorToggleRow(
                            label = "Lowercase (a-z)",
                            checked = incLower,
                            onCheckedChange = { incLower = it }
                        )
                        GeneratorToggleRow(
                            label = "Digits (0-9)",
                            checked = incDigits,
                            onCheckedChange = { incDigits = it }
                        )
                        GeneratorToggleRow(
                            label = "Symbols (!@#$%^&*)",
                            checked = incSymbols,
                            onCheckedChange = { incSymbols = it }
                        )
                        GeneratorToggleRow(
                            label = "Exclude Ambiguous (l, 1, O, 0)",
                            checked = excludeAmbiguous,
                            onCheckedChange = { excludeAmbiguous = it }
                        )
                    } else {
                        // PASSPHRASE CONFIG
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Word Count",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = kryptonColors.textPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(kryptonColors.primaryContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${wordCount.toInt()} words",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = kryptonColors.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = wordCount,
                            onValueChange = { wordCount = kotlin.math.round(it) },
                            valueRange = 3f..8f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                thumbColor = kryptonColors.primary,
                                activeTrackColor = kryptonColors.primary,
                                inactiveTrackColor = kryptonColors.primary.copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        GeneratorToggleRow(
                            label = "Capitalize Each Word",
                            checked = capitalizeWords,
                            onCheckedChange = { capitalizeWords = it }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Word Delimiter",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = kryptonColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("-" to "Hyphen", "_" to "Underscore", "." to "Period", " " to "Space").forEach { (del, _) ->
                                val selected = delimiter == del
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .then(
                                            if (selected) Modifier.background(kryptonColors.primary)
                                            else Modifier.background(kryptonColors.neuSurface)
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = if (selected) androidx.compose.ui.graphics.SolidColor(kryptonColors.primary) else kryptonColors.neuBorderGradient,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { delimiter = del },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (del == " ") "Space" else del,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (selected) kryptonColors.onPrimary else kryptonColors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(160.dp)) // Space for floating bottom nav bar
        }
    }
}

@Composable
private fun GeneratorToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val kryptonColors = LocalKryptonColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = kryptonColors.textPrimary
        )
        LiquidGlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
