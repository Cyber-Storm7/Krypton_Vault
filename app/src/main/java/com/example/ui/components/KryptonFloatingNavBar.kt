package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalKryptonColors

enum class KryptonNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    VAULT(
        title = "Vault",
        selectedIcon = Icons.Filled.Security,
        unselectedIcon = Icons.Outlined.Security,
        testTag = "nav_tab_vault"
    ),
    GENERATOR(
        title = "Generator",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_tab_generator"
    ),
    HEALTH(
        title = "Health",
        selectedIcon = Icons.Filled.HealthAndSafety,
        unselectedIcon = Icons.Outlined.HealthAndSafety,
        testTag = "nav_tab_health"
    ),
    SETTINGS(
        title = "Settings",
        selectedIcon = Icons.Filled.Tune,
        unselectedIcon = Icons.Outlined.Tune,
        testTag = "nav_tab_settings"
    )
}

/**
 * Reference Expanding-Pill Mobile Navigation Bar:
 * Exactly inspired by the reference design dock.
 * Unselected items show clean icons; selected item expands into a soft tinted stadium pill with Icon + Text.
 * Centered home indicator bar line rests at the base.
 * Retains exact 4 items: Vault, Generator, Health, Settings.
 */
@Composable
fun KryptonFloatingNavBar(
    selectedTab: KryptonNavTab,
    onTabSelected: (KryptonNavTab) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: Any? = null // For backwards compatibility
) {
    val kryptonColors = LocalKryptonColors.current
    val isDark = kryptonColors.isDark
    val haptic = LocalHapticFeedback.current
    val barShape = remember { RoundedCornerShape(32.dp) }
    val tabs = remember { KryptonNavTab.entries }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer Floating Capsule Dock
        val barElevation = if (isDark) 0.dp else 4.dp
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (barElevation > 0.dp) {
                        Modifier.shadow(elevation = barElevation, shape = barShape)
                    } else Modifier
                )
                .clip(barShape)
                .background(kryptonColors.neuSurface)
                .border(
                    width = 1.dp,
                    brush = kryptonColors.neuBorderGradient,
                    shape = barShape
                )
                .padding(top = 8.dp, bottom = 6.dp, start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row of Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pillShape = remember { RoundedCornerShape(percent = 50) }
                val activePillBg = remember(kryptonColors, isDark) {
                    if (isDark) {
                        Color(0xFF1E1E22)
                    } else {
                        Color(0xFFE5E7EB)
                    }
                }
                val activePillBorder = remember(kryptonColors, isDark) {
                    if (isDark) {
                        if (kryptonColors.isMonochrome) Color(0xFF3F3F46) else kryptonColors.primary.copy(alpha = 0.5f)
                    } else {
                        if (kryptonColors.isMonochrome) Color(0xFFD1D5DB) else kryptonColors.primary.copy(alpha = 0.6f)
                    }
                }
                val activeContentColor = remember(kryptonColors, isDark) {
                    if (kryptonColors.isMonochrome) {
                        if (isDark) Color.White else Color(0xFF131417)
                    } else {
                        kryptonColors.primary
                    }
                }
                val unselectedContentColor = remember(isDark) {
                    if (isDark) Color(0xFF8E92A0) else Color(0xFF6B7280)
                }

                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab

                    Box(
                        modifier = Modifier
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                            .clip(pillShape)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(activePillBg)
                                        .border(1.dp, activePillBorder, pillShape)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable(
                                role = Role.Tab,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = true,
                                    color = activeContentColor.copy(alpha = 0.2f)
                                ),
                                onClick = {
                                    if (!isSelected) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onTabSelected(tab)
                                    }
                                }
                            )
                            .padding(
                                horizontal = if (isSelected) 16.dp else 12.dp,
                                vertical = 8.dp
                            )
                            .testTag(tab.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) activeContentColor else unselectedContentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeContentColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sleek reference home indicator bar line
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDark) Color(0xFF333642) else Color(0xFFCBD2DE))
            )
        }
    }
}
