package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FlaggedVaultItem
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.KryptonAmber
import com.example.ui.theme.KryptonGreen
import com.example.ui.theme.KryptonRed
import com.example.ui.theme.LocalKryptonColors
import com.example.ui.viewmodel.VaultViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SecurityAuditScreen(
    viewModel: VaultViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToEdit: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val kryptonColors = LocalKryptonColors.current

    LaunchedEffect(Unit) {
        viewModel.runSecurityAudit()
    }

    Scaffold(
        containerColor = kryptonColors.neuBackground,
        topBar = {
            TopAppBar(
                title = { Text("Zero-Knowledge Security Audit", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = kryptonColors.textPrimary) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = kryptonColors.primary)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.runSecurityAudit() },
                        modifier = Modifier.testTag("refresh_security_audit_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Audit", tint = kryptonColors.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = kryptonColors.neuBackground)
            )
        }
    ) { innerPadding ->
        if (state.isAuditing && state.securityAudit == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = kryptonColors.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyzing Shannon Entropy & Health...",
                        fontSize = 14.sp,
                        color = kryptonColors.textSecondary
                    )
                }
            }
        } else {
            val report = state.securityAudit
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 160.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isAuditing) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(percent = 50)),
                            color = kryptonColors.primary,
                            trackColor = kryptonColors.primary.copy(alpha = 0.2f)
                        )
                    }
                }
                // Summary Metrics
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AuditMetricCard(
                                title = "Total Items",
                                count = report?.totalCount ?: 0,
                                icon = Icons.Default.Security,
                                tint = kryptonColors.primary,
                                modifier = Modifier.weight(1f)
                            )
                            AuditMetricCard(
                                title = "Weak Passwords",
                                count = report?.weakCount ?: 0,
                                icon = Icons.Default.Warning,
                                tint = if (kryptonColors.isMonochrome) Color(0xFFE4E4E7) else KryptonRed,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AuditMetricCard(
                                title = "Reused Passwords",
                                count = report?.reusedCount ?: 0,
                                icon = Icons.Default.Repeat,
                                tint = if (kryptonColors.isMonochrome) Color(0xFFCBD5E1) else KryptonAmber,
                                modifier = Modifier.weight(1f)
                            )
                            AuditMetricCard(
                                title = "Old (>180d)",
                                count = report?.oldCount ?: 0,
                                icon = Icons.Default.History,
                                tint = if (kryptonColors.isMonochrome) Color(0xFFA1A1AA) else Color(0xFFA855F7),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Overall Health Status Banner
                item {
                    val isAllGood = (report?.weakCount ?: 0) == 0 && (report?.reusedCount ?: 0) == 0
                    val bannerIconColor = if (isAllGood) {
                        if (kryptonColors.isMonochrome) kryptonColors.primary else KryptonGreen
                    } else {
                        if (kryptonColors.isMonochrome) Color(0xFFE4E4E7) else KryptonRed
                    }

                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(kryptonColors.neuInsetSurface)
                                    .border(1.dp, kryptonColors.neuInsetBorderGradient, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAllGood) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = bannerIconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAllGood) "Vault Health Excellent" else "Action Recommended",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = kryptonColors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isAllGood) "All passwords meet cryptographic Shannon entropy thresholds and are uniquely diversified."
                                    else "Resolve weak, reused, or expired credentials below to maximize zero-knowledge security.",
                                    fontSize = 12.sp,
                                    color = kryptonColors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Section header
                item {
                    Text(
                        text = "Flagged Accounts (${report?.flaggedItems?.size ?: 0})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = kryptonColors.textPrimary
                    )
                }

                if (report?.flaggedItems.isNullOrEmpty()) {
                    item {
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No compromised or vulnerable items detected 🎉",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = kryptonColors.textSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(report!!.flaggedItems, key = { it.id }) { flaggedItem ->
                        FlaggedItemCard(
                            item = flaggedItem,
                            onFixClick = { onNavigateToEdit(flaggedItem.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditMetricCard(
    title: String,
    count: Int,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val kryptonColors = LocalKryptonColors.current
    LiquidGlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = kryptonColors.textSecondary
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(kryptonColors.neuInsetSurface)
                        .border(1.dp, kryptonColors.neuInsetBorderGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = kryptonColors.textPrimary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlaggedItemCard(
    item: FlaggedVaultItem,
    onFixClick: () -> Unit
) {
    val kryptonColors = LocalKryptonColors.current
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onFixClick),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = kryptonColors.textPrimary
                    )
                    if (item.username.isNotBlank()) {
                        Text(
                            text = item.username,
                            fontSize = 13.sp,
                            color = kryptonColors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(kryptonColors.primary)
                        .clickable(onClick = onFixClick)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Fix",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = kryptonColors.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (item.isWeak) {
                    AuditRiskBadge(
                        text = "Weak (${item.entropyBits} bits)",
                        accentColor = if (kryptonColors.isMonochrome) Color(0xFFE4E4E7) else KryptonRed,
                        onClick = onFixClick
                    )
                }
                if (item.isReused) {
                    AuditRiskBadge(
                        text = "Reused Password",
                        accentColor = if (kryptonColors.isMonochrome) Color(0xFFCBD5E1) else KryptonAmber,
                        onClick = onFixClick
                    )
                }
                if (item.isOld) {
                    AuditRiskBadge(
                        text = "Modified > 180 Days Ago",
                        accentColor = if (kryptonColors.isMonochrome) Color(0xFFA1A1AA) else Color(0xFFA855F7),
                        onClick = onFixClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AuditRiskBadge(
    text: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    val kryptonColors = LocalKryptonColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(kryptonColors.neuInsetSurface)
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = accentColor
        )
    }
}
