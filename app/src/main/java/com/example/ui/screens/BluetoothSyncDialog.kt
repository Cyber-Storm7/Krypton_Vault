package com.example.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BluetoothSyncManager
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.theme.LocalKryptonColors
import com.example.ui.viewmodel.VaultViewModel
import kotlinx.coroutines.launch
import java.security.SecureRandom

enum class SyncMode {
    SEND,
    RECEIVE
}

/**
 * Encrypted Peer-to-Peer Bluetooth Sync Dialog.
 * Enables zero-internet vault transfer between two physical Android devices.
 */
@Composable
fun BluetoothSyncDialog(
    viewModel: VaultViewModel,
    onOpenSelectiveSync: ((List<com.example.model.VaultItemDecrypted>) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val kryptonColors = LocalKryptonColors.current

    val syncManager = remember { BluetoothSyncManager(context) }
    val syncState by syncManager.syncState.collectAsState()
    val statusMessage by syncManager.statusMessage.collectAsState()
    val syncProgress by syncManager.syncProgress.collectAsState()

    var selectedMode by remember { mutableStateOf(SyncMode.SEND) }
    var syncCode by remember {
        mutableStateOf(syncManager.generate4DigitPin())
    }
    var backupPassphrase by remember { mutableStateOf("KryptonP2PTransferKey!") }

    val bluetoothAdapter = remember {
        (context.getSystemService(android.content.Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager)?.adapter
    }
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var selectedTargetDevice by remember { mutableStateOf<BluetoothDevice?>(null) }

    fun refreshDevices() {
        try {
            if (syncManager.hasBluetoothPermission() && bluetoothAdapter != null) {
                @SuppressLint("MissingPermission")
                pairedDevices = bluetoothAdapter.bondedDevices.toList()
            }
        } catch (_: SecurityException) {}
    }

    LaunchedEffect(Unit) {
        refreshDevices()
    }

    DisposableEffect(Unit) {
        onDispose {
            syncManager.cancel()
        }
    }

    AlertDialog(
        onDismissRequest = {
            syncManager.cancel()
            onDismiss()
        },
        containerColor = kryptonColors.neuSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = kryptonColors.primary
                )
                Text(
                    text = "Encrypted Bluetooth P2P Sync",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = kryptonColors.textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Mode Selector (Send / Receive)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedMode == SyncMode.SEND,
                        onClick = {
                            selectedMode = SyncMode.SEND
                            syncManager.cancel()
                        },
                        label = { Text("Send Vault (Host)") },
                        leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedMode == SyncMode.RECEIVE,
                        onClick = {
                            selectedMode = SyncMode.RECEIVE
                            syncManager.cancel()
                            refreshDevices()
                        },
                        label = { Text("Receive Vault") },
                        leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 4-digit Sync Code Display / Input
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (selectedMode == SyncMode.SEND) "Mutual 4-Digit Sync PIN:" else "Enter Host's 4-Digit Sync PIN:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (selectedMode == SyncMode.SEND) {
                            Text(
                                text = syncCode,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = kryptonColors.primary
                            )
                            Text(
                                text = "Have the receiving device enter this code",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            OutlinedTextField(
                                value = syncCode,
                                onValueChange = { if (it.length <= 4) syncCode = it },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("4-digit PIN") }
                            )
                        }
                    }
                }

                // Receive Mode Device Picker
                if (selectedMode == SyncMode.RECEIVE) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Sending Device:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { refreshDevices() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                            }
                        }

                        if (pairedDevices.isEmpty()) {
                            Text(
                                text = "No paired devices found. Pair sender in Android Settings first.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            ) {
                                items(pairedDevices) { dev ->
                                    @SuppressLint("MissingPermission")
                                    val name = try { dev.name ?: dev.address } catch (_: SecurityException) { "Device" }
                                    val isSelected = selectedTargetDevice == dev

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) kryptonColors.primary.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                            .clickable { selectedTargetDevice = dev }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) kryptonColors.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Status & Progress Indicator
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (syncState == com.example.bluetooth.BluetoothVaultSyncManager.SyncState.ERROR) MaterialTheme.colorScheme.error else kryptonColors.primary
                        )
                        if (syncState == com.example.bluetooth.BluetoothVaultSyncManager.SyncState.LISTENING ||
                            syncState == com.example.bluetooth.BluetoothVaultSyncManager.SyncState.CONNECTING ||
                            syncState == com.example.bluetooth.BluetoothVaultSyncManager.SyncState.TRANSFERRING
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = kryptonColors.primary
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { syncProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = kryptonColors.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Action Button
                if (selectedMode == SyncMode.SEND) {
                    LiquidGlassPillButton(
                        text = if (syncState == com.example.bluetooth.BluetoothVaultSyncManager.SyncState.LISTENING) "Listening for Peer..." else "Start Bluetooth Broadcast",
                        onClick = {
                            coroutineScope.launch {
                                val passChars = backupPassphrase.toCharArray()
                                viewModel.createSyncPayload(passChars) { backupPayload ->
                                    if (backupPayload != null) {
                                        coroutineScope.launch {
                                            syncManager.startServerAndSend(
                                                fourDigitPin = syncCode,
                                                payloadBytes = backupPayload,
                                                onComplete = {
                                                    try {
                                                        Toast.makeText(context, "Vault transmitted successfully!", Toast.LENGTH_SHORT).show()
                                                    } catch (_: Exception) {}
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        isPrimary = true
                    )
                } else {
                    LiquidGlassPillButton(
                        text = if (syncState == com.example.bluetooth.BluetoothVaultSyncManager.SyncState.TRANSFERRING) "Receiving..." else "Connect & Sync from Host",
                        onClick = {
                            val target = selectedTargetDevice
                            if (target == null) {
                                Toast.makeText(context, "Select sender device first", Toast.LENGTH_SHORT).show()
                                return@LiquidGlassPillButton
                            }
                            if (syncCode.length != 4) {
                                Toast.makeText(context, "Enter 4-digit sync PIN", Toast.LENGTH_SHORT).show()
                                return@LiquidGlassPillButton
                            }

                            coroutineScope.launch {
                                val receivedBytes = syncManager.connectAndReceive(target, syncCode)
                                if (receivedBytes != null) {
                                    val passChars = backupPassphrase.toCharArray()
                                    try {
                                        val parsedItems = viewModel.parseBackupItems(receivedBytes, passChars)
                                        if (onOpenSelectiveSync != null) {
                                            onDismiss()
                                            onOpenSelectiveSync(parsedItems)
                                        } else {
                                            for (item in parsedItems) {
                                                viewModel.saveItem(item) {}
                                            }
                                            try {
                                                Toast.makeText(context, "Vault successfully merged (${parsedItems.size} items)!", Toast.LENGTH_LONG).show()
                                            } catch (_: Exception) {}
                                            onDismiss()
                                        }
                                    } catch (e: Exception) {
                                        try {
                                            Toast.makeText(context, "Sync parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        } catch (_: Exception) {}
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        isPrimary = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    syncManager.cancel()
                    onDismiss()
                }
            ) {
                Text("Close", color = kryptonColors.primary, fontWeight = FontWeight.Bold)
            }
        }
    )
}
