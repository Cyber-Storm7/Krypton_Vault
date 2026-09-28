package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Unlock : Screen("unlock")
    object VaultList : Screen("vault_list")
    object Generator : Screen("generator")
    object SecurityAudit : Screen("security_audit")
    object Settings : Screen("settings")
    object SelectiveSync : Screen("selective_sync")
    object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: String = "new"): String = "item_detail/$itemId"
    }
}
