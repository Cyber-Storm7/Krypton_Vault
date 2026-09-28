package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.theme.LocalKryptonColors

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.TotpGenerator
import com.example.crypto.wipe
import com.example.model.VaultItemSummary
import com.example.model.VaultItemType
import com.example.ui.viewmodel.VaultViewModel
import com.example.util.ClipboardHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultListScreen(
    viewModel: VaultViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAudit: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLockVault: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val items by viewModel.filteredItems.collectAsState()
    val context = LocalContext.current
    val kryptonColors = LocalKryptonColors.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.statusMessage) {
        state.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = kryptonColors.neuBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(kryptonColors.neuSurface)
                                .border(1.dp, kryptonColors.neuBorderGradient, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = kryptonColors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "KryptonVault",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = kryptonColors.textPrimary
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(kryptonColors.neuSurface)
                            .border(1.dp, kryptonColors.neuBorderGradient, CircleShape)
                            .clickable { onNavigateToAudit() }
                            .testTag("nav_audit_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "Security Audit", tint = kryptonColors.primary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(kryptonColors.neuSurface)
                            .border(1.dp, kryptonColors.neuBorderGradient, CircleShape)
                            .clickable { menuExpanded = true }
                            .testTag("nav_overflow_menu"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = kryptonColors.textSecondary, modifier = Modifier.size(18.dp))
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(kryptonColors.neuSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Vault Settings", color = kryptonColors.textPrimary) },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = kryptonColors.primary) },
                            onClick = {
                                menuExpanded = false
                                onNavigateToSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lock Vault Now", color = kryptonColors.textPrimary) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = kryptonColors.primary) },
                            onClick = {
                                menuExpanded = false
                                viewModel.lockVault()
                                onLockVault()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = kryptonColors.neuBackground
                )
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 90.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(kryptonColors.primary)
                    .clickable { onNavigateToDetail("new") }
                    .testTag("add_item_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Item",
                    tint = kryptonColors.onPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Neumorphic Concave Search Bar
            LiquidGlassTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = "Search vault secrets...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = kryptonColors.primary
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = kryptonColors.primary
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                testTag = "search_bar_input"
            )

            // Neumorphic Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val categories = listOf("All", "Logins", "Notes", "Cards", "2FA")
                items(categories) { category ->
                    val isSelected = state.selectedCategory == category
                    val chipShape = RoundedCornerShape(percent = 50)
                    Box(
                        modifier = Modifier
                            .clip(chipShape)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(kryptonColors.neuInsetSurface)
                                        .border(1.dp, kryptonColors.neuInsetBorderGradient, chipShape)
                                } else {
                                    Modifier
                                        .background(kryptonColors.neuSurface)
                                        .border(1.dp, kryptonColors.neuBorderGradient, chipShape)
                                }
                            )
                            .clickable { viewModel.setSelectedCategory(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) kryptonColors.primary else kryptonColors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Vault Items List or Empty State
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (state.searchQuery.isNotBlank()) "No matching records found" else "Your Vault is Empty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (state.searchQuery.isNotBlank()) "Try changing your search keywords" else "Tap '+' below to store your passwords, notes, or 2FA authenticators securely.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }, contentType = { it.type }) { item ->
                        VaultItemCard(
                            item = item,
                            onItemClick = { onNavigateToDetail(item.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(item.id) },
                            onCopyUsername = {
                                if (item.username.isNotBlank()) {
                                    ClipboardHelper.copyPlain(context, "Username", item.username)
                                    Toast.makeText(context, "Username copied", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onCopyPassword = {
                                scope.launch {
                                    val password = viewModel.getPasswordForCopy(item.id)
                                    if (password != null) {
                                        ClipboardHelper.copySensitive(context, "Password", password)
                                        password.wipe()
                                        Toast.makeText(context, "Password copied! Auto-clearing in 30s", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "No password saved", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultItemCard(
    item: VaultItemSummary,
    onItemClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopyUsername: () -> Unit,
    onCopyPassword: () -> Unit
) {
    val kryptonColors = LocalKryptonColors.current

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .testTag("vault_item_card_${item.id}"),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Neumorphic Concave Socket for Item Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(kryptonColors.neuInsetSurface)
                        .border(1.dp, kryptonColors.neuInsetBorderGradient, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (item.type) {
                        VaultItemType.LOGIN -> Icons.Default.Key
                        VaultItemType.SECURE_NOTE -> Icons.Default.Description
                        VaultItemType.CREDIT_CARD -> Icons.Default.CreditCard
                        VaultItemType.IDENTITY -> Icons.Default.Person
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = kryptonColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = kryptonColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.username.isNotBlank()) {
                        Text(
                            text = item.username,
                            fontSize = 13.sp,
                            color = kryptonColors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Favorite Toggle
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) (if (kryptonColors.isMonochrome) kryptonColors.primary else Color(0xFFFBBF24)) else kryptonColors.textSecondary
                    )
                }
            }

            // TOTP Live 2FA Row if item has TOTP
            if (item.hasTotp && item.totpSecret.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                TotpLiveCardRow(secret = item.totpSecret)
            }

            // Quick Actions Footer
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.username.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(kryptonColors.neuSurface)
                            .border(1.dp, kryptonColors.neuBorderGradient, CircleShape)
                            .clickable { onCopyUsername() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Copy Username",
                            modifier = Modifier.size(16.dp),
                            tint = kryptonColors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(kryptonColors.neuSurface)
                        .border(1.dp, kryptonColors.neuBorderGradient, CircleShape)
                        .clickable { onCopyPassword() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy Password",
                        modifier = Modifier.size(16.dp),
                        tint = kryptonColors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Isolated TOTP live ticker row:
 * Ticking runs ONLY for items with active TOTP, preventing unnecessary recomposition
 * of other non-TOTP vault item cards.
 */
@Composable
private fun TotpLiveCardRow(
    secret: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val kryptonColors = LocalKryptonColors.current
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(secret) {
        while (true) {
            val now = System.currentTimeMillis()
            currentTimeMillis = now
            val sleepMs = 1000L - (now % 1000L)
            delay(sleepMs.coerceAtLeast(100L))
        }
    }

    val currentWindow = currentTimeMillis / 30000L
    val totpCode = remember(secret, currentWindow) {
        TotpGenerator.generateTotp(secret, timeMillis = currentTimeMillis)
    }
    val remainingSeconds = TotpGenerator.getRemainingSeconds(currentTimeMillis)
    val progress = TotpGenerator.getRemainingProgress(currentTimeMillis)
    val primaryColor = kryptonColors.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(kryptonColors.neuInsetSurface)
            .border(1.dp, kryptonColors.neuInsetBorderGradient, RoundedCornerShape(10.dp))
            .clickable {
                ClipboardHelper.copySensitive(context, "TOTP Code", totpCode.toCharArray())
                Toast.makeText(context, "2FA Code copied! Auto-clearing in 30s", Toast.LENGTH_SHORT).show()
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp,
                    color = if (remainingSeconds <= 5) MaterialTheme.colorScheme.error else primaryColor,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = totpCode.chunked(3).joinToString(" "),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = primaryColor
            )
        }

        Text(
            text = "${remainingSeconds}s",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = kryptonColors.textSecondary
        )
    }
}
