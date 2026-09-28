package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.EntropyCalculator
import com.example.crypto.PasswordGenerator
import com.example.crypto.wipe
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.components.LiquidGlassSwitch
import com.example.ui.theme.LocalKryptonColors
import com.example.util.ClipboardHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PasswordGeneratorBottomSheet(
    onDismiss: () -> Unit,
    onPasswordSelected: (CharArray) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val kryptonColors = LocalKryptonColors.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Random, 1 = Passphrase

    // Random config
    var length by remember { mutableFloatStateOf(18f) }
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
    }

    val lengthInt = length.toInt()
    val wordCountInt = wordCount.toInt()
    LaunchedEffect(selectedTab, lengthInt, incUpper, incLower, incDigits, incSymbols, excludeAmbiguous, wordCountInt, delimiter, capitalizeWords) {
        regenerate()
    }

    val entropyReport = remember(generatedChars) {
        EntropyCalculator.analyze(generatedChars)
    }

    ModalBottomSheet(
        onDismissRequest = {
            generatedChars.wipe()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = kryptonColors.neuBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Password Generator",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = kryptonColors.textPrimary
                )
                IconButton(
                    onClick = regenerate,
                    modifier = Modifier.testTag("regenerate_password_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Regenerate",
                        tint = kryptonColors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Output Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(kryptonColors.neuInsetSurface)
                    .border(1.dp, kryptonColors.neuInsetBorderGradient, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = String(generatedChars),
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = kryptonColors.textPrimary,
                        modifier = Modifier.testTag("generated_password_display")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val strengthColor = if (kryptonColors.isMonochrome) kryptonColors.primary else Color(entropyReport.strengthLevel.colorHex)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${entropyReport.strengthLevel.label} • ${entropyReport.entropyBits} bits",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = strengthColor
                        )
                        Text(
                            text = "Crack time: ${entropyReport.crackTimeFormatted}",
                            fontSize = 11.sp,
                            color = kryptonColors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    val progress = (entropyReport.entropyBits / 100.0).toFloat().coerceIn(0.05f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(percent = 50)),
                        color = strengthColor,
                        trackColor = strengthColor.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selector Pill Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(kryptonColors.neuInsetSurface)
                    .border(1.dp, kryptonColors.neuInsetBorderGradient, RoundedCornerShape(percent = 50))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Random String", "Diceware Passphrase").forEachIndexed { index, title ->
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
                            fontSize = 13.sp,
                            color = if (isSelected) kryptonColors.onPrimary else kryptonColors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Random String controls
                Text(
                    text = "Length: ${length.toInt()} characters",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = kryptonColors.textPrimary
                )
                Slider(
                    value = length,
                    onValueChange = { length = kotlin.math.round(it) },
                    valueRange = 8f..64f,
                    steps = 55,
                    colors = SliderDefaults.colors(
                        thumbColor = kryptonColors.primary,
                        activeTrackColor = kryptonColors.primary,
                        inactiveTrackColor = kryptonColors.primary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("password_length_slider")
                )

                Spacer(modifier = Modifier.height(6.dp))

                SwitchRow(label = "Uppercase Letters (A-Z)", checked = incUpper, onCheckedChange = { incUpper = it })
                SwitchRow(label = "Lowercase Letters (a-z)", checked = incLower, onCheckedChange = { incLower = it })
                SwitchRow(label = "Numbers (0-9)", checked = incDigits, onCheckedChange = { incDigits = it })
                SwitchRow(label = "Symbols (!@#$%...)", checked = incSymbols, onCheckedChange = { incSymbols = it })
                SwitchRow(label = "Exclude Ambiguous (l, 1, O, 0)", checked = excludeAmbiguous, onCheckedChange = { excludeAmbiguous = it })

            } else {
                // Passphrase controls
                Text(
                    text = "Words: ${wordCount.toInt()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = kryptonColors.textPrimary
                )
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

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Word Delimiter",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = kryptonColors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("-" to "Hyphen", "_" to "Underscore", "." to "Period", " " to "Space").forEach { (sym, name) ->
                        val selected = delimiter == sym
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .then(
                                    if (selected) Modifier.background(kryptonColors.primary)
                                    else Modifier.background(kryptonColors.neuSurface)
                                )
                                .border(
                                    width = 1.dp,
                                    brush = if (selected) androidx.compose.ui.graphics.SolidColor(kryptonColors.primary) else kryptonColors.neuBorderGradient,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { delimiter = sym },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) kryptonColors.onPrimary else kryptonColors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                SwitchRow(label = "Capitalize Each Word", checked = capitalizeWords, onCheckedChange = { capitalizeWords = it })
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions: Use Password or Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LiquidGlassPillButton(
                    text = "Copy",
                    onClick = {
                        ClipboardHelper.copySensitive(context, "Password", generatedChars)
                        Toast.makeText(context, "Copied! Auto-clearing in 30s", Toast.LENGTH_SHORT).show()
                    },
                    isPrimary = false,
                    leadingIcon = {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_generated_password_button")
                )

                LiquidGlassPillButton(
                    text = "Use Password",
                    onClick = {
                        val pass = generatedChars.clone()
                        generatedChars.wipe()
                        onPasswordSelected(pass)
                        onDismiss()
                    },
                    isPrimary = true,
                    leadingIcon = {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("use_generated_password_button")
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val kryptonColors = LocalKryptonColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = kryptonColors.textPrimary
        )
        LiquidGlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
