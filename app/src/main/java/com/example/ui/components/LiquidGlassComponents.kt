package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalKryptonColors
import kotlinx.coroutines.launch

/**
 * Authentic Neumorphic Soft-UI Card:
 * Molded tactile surface emerging organically from the background plane.
 * In Extruded (Convex) mode: Casts deep shadow on bottom-right and soft highlight on top-left.
 * In Inset (Concave) mode: Appears sunken into the canvas with recessed border gradient.
 */
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 4.dp,
    isInset: Boolean = false,
    content: @Composable () -> Unit
) {
    val kryptonColors = LocalKryptonColors.current
    val isDark = kryptonColors.isDark

    val surfaceColor = remember(kryptonColors, isInset) {
        if (isInset) kryptonColors.neuInsetSurface else kryptonColors.neuSurface
    }

    val borderBrush = remember(kryptonColors, isInset) {
        if (isInset) kryptonColors.neuInsetBorderGradient else kryptonColors.neuBorderGradient
    }

    // In dark mode on OLED black, elevation shadows are completely invisible and cause GPU jank.
    // Zero elevation in dark mode keeps 60/120fps smooth scrolling with clean crisp borders.
    val effectiveElevation = if (isInset || isDark) 0.dp else elevation

    Box(
        modifier = modifier
            .then(
                if (effectiveElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = effectiveElevation,
                        shape = shape
                    )
                } else Modifier
            )
            .clip(shape)
            .background(surfaceColor)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = shape
            )
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * Backward-compatible alias for existing screens calling LiquidGlassCard.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    glowColor: Color = Color.Transparent,
    content: @Composable () -> Unit
) {
    NeumorphicCard(
        modifier = modifier,
        shape = shape,
        elevation = elevation,
        content = content
    )
}

/**
 * Neumorphic Tactile Pill Button:
 * Embossed 3D button that depresses seamlessly when tapped (tactile physical press state).
 * Supports solid primary accent or extruded secondary surface.
 */
@Composable
fun NeumorphicPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    testTag: String = "neumorphic_pill_button"
) {
    val kryptonColors = LocalKryptonColors.current
    val isDark = kryptonColors.isDark
    val shape = remember { RoundedCornerShape(percent = 50) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed || isDark) 0.dp else if (isPrimary) 3.dp else 2.dp,
        animationSpec = spring(stiffness = 600f),
        label = "btn_elevation"
    )

    val backgroundColor = remember(isPrimary, isPressed, kryptonColors) {
        if (isPrimary) {
            kryptonColors.primary
        } else {
            if (isPressed) kryptonColors.neuInsetSurface else kryptonColors.neuSurface
        }
    }

    val borderBrush = remember(isPrimary, isPressed, kryptonColors) {
        if (isPrimary) {
            SolidColor(kryptonColors.primary)
        } else {
            if (isPressed) kryptonColors.neuInsetBorderGradient else kryptonColors.neuBorderGradient
        }
    }

    val contentColor = remember(isPrimary, kryptonColors) {
        if (isPrimary) {
            kryptonColors.onPrimary
        } else {
            kryptonColors.primary
        }
    }

    val rememberClick = remember(onClick) { onClick }

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(52.dp)
            .then(
                if (animatedElevation > 0.dp) {
                    Modifier.shadow(elevation = animatedElevation, shape = shape)
                } else Modifier
            )
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple(color = if (isPrimary) kryptonColors.onPrimary else kryptonColors.primary),
                onClick = rememberClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) contentColor else contentColor.copy(alpha = 0.4f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
    }
}

/**
 * Backward-compatible alias for existing screens calling LiquidGlassPillButton.
 */
@Composable
fun LiquidGlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    testTag: String = "minimalist_pill_button"
) {
    NeumorphicPillButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        isPrimary = isPrimary,
        enabled = enabled,
        leadingIcon = leadingIcon,
        testTag = testTag
    )
}

/**
 * Neumorphic Inset Text Field:
 * Recessed concave groove sculpted into the surface.
 * Features an inverted bevel border and soft inner shadow with auto-scroll into view hook.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NeumorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    testTag: String = "neumorphic_text_field"
) {
    val kryptonColors = LocalKryptonColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isFocused) {
        if (isFocused) {
            coroutineScope.launch {
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    val shape = remember { RoundedCornerShape(16.dp) }
    val surfaceColor = remember(kryptonColors) { kryptonColors.neuInsetSurface }

    val borderBrush = remember(kryptonColors, isFocused) {
        if (isFocused) {
            SolidColor(kryptonColors.primary)
        } else {
            kryptonColors.neuInsetBorderGradient
        }
    }

    val textColor = remember(kryptonColors) { kryptonColors.textPrimary }
    val placeholderColor = remember(kryptonColors) { kryptonColors.textSecondary }
    val cursorBrush = remember(kryptonColors) { SolidColor(kryptonColors.primary) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .testTag(testTag)
            .bringIntoViewRequester(bringIntoViewRequester)
            .height(54.dp)
            .clip(shape)
            .background(surfaceColor)
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = shape
            ),
        textStyle = TextStyle(
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        ),
        cursorBrush = cursorBrush,
        interactionSource = interactionSource,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = placeholderColor,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }
    )
}

/**
 * Backward-compatible alias for existing screens calling LiquidGlassTextField.
 */
@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    testTag: String = "minimalist_text_field"
) {
    NeumorphicTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        testTag = testTag
    )
}

/**
 * Neumorphic Tactile Switch:
 * Recessed sunken capsule track with extruded 3D sliding thumb button.
 */
@Composable
fun NeumorphicSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "neumorphic_switch"
) {
    val kryptonColors = LocalKryptonColors.current

    val width = 52.dp
    val height = 30.dp
    val thumbSize = 24.dp
    val padding = 3.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) width - thumbSize - padding else padding,
        animationSpec = spring(stiffness = 500f),
        label = "neu_thumb_offset"
    )

    val capsuleShape = remember { RoundedCornerShape(percent = 50) }
    val thumbShape = remember { CircleShape }

    val trackColor by animateColorAsState(
        targetValue = if (checked) kryptonColors.primary else kryptonColors.neuInsetSurface,
        label = "neu_track_color"
    )

    val trackBorder = if (checked) SolidColor(kryptonColors.primary) else kryptonColors.neuInsetBorderGradient

    val thumbBg = if (checked) kryptonColors.onPrimary else kryptonColors.neuSurfaceElevated
    val rememberToggle = remember(onCheckedChange, checked) { { onCheckedChange(!checked) } }

    val isDark = kryptonColors.isDark
    val thumbElevation = if (isDark) 0.dp else 2.dp

    Box(
        modifier = modifier
            .testTag(testTag)
            .size(width, height)
            .clip(capsuleShape)
            .background(trackColor)
            .border(width = 1.dp, brush = trackBorder, shape = capsuleShape)
            .clickable(
                enabled = enabled,
                role = Role.Switch,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = rememberToggle
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .then(
                    if (thumbElevation > 0.dp) {
                        Modifier.shadow(
                            elevation = thumbElevation,
                            shape = thumbShape
                        )
                    } else Modifier
                )
                .clip(thumbShape)
                .background(thumbBg)
                .border(
                    width = 1.dp,
                    brush = kryptonColors.neuBorderGradient,
                    shape = thumbShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = kryptonColors.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * Backward-compatible alias for existing screens calling LiquidGlassSwitch.
 */
@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "minimalist_switch"
) {
    NeumorphicSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        testTag = testTag
    )
}

/**
 * Neumorphic Screen Container:
 * Full-bleed cohesive canvas wrapper using neuBackground for consistent tactile depth.
 */
@Composable
fun NeumorphicScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val kryptonColors = LocalKryptonColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(kryptonColors.neuBackground)
    ) {
        content()
    }
}

/**
 * Backward-compatible alias for existing screens calling LiquidGlassScreenContainer.
 */
@Composable
fun LiquidGlassScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    NeumorphicScreenContainer(
        modifier = modifier,
        content = content
    )
}
