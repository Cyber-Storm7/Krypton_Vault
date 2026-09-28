package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VaultItemDecrypted
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.theme.DarkGlassSurfaceElevated
import com.example.ui.theme.DarkOledBackground
import com.example.ui.theme.ElectricMint
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GlassIridescentBorderBrush
import com.example.ui.theme.LightGlassSurfaceElevated
import com.example.ui.theme.LightPearlescentBackground
import com.example.ui.theme.LocalKryptonColors
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.VaultViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncItemDiffCategory {
    NEW,
    UPDATED,
    UNCHANGED
}

enum class ConflictResolutionChoice {
    KEEP_MINE,
    USE_THEIRS
}

data class SyncDiffEntry(
    val id: String,
    val title: String,
    val username: String,
    val category: SyncItemDiffCategory,
    val localItem: VaultItemDecrypted?,
    val remoteItem: VaultItemDecrypted,
    val localTimestamp: Long,
    val remoteTimestamp: Long
)

/**
 * 4.2 Visual Conflict Resolution Screen (SelectiveSyncScreen.kt).
 * Displays a full-screen Liquid Glass Diff preview of incoming sync data:
 * - Categorizes items: New (green badge), Updated (amber badge), Unchanged.
 * - Side-by-Side Conflict Resolution comparing Local vs. Remote timestamps.
 * - Interactive radio selector per conflict: [ Keep Mine ] vs [ Use Theirs ].
 * - Bottom sticky Liquid Glass button: "Apply Selected Changes" to commit verified merges.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectiveSyncScreen(
    remoteItems: List<VaultItemDecrypted>,
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kryptonColors = LocalKryptonColors.current

    val newBadgeColor = if (kryptonColors.isMonochrome) kryptonColors.primary else EmeraldPrimary
    val updatedBadgeColor = if (kryptonColors.isMonochrome) Color(0xFFCBD5E1) else WarningAmber
    val remotePeerColor = if (kryptonColors.isMonochrome) kryptonColors.primary else ElectricMint
    val keepMineColor = if (kryptonColors.isMonochrome) kryptonColors.primary else EmeraldPrimary
    val useTheirsColor = if (kryptonColors.isMonochrome) kryptonColors.primary else ElectricMint

    var diffEntries by remember { mutableStateOf<List<SyncDiffEntry>>(emptyList()) }
    val conflictDecisions = remember { mutableStateMapOf<String, ConflictResolutionChoice>() }
    var isApplying by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    // Compute diff against local items
    LaunchedEffect(remoteItems) {
        val localList = viewModel.getAllItemsDecrypted()
        val localMap = localList.associateBy { it.id }

        val computed = remoteItems.map { remote ->
            val local = localMap[remote.id]
            when {
                local == null -> {
                    SyncDiffEntry(
                        id = remote.id,
                        title = remote.title,
                        username = remote.username,
                        category = SyncItemDiffCategory.NEW,
                        localItem = null,
                        remoteItem = remote,
                        localTimestamp = 0L,
                        remoteTimestamp = remote.updatedAt
                    )
                }
                local.updatedAt < remote.updatedAt -> {
                    conflictDecisions[remote.id] = ConflictResolutionChoice.USE_THEIRS
                    SyncDiffEntry(
                        id = remote.id,
                        title = remote.title,
                        username = remote.username,
                        category = SyncItemDiffCategory.UPDATED,
                        localItem = local,
                        remoteItem = remote,
                        localTimestamp = local.updatedAt,
                        remoteTimestamp = remote.updatedAt
                    )
                }
                local.updatedAt > remote.updatedAt -> {
                    conflictDecisions[remote.id] = ConflictResolutionChoice.KEEP_MINE
                    SyncDiffEntry(
                        id = remote.id,
                        title = remote.title,
                        username = remote.username,
                        category = SyncItemDiffCategory.UPDATED,
                        localItem = local,
                        remoteItem = remote,
                        localTimestamp = local.updatedAt,
                        remoteTimestamp = remote.updatedAt
                    )
                }
                else -> {
                    SyncDiffEntry(
                        id = remote.id,
                        title = remote.title,
                        username = remote.username,
                        category = SyncItemDiffCategory.UNCHANGED,
                        localItem = local,
                        remoteItem = remote,
                        localTimestamp = local.updatedAt,
                        remoteTimestamp = remote.updatedAt
                    )
                }
            }
        }
        diffEntries = computed
    }

    val newCount = diffEntries.count { it.category == SyncItemDiffCategory.NEW }
    val updatedCount = diffEntries.count { it.category == SyncItemDiffCategory.UPDATED }
    val unchangedCount = diffEntries.count { it.category == SyncItemDiffCategory.UNCHANGED }

    Scaffold(
        containerColor = kryptonColors.neuBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Selective Sync Diff",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Air-gapped Bluetooth Merge",
                            fontSize = 12.sp,
                            color = kryptonColors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = kryptonColors.neuBackground
                )
            )
        },
        bottomBar = {
            // Bottom sticky Neumorphic button: "Apply Selected Changes"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(kryptonColors.neuSurface)
                    .border(1.dp, kryptonColors.neuBorderGradient, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                LiquidGlassPillButton(
                    text = if (isApplying) "Applying Changes..." else "Apply Selected Changes",
                    onClick = {
                        scope.launch {
                            isApplying = true
                            for (entry in diffEntries) {
                                when (entry.category) {
                                    SyncItemDiffCategory.NEW -> {
                                        viewModel.saveItem(entry.remoteItem) {}
                                    }
                                    SyncItemDiffCategory.UPDATED -> {
                                        val choice = conflictDecisions[entry.id] ?: ConflictResolutionChoice.USE_THEIRS
                                        if (choice == ConflictResolutionChoice.USE_THEIRS) {
                                            viewModel.saveItem(entry.remoteItem) {}
                                        }
                                    }
                                    SyncItemDiffCategory.UNCHANGED -> {
                                        // No action needed
                                    }
                                }
                            }
                            isApplying = false
                            Toast.makeText(context, "Sync changes successfully applied!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_selected_changes_button"),
                    leadingIcon = {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    isPrimary = true
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Summary Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // New Badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(newBadgeColor.copy(alpha = 0.15f))
                        .border(1.dp, newBadgeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$newCount New",
                        color = newBadgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Updated Badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(updatedBadgeColor.copy(alpha = 0.15f))
                        .border(1.dp, updatedBadgeColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$updatedCount Updated",
                        color = updatedBadgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Unchanged Badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(kryptonColors.surfaceElevated)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$unchangedCount Identical",
                        color = kryptonColors.textSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Diff Items List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(diffEntries) { entry ->
                    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Header Row with Title and Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = kryptonColors.textPrimary
                                    )
                                    if (entry.username.isNotBlank()) {
                                        Text(
                                            text = entry.username,
                                            fontSize = 13.sp,
                                            color = kryptonColors.textSecondary
                                        )
                                    }
                                }

                                // Category Badge
                                when (entry.category) {
                                    SyncItemDiffCategory.NEW -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(newBadgeColor.copy(alpha = 0.2f))
                                                .border(1.dp, newBadgeColor, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "NEW",
                                                color = newBadgeColor,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    SyncItemDiffCategory.UPDATED -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(updatedBadgeColor.copy(alpha = 0.2f))
                                                .border(1.dp, updatedBadgeColor, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "UPDATED",
                                                color = updatedBadgeColor,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    SyncItemDiffCategory.UNCHANGED -> {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(kryptonColors.surfaceElevated)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "UNCHANGED",
                                                color = kryptonColors.textSecondary,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Conflict Resolution: Side-by-side comparison for UPDATED items
                            if (entry.category == SyncItemDiffCategory.UPDATED) {
                                val currentChoice = conflictDecisions[entry.id] ?: ConflictResolutionChoice.USE_THEIRS

                                Spacer(modifier = Modifier.height(4.dp))

                                // Timestamps comparison
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(kryptonColors.neuInsetSurface)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Local Device",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = kryptonColors.textPrimary
                                        )
                                        Text(
                                            text = dateFormat.format(Date(entry.localTimestamp)),
                                            fontSize = 11.sp,
                                            color = kryptonColors.textSecondary
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "Remote Peer",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = remotePeerColor
                                        )
                                        Text(
                                            text = dateFormat.format(Date(entry.remoteTimestamp)),
                                            fontSize = 11.sp,
                                            color = kryptonColors.textSecondary
                                        )
                                    }
                                }

                                // Interactive Radio Selectors: [ Keep Mine ] vs [ Use Theirs ]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // [ Keep Mine ]
                                    val isKeepMine = currentChoice == ConflictResolutionChoice.KEEP_MINE
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isKeepMine) keepMineColor.copy(alpha = 0.2f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isKeepMine) keepMineColor else kryptonColors.outline,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { conflictDecisions[entry.id] = ConflictResolutionChoice.KEEP_MINE }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isKeepMine) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (isKeepMine) keepMineColor else kryptonColors.textTertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Keep Mine",
                                                fontSize = 13.sp,
                                                fontWeight = if (isKeepMine) FontWeight.Bold else FontWeight.Normal,
                                                color = kryptonColors.textPrimary
                                            )
                                        }
                                    }

                                    // [ Use Theirs ]
                                    val isUseTheirs = currentChoice == ConflictResolutionChoice.USE_THEIRS
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isUseTheirs) useTheirsColor.copy(alpha = 0.2f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isUseTheirs) useTheirsColor else kryptonColors.outline,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { conflictDecisions[entry.id] = ConflictResolutionChoice.USE_THEIRS }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isUseTheirs) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = if (isUseTheirs) useTheirsColor else kryptonColors.textTertiary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Use Theirs",
                                                fontSize = 13.sp,
                                                fontWeight = if (isUseTheirs) FontWeight.Bold else FontWeight.Normal,
                                                color = kryptonColors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
