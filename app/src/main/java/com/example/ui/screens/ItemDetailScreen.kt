package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.ui.viewmodel.VaultViewModel

/**
 * ItemDetailScreen composable conforming to the KryptonVault engineering specification.
 * Forwards directly to ItemDetailEditScreen.
 */
@Composable
fun ItemDetailScreen(
    itemId: String,
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit
) {
    ItemDetailEditScreen(
        itemId = itemId,
        viewModel = viewModel,
        onNavigateBack = onNavigateBack
    )
}
