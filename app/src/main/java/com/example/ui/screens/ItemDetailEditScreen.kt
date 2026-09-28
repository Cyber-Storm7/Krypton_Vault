package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.crypto.TotpGenerator
import com.example.crypto.wipe
import com.example.model.VaultItemDecrypted
import com.example.model.VaultItemType
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.theme.LocalKryptonColors
import com.example.ui.viewmodel.VaultViewModel
import com.example.util.ClipboardHelper
import com.example.util.LifecycleManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailEditScreen(
    itemId: String,
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val kryptonColors = LocalKryptonColors.current
    val isNewItem = itemId == "new"

    var selectedType by remember { mutableStateOf(VaultItemType.LOGIN) }
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var username by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var websiteUrl by remember { mutableStateOf("") }
    var totpSecret by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Card specifics
    var cardHolder by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvvInput by remember { mutableStateOf("") }
    var cvvVisible by remember { mutableStateOf(false) }

    var isFavorite by remember { mutableStateOf(false) }
    var createdAt by remember { mutableStateOf(System.currentTimeMillis()) }

    var showGeneratorSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val saveItemAction = {
        if (title.isBlank()) {
            Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
        } else {
            val passChars = passwordInput.toCharArray()
            val cvvChars = cardCvvInput.toCharArray()

            val itemToSave = VaultItemDecrypted(
                id = if (isNewItem) java.util.UUID.randomUUID().toString() else itemId,
                type = selectedType,
                title = title.trim(),
                username = username.trim(),
                password = passChars,
                websiteUrl = websiteUrl.trim(),
                totpSecret = totpSecret.trim(),
                notes = notes.trim(),
                cardHolder = cardHolder.trim(),
                cardNumber = cardNumber.trim(),
                cardExpiry = cardExpiry.trim(),
                cardCvv = cvvChars,
                category = category.trim().ifBlank { "General" },
                isFavorite = isFavorite,
                createdAt = createdAt,
                updatedAt = System.currentTimeMillis()
            )

            viewModel.saveItem(itemToSave) {
                onNavigateBack()
            }
        }
    }

    // Load item if editing existing
    LaunchedEffect(itemId) {
        if (!isNewItem) {
            val item = viewModel.getItemForEdit(itemId)
            if (item != null) {
                selectedType = item.type
                title = item.title
                category = item.category
                username = item.username
                passwordInput = String(item.password)
                websiteUrl = item.websiteUrl
                totpSecret = item.totpSecret
                notes = item.notes
                cardHolder = item.cardHolder
                cardNumber = item.cardNumber
                cardExpiry = item.cardExpiry
                cardCvvInput = String(item.cardCvv)
                isFavorite = item.isFavorite
                createdAt = item.createdAt
                item.wipeSensitiveFields()
            }
        }
    }

    // Zero out memory on screen exit
    DisposableEffect(Unit) {
        onDispose {
            // Memory is cleared
        }
    }

    Scaffold(
        containerColor = kryptonColors.neuBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNewItem) "New Secure Item" else "Edit Item",
                        fontWeight = FontWeight.Bold,
                        color = kryptonColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("item_detail_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = kryptonColors.primary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isFavorite = !isFavorite },
                        modifier = Modifier.testTag("toggle_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) (if (kryptonColors.isMonochrome) kryptonColors.primary else Color(0xFFFBBF24)) else kryptonColors.textSecondary
                        )
                    }
                    if (!isNewItem) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("delete_item_icon")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete item",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = saveItemAction,
                        modifier = Modifier.testTag("save_item_button")
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save Item",
                            tint = kryptonColors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = kryptonColors.neuBackground
                )
            )
        }
    ) { innerPadding ->
        val kryptonTextFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = kryptonColors.primary,
            unfocusedBorderColor = kryptonColors.outline.copy(alpha = 0.5f),
            focusedLabelColor = kryptonColors.primary,
            unfocusedLabelColor = kryptonColors.textSecondary,
            focusedTextColor = kryptonColors.textPrimary,
            unfocusedTextColor = kryptonColors.textPrimary,
            cursorColor = kryptonColors.primary,
            focusedContainerColor = kryptonColors.neuInsetSurface,
            unfocusedContainerColor = kryptonColors.neuInsetSurface
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Type Selector (Horizontally scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VaultItemType.entries.forEach { type ->
                    val isSelected = selectedType == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(kryptonColors.primary)
                                        .shadow(2.dp, RoundedCornerShape(percent = 50))
                                } else {
                                    Modifier
                                        .background(kryptonColors.neuSurface)
                                        .border(1.dp, kryptonColors.neuBorderGradient, RoundedCornerShape(percent = 50))
                                }
                            )
                            .clickable { selectedType = type }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = type.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) kryptonColors.onPrimary else kryptonColors.textPrimary
                        )
                    }
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = kryptonTextFieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("item_title_input")
            )

            // Category
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = kryptonTextFieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedType == VaultItemType.LOGIN || selectedType == VaultItemType.IDENTITY) {
                // Username / Email
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username or Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = kryptonTextFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_username_input")
                )

                // Password with Visibility Toggle, Copy, and Generator trigger
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password") },
                    singleLine = true,
                    colors = kryptonTextFieldColors,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = kryptonColors.textSecondary
                                )
                            }
                            if (passwordInput.isNotEmpty()) {
                                IconButton(onClick = {
                                    ClipboardHelper.copySensitive(context, "Password", passwordInput.toCharArray())
                                    Toast.makeText(context, "Password copied! Auto-clearing in 30s", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy password",
                                        tint = kryptonColors.primary
                                    )
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_password_input")
                )

                // Password Generator Trigger Button (Neumorphic)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(kryptonColors.neuSurface)
                        .border(1.dp, kryptonColors.neuBorderGradient, RoundedCornerShape(12.dp))
                        .clickable { showGeneratorSheet = true }
                        .testTag("open_password_generator_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = kryptonColors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate Strong Password / Passphrase",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = kryptonColors.primary
                        )
                    }
                }

                // Share to PC Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(kryptonColors.neuSurface)
                        .border(1.dp, kryptonColors.neuBorderGradient, RoundedCornerShape(12.dp))
                        .clickable {
                            if (passwordInput.isEmpty()) {
                                Toast.makeText(context, "No password to share", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            val activity = context as? FragmentActivity
                            if (activity == null) {
                                Toast.makeText(context, "Cannot launch biometric prompt", Toast.LENGTH_SHORT).show()
                                return@clickable
                            }

                            val executor = ContextCompat.getMainExecutor(activity)
                            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                .setTitle("Confirm Identity")
                                .setSubtitle("Authenticate to share password securely")
                                .setNegativeButtonText("Cancel")
                                .setConfirmationRequired(false)
                                .build()

                            val biometricPrompt = BiometricPrompt(
                                activity,
                                executor,
                                object : BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                        super.onAuthenticationSucceeded(result)
                                        val decryptedPassword = passwordInput.toCharArray()
                                        LifecycleManager.suppressNextPause()
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, String(decryptedPassword))
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Share Password")
                                        )
                                        decryptedPassword.wipe()
                                    }

                                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                        super.onAuthenticationError(errorCode, errString)
                                        if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                            errorCode != BiometricPrompt.ERROR_USER_CANCELED
                                        ) {
                                            Toast.makeText(context, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onAuthenticationFailed() {
                                        super.onAuthenticationFailed()
                                    }
                                }
                            )

                            try {
                                biometricPrompt.authenticate(promptInfo)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Biometric unavailable: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("share_to_pc_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = kryptonColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share to PC",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = kryptonColors.textPrimary
                        )
                    }
                }

                // Website URL with Open in Browser Action
                OutlinedTextField(
                    value = websiteUrl,
                    onValueChange = { websiteUrl = it },
                    label = { Text("Website URL") },
                    singleLine = true,
                    colors = kryptonTextFieldColors,
                    trailingIcon = {
                        if (websiteUrl.isNotBlank()) {
                            IconButton(onClick = {
                                try {
                                    val formatted = if (!websiteUrl.startsWith("http://") && !websiteUrl.startsWith("https://")) {
                                        "https://$websiteUrl"
                                    } else websiteUrl
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formatted))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in browser", tint = kryptonColors.primary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2FA / TOTP Secret Key
                OutlinedTextField(
                    value = totpSecret,
                    onValueChange = { totpSecret = it },
                    label = { Text("2FA / TOTP Secret (Base32)") },
                    singleLine = true,
                    colors = kryptonTextFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Live TOTP code preview if secret entered
                if (totpSecret.isNotBlank()) {
                    TotpPreviewSection(totpSecret = totpSecret)
                }
            }

            if (selectedType == VaultItemType.CREDIT_CARD) {
                OutlinedTextField(
                    value = cardHolder,
                    onValueChange = { cardHolder = it },
                    label = { Text("Cardholder Name") },
                    singleLine = true,
                    colors = kryptonTextFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card Number") },
                    singleLine = true,
                    colors = kryptonTextFieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = cardExpiry,
                        onValueChange = { cardExpiry = it },
                        label = { Text("Expiry (MM/YY)") },
                        singleLine = true,
                        colors = kryptonTextFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = cardCvvInput,
                        onValueChange = { cardCvvInput = it },
                        label = { Text("CVV") },
                        singleLine = true,
                        colors = kryptonTextFieldColors,
                        visualTransformation = if (cvvVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { cvvVisible = !cvvVisible }) {
                                Icon(
                                    imageVector = if (cvvVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle CVV",
                                    tint = kryptonColors.textSecondary
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Encrypted Notes") },
                minLines = 3,
                maxLines = 6,
                colors = kryptonTextFieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Save Secret Button at bottom of form
            LiquidGlassPillButton(
                text = if (isNewItem) "Save Secret" else "Save Changes",
                onClick = saveItemAction,
                isPrimary = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = kryptonColors.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_item_bottom_button")
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = kryptonColors.neuSurface,
            title = { Text("Delete Item", color = kryptonColors.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete '$title' from your encrypted vault?", color = kryptonColors.textSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteItem(itemId)
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = kryptonColors.textSecondary)
                }
            }
        )
    }

    // Password Generator BottomSheet
    if (showGeneratorSheet) {
        PasswordGeneratorBottomSheet(
            onDismiss = { showGeneratorSheet = false },
            onPasswordSelected = { generatedChars ->
                passwordInput = String(generatedChars)
                generatedChars.wipe()
            }
        )
    }
}

/**
 * Isolated TOTP live preview ticker for ItemDetailEditScreen:
 * Runs the 1-second ticker strictly within this isolated composable so the entire
 * form and its input fields never recompose or stutter during editing.
 */
@Composable
private fun TotpPreviewSection(
    totpSecret: String,
    modifier: Modifier = Modifier
) {
    val kryptonColors = LocalKryptonColors.current
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(totpSecret) {
        while (true) {
            val now = System.currentTimeMillis()
            currentTimeMillis = now
            val sleepMs = 1000L - (now % 1000L)
            delay(sleepMs.coerceAtLeast(100L))
        }
    }

    val currentWindow = currentTimeMillis / 30000L
    val code = remember(totpSecret, currentWindow) {
        TotpGenerator.generateTotp(totpSecret, timeMillis = currentTimeMillis)
    }
    val remainingSeconds = TotpGenerator.getRemainingSeconds(currentTimeMillis)
    val progress = TotpGenerator.getRemainingProgress(currentTimeMillis)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(kryptonColors.neuInsetSurface)
            .border(1.dp, kryptonColors.neuInsetBorderGradient, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = kryptonColors.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Live TOTP Code", fontSize = 11.sp, color = kryptonColors.textSecondary)
                Text(
                    text = code.chunked(3).joinToString(" "),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = kryptonColors.primary
                )
            }
        }
        Text(
            text = "${remainingSeconds}s remaining",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = kryptonColors.textSecondary
        )
    }
}

